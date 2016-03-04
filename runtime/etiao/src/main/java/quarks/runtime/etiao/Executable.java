/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016  
*/
package quarks.runtime.etiao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import quarks.execution.Job;
import quarks.execution.services.RuntimeServices;
import quarks.execution.services.ServiceContainer;
import quarks.function.BiConsumer;
import quarks.function.Consumer;
import quarks.oplet.Oplet;

/**
 * Executes and provides runtime services to the executable graph 
 * elements (oplets and functions).
 */
public class Executable implements RuntimeServices {

    private final EtiaoJob job;
    private final ThreadFactory controlThreads;
    private final BiConsumer<Object, Throwable> completionHandler;
    private final ThreadFactoryTracker userThreads;
    private final TrackingScheduledExecutor controlScheduler;
    private final TrackingScheduledExecutor userScheduler;
    private Throwable lastError;
    
    /**
     * Services specific to this job.
     */
    private final ServiceContainer jobServices  = new ServiceContainer();

    private List<Invocation<? extends Oplet<?, ?>, ?, ?>> invocations = new ArrayList<>();

    /**
     * Creates a new {@code Executable} for the specified job.
     * @param job {@code Job} implementation controlling this {@code Executable}.
     */
    public Executable(EtiaoJob job) {
        this(job, null);
    }

    /**
     * Creates a new {@code Executable} for the specified job, which uses the 
     * provided thread factory to create new threads for executing the oplets.
     * 
     * @param job {@code Job} implementation controlling this {@code Executable}
     * @param threads thread factory for executing the oplets
     */
    public Executable(EtiaoJob job, ThreadFactory threads) {
        this.job = job;
        this.controlThreads = (threads != null) ? threads : Executors.defaultThreadFactory();
        this.completionHandler = new BiConsumer<Object, Throwable>() {
            // XXX Use the project's BiConsumer (serializable) implementation to avoid 
            // depending on Java 8's functional interfaces.
            private static final long serialVersionUID = 1L;

            /**
             * Handler invoked by userThreads, userScheduler, and controlScheduler,
             * upon handling an uncaught exception from a user task or when they 
             * have completed all the tasks.
             * 
             * @param t The uncaught exception; null when called because all 
             *      tasks have completed.  
             */
            @Override
            public void accept(Object source, Throwable t) {
                if (t != null) {
                    Executable.this.setLastError(t);
                    cleanup();
                }
                else if (job.getCurrentState() == Job.State.RUNNING &&
                        (source == userScheduler || source == userThreads) && 
                        !hasActiveTasks()) {
                    // TODO trace message, debugging
                    // System.err.println("No more active user tasks");
                }
                notifyCompleter();
            }  
        };
        this.userThreads = new ThreadFactoryTracker(job.getName(), controlThreads, completionHandler);
        this.controlScheduler = TrackingScheduledExecutor.newScheduler(controlThreads, completionHandler);
        this.userScheduler = TrackingScheduledExecutor.newScheduler(userThreads, completionHandler);
    }

    private ThreadFactory getThreads() {
        return userThreads;
    }

    /**
     * Returns the {@code ScheduledExecutorService} used for running 
     * executable graph elements.
     * 
     * @return the scheduler
     */
    public ScheduledExecutorService getScheduler() {
        return userScheduler;
    }
    
    /**
     * Acts as a service provider for executable elements in the graph, first
     * looking for a service specific to this job, and then one from the 
     * container.
     */
    @Override
    public <T> T getService(Class<T> serviceClass) {
        T service = jobServices.getService(serviceClass);
        if (service != null)
            return service;
                    
        return job.getContainerServices().getService(serviceClass);
    }

    /**
     * Creates a new {@code Invocation} associated with the specified oplet.
     * 
     * @param oplet the oplet
     * @param inputs the invocation's inputs
     * @param outputs the invocation's outputs
     * @return a new invocation for the given oplet
     */
    public <T extends Oplet<I, O>, I, O> Invocation<T, I, O> addOpletInvocation(T oplet, int inputs, int outputs) {
        Invocation<T, I, O> invocation = new Invocation<>(
        		Invocation.ID_PREFIX + invocations.size(), oplet, inputs, outputs);
        invocations.add(invocation);
        return invocation;
    }

    /**
     * Initializes the 
     */
    public void initialize() {
        jobServices.addService(ThreadFactory.class, getThreads());
        jobServices.addService(ScheduledExecutorService.class, getScheduler());
        invokeAction(invocation -> invocation.initialize(job, this));
    }

    /**
     * Starts all the invocations.
     */
    public void start() {
        invokeAction(invocation -> invocation.start());
    }

    /**
     * Shutdown the user scheduler and thread factory, close all 
     * invocations, then shutdown the control scheduler.
     */
    public void close() {
        getScheduler().shutdownNow();
        userThreads.shutdownNow();
        
        invokeAction(invocation -> {
            try {
                invocation.close();
            }
            catch (Throwable t) {
                // TODO log, don't rethrow
                t.printStackTrace();
            } finally {
                jobServices.cleanOplet(job.getId(), invocation.getId());
                job.getContainerServices().cleanOplet(job.getId(), invocation.getId());
            }
        });

        notifyCompleter();
        List<Runnable> unfinished = controlScheduler.shutdownNow();
        if (!unfinished.isEmpty()) {
            // TODO log warning if there are unfinished tasks
            System.err.println("Could not finish " + unfinished.size() + " tasks");
        }
    }

    private void invokeAction(Consumer<Invocation<?, ?, ?>> action) {
        ExecutorCompletionService<Boolean> completer = new ExecutorCompletionService<>(controlScheduler);
        for (Invocation<?, ?, ?> invocation : invocations) {
            completer.submit(() -> {
                action.accept(invocation);
                return true;
            });
        }

        int remainingTasks = invocations.size();
        while (remainingTasks > 0) {
            try {
                Future<Boolean> completed = completer.poll(10, TimeUnit.SECONDS);
                if (completed == null) {
                    // TODO logging
                    System.err.println("Completer timed out");
                    throw new RuntimeException(new TimeoutException());
                }
                else {
                    try {
                        completed.get();
                    }
                    catch (ExecutionException | InterruptedException | CancellationException e) {
                        // TODO logging
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            remainingTasks--;
        }
        
        job.onActionComplete();
    }

    /**
     * Cleanup after failure.
     */
    private void cleanup() {
        userScheduler.shutdown();
        userThreads.shutdown();
    }

    /**
     * Check whether there are user tasks still active.
     * @return {@code true} if at least a user task is still active.
     */
    public boolean hasActiveTasks() {
        return userScheduler.hasActiveTasks() || 
               userThreads.hasActiveNonDaemonThreads();
    }

    public synchronized Throwable getLastError() {
        return lastError;
    }

    private synchronized void setLastError(Throwable lastError) {
        this.lastError = lastError;
    }
    
    /**
     * The thread that is waiting for completion of the Executable's 
     * asynchronous work, may be null.
     */
    private Thread completer;
    private boolean completerNotify;

    /**
     * Waits for outstanding user threads or tasks.
     * 
     * @throws ExecutionException if the job execution threw an exception.  
     *      Wraps the latest uncaught Exception thrown by a background activity.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @return true if the {@code Executable} has completed, false if the if the wait timed out.
     */
    final boolean complete(long timeoutMillis) throws InterruptedException, ExecutionException {
        long totalWait = timeoutMillis;
        if (totalWait <= 0)
            totalWait = 1000;

        synchronized (this) {
            completer = Thread.currentThread();
        }

        final long start = System.currentTimeMillis();
        try {
            while ((System.currentTimeMillis() - start) < totalWait) {                
                if (Thread.interrupted())  // Clears interrupted status
                    throw new InterruptedException();
                
                // Check for errors from background activities
                Throwable t = getLastError();
                if (t != null) {
                    throw executionException(t);
                }
                
                if (!hasActiveTasks()) {
                    break;
                }
                
                // Wait for notification that something interesting to us has
                // terminated.
                synchronized (completer) {
                    if (!completerNotify) {
                        try {
                            completer.wait(totalWait);                        
                        } catch (InterruptedException e) {
                            if (!completerNotify) {
                                // Interrupted, but not by a notification
                                throw e;
                            }
                        }
                    }
                    completerNotify = false;
                }
            }
        } finally {
            synchronized (this) {
                completer = null;
            }
        }
        return ((System.currentTimeMillis() - start) < totalWait);
    }
    
    private void notifyCompleter() {
        Thread completer;
        synchronized (this) {
            completer = this.completer;
        }
        if (completer == null)
            return;
        
        synchronized (completer) {
            completerNotify = true;
            completer.notifyAll();
        }
    }
    
    static ExecutionException executionException(Throwable t) {
        return (t instanceof ExecutionException) ? 
                (ExecutionException) t : new ExecutionException(t);
    }
}
