/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.topology;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import quarks.execution.Job;
import quarks.providers.direct.DirectProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * Using the Job API to get/set a job's state.
 */
public class JobExecution {
    public final static long JOB_LIFE_MILLIS = 10000;
    public final static long WAIT_AFTER_CLOSE = 2000;

    public static void main(String[] args) throws ExecutionException {

        DirectProvider tp = new DirectProvider();

        Topology t = tp.newTopology("JobExecution");

        // Source
        Random r = new Random();
        TStream<Double> gaussian = t.poll(() -> r.nextGaussian(), 1, TimeUnit.SECONDS);

        // Peek
        gaussian = gaussian.peek(g -> System.out.println("R:" + g));
  
        // Transform to strings
        TStream<String> gsPeriodic = gaussian.map(g -> "G:" + g + ":");
        gsPeriodic.print();
  
        // Submit job and poll its status for a while
        Future<Job> futureJob = tp.submit(t);
        Reporter reporter = new Reporter();
        try {
            Job job = futureJob.get();
            reporter.start(job);

            // Wait for the job to complete
            try {
                job.complete(JOB_LIFE_MILLIS, TimeUnit.MILLISECONDS);
                System.out.println("The job completed successfully");
            } catch (ExecutionException e) {
                System.out.println("The job aborted by throwing exception: " + e);
            }
            catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for the job to complete");
            }
            catch (TimeoutException e) {
                System.out.println("Timed out while waiting for the job to complete");
            }
            finally {
                System.out.println("Closing the job...");
                job.stateChange(Job.Action.CLOSE);
            }
            System.out.println("Sleep after job close for " + WAIT_AFTER_CLOSE + " ms");
            Thread.sleep(WAIT_AFTER_CLOSE);
        }
        catch (InterruptedException e) {
            System.err.println("Interrupted!");
        }
        finally {
            reporter.stop();
        }
    }

    static class Reporter implements Runnable {
        private volatile Job job;
        private volatile Thread runner;
        
        @Override
        public void run() {
            try {
                while (true) {
                    if (job != null)
                        System.out.println("Job state is: current=" + job.getCurrentState() + 
                                " next=" + job.getNextState());
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                System.out.println("Reporter interrupted");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        void start(Job job) {
            this.job = job;
            runner = Executors.defaultThreadFactory().newThread(this);
            runner.setName("Reporter");
            runner.setDaemon(false);
            runner.start();
        }
        
        void stop() {
            runner.interrupt();
        }
    }
}
