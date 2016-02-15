/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.samples.topology;

import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import quarks.execution.Job;
import quarks.providers.direct.DirectProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

public class SimpleFilterTransform {
    public static void main(String[] args) throws Exception {

        DirectProvider tp = new DirectProvider();

        Topology t = tp.newTopology("SimpleFilterTransform");

        Random r = new Random();
        TStream<Double> gaussian = t.generate(() -> r.nextGaussian());

        // testing Peek!
        gaussian = gaussian.peek(g -> System.out.println("R:" + g));

        // A filter
        gaussian = gaussian.filter(g -> g > 0.5);

        // A transformation
        TStream<String> gs = gaussian.map(g -> "G:" + g + ":");
        gs.print();

        // Submit the job, then close it after a while 
        Future<Job> futureJob = tp.submit(t);
        Job job = futureJob.get();
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        job.stateChange(Job.Action.CLOSE);
    }
}
