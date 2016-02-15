/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.oplet.plumbing;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import quarks.oplet.OpletContext;
import quarks.oplet.core.Pipe;

/**
 * Isolate upstream processing from downstream
 * processing guaranteeing tuple order.
 * Input tuples are placed at the tail of a queue
 * and dedicated thread removes them from the
 * head and is used for downstream processing.
 *
 * @param <T> Type of the tuple.
 */
public class Isolate<T> extends Pipe<T,T> implements Runnable {
    private static final long serialVersionUID = 1L;
    
    private Thread thread;
    private LinkedBlockingQueue<T> tuples = new LinkedBlockingQueue<>();
    
    @Override
    public void initialize(OpletContext<T, T> context) {
        super.initialize(context);
        thread = context.getService(ThreadFactory.class).newThread(this);
    }
   
    @Override
    public void start() {
        super.start();
        thread.start();
    }

    @Override
    public void accept(T tuple) {
        try {
            tuples.put(tuple);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }      
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                submit(tuples.take());
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    @Override
    public void close() throws Exception {
    }
    
}
