/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
*/
package quarks.runtime.etiao;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quarks.function.BiConsumer;
import quarks.oplet.core.Source;

/**
 * Tracks threads created for executing user tasks.
 * <p>
 * All user threads are interrupted when the tracker is shutdown.
 * Runnable implementations (see {@link Source}) must exit the task 
 * if the current thread is interrupted. A handler which notifies the 
 * {@link Executable} is invoked when a user thread abruptly terminates due 
 * to an uncaught exception.</p>
 * <p>
 * If no {@code ThreadFactory} is provided, then this object uses the
 * factory returned by {@link Executors#defaultThreadFactory()}.</p>
 */
public class ThreadFactoryTracker implements ThreadFactory {

    private final String threadName;
    private final ThreadFactory factory;
    private final BiConsumer<Object, Throwable> completer;
    private final Thread.UncaughtExceptionHandler handler;
    private volatile boolean shutdown;
    private final ThreadSets threads = new ThreadSets();
    private static final Logger logger = LoggerFactory.getLogger(ThreadFactoryTracker.class);

    ThreadFactoryTracker(String threadName, ThreadFactory tf, BiConsumer<Object, Throwable> completer) {
        this.threadName = threadName;
		this.factory = (tf != null) ? tf : Executors.defaultThreadFactory();
		this.completer = completer;
        this.handler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                try {
                    if (!trackedThreadUncaughtException(thread, throwable))
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, throwable);
                } finally {
                    threads.removeRunning(Thread.currentThread());
                    completer.accept(this, throwable);
                }
            }};
    }

    /**
     * Return a thread.
     */
    @Override
    public Thread newThread(Runnable r) {
        if (shutdown)
            return null;

        Runnable wrapper = new Runnable() {
            /**
             * Keep track of non-daemon threads so shutdown can wait for
             * their termination.
             */
            @Override
            public void run() {
                threads.fromNewToRunning(Thread.currentThread());
                
                r.run();
                /* If the task has thrown an exception, the code below is 
                 * not getting executed. The uncaughtException this.handler 
                 * removes the thread from the tracker list and notifies 
                 * the completer.
                 */  
                threads.removeRunning(Thread.currentThread());
                if (!hasActiveNonDaemonThreads())
                    completer.accept(ThreadFactoryTracker.this, null);
            }
        };
        
        Thread t = factory.newThread(wrapper);
        t.setName(t.getName() + "-" + threadName);
        t.setUncaughtExceptionHandler(handler);
        threads.addNew(t);
        return t;
    }

    /**
     * This initiates an orderly shutdown in which no new tasks will be 
     * accepted but previously submitted tasks continue to be executed.
     */
    public void shutdown() {
        shutdown = true;
    }

    /**
     * Interrupts all user treads and briefly waits for each thread to finish
     * execution.
     * 
     * User tasks must catch {@link InterruptedException} and exit the task.
     */
    public void shutdownNow() {
        // Ensure no new threads will be created.
        shutdown();

        Thread[] leftoverThreads = threads.runningArray();
        for (Thread t : leftoverThreads)
            t.interrupt();
        // TODO should we interrupt newThreads as well
        
        // Briefly wait for threads to complete
        for (Thread t : leftoverThreads) {
            try {
                t.join(10);
            } catch (InterruptedException e) {
            }
        }
    }
    
    /**
     * Check to see if there are non daemon user threads that have not yet 
     * completed.  This includes non-daemon threads which have been created 
     * but are not running yet.
     * @return {@code true} if there are active non daemon threads, false otherwise.
     */
    public boolean hasActiveNonDaemonThreads() {
        return threads.hasActiveNonDaemonThreads();
    }

    /**
     * Handle an exception thrown by a tracked thread.
     * 
     * @return true if this exception was handled.
     */
    private boolean trackedThreadUncaughtException(Thread t, Throwable e) {
        getLogger().error("Uncaught exception in thread " + t.getName(), e);
        return true;
    }

    private Logger getLogger() {
        return logger;
    }

    private static class ThreadSets {
        private final Set<Thread> newThreads = new HashSet<Thread>();     // created, not running yet
        private final Set<Thread> runningThreads = new HashSet<Thread>(); // running
        
        synchronized void addNew(Thread t) {
            newThreads.add(t);
        }

        synchronized void removeRunning(Thread t) {
            runningThreads.remove(t);
        }

        synchronized void fromNewToRunning(Thread t) {
            newThreads.remove(t);
            runningThreads.add(t);
        }

        synchronized Thread[] runningArray() {
            return runningThreads.toArray(new Thread[0]);
        }
        
        synchronized boolean hasActiveNonDaemonThreads() {
            if (runningThreads.isEmpty() && newThreads.isEmpty())
                return false;

            for (Thread t : runningThreads) {
                if (t.isDaemon())
                    continue;
                return true;
            }
            for (Thread t : newThreads) {
                if (t.isDaemon())
                    continue;
                return true;
            }
            return false;
        }
    }
}
