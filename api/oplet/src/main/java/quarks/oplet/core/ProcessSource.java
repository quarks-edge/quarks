/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
*/
package quarks.oplet.core;

import java.util.concurrent.ThreadFactory;

public abstract class ProcessSource<T> extends Source<T>implements Runnable {

    @Override
    public void start() {
        Thread t = getOpletContext().getService(ThreadFactory.class).newThread(this);
        t.setDaemon(false);
        t.start();
    }

    protected Runnable getRunnable() {
        return this;
    }

    protected abstract void process() throws Exception;

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
