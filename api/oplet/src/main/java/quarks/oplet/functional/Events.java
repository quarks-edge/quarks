/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.functional;


import static quarks.function.Functions.closeFunction;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import quarks.function.Consumer;
import quarks.oplet.core.Source;

/**
 * Generate tuples from events.
 * This oplet implements {@link Consumer} which
 * can be called directly from an event handler,
 * listener or callback. 
 * 
 * @param <T> Data container type for output tuples.
 */
public class Events<T> extends Source<T>implements Consumer<T> {

    private static final long serialVersionUID = 1L;
    private Consumer<Consumer<T>> eventSetup;

    public Events(Consumer<Consumer<T>> eventSetup) {
        this.eventSetup = eventSetup;
    }

    @Override
    public void close() throws Exception {
        closeFunction(eventSetup);
    }

    @Override
    public void start() {
        // TODO figure out what we really want to do...
        // For now, allocate a thread so the job containing this oplet
        // doesn't look "complete" and shutdown.
        //
        // We could do one or more of the following:
        // - provide the Consumer<T> with a way to access this oplet's
        //   thread factory so it can allocate a thread from it
        //   (e.g., the JAA Initializable and FunctionContext)
        // - allocate a thread here and make it available to the fn
        // - allocate a consumer thread and buffer and where received
        //   tuples are queued and then send downstream

        Thread endlessEventSource = getOpletContext()
                .getService(ThreadFactory.class)
                .newThread(() -> {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    }
                    catch (InterruptedException e) {
                        // cancelled; we're done.
                    }
                });
        getOpletContext().getService(ScheduledExecutorService.class)
                    .submit(endlessEventSource);
        
        eventSetup.accept(this);
    }

    @Override
    public void accept(T tuple) {
        submit(tuple);
    }
}
