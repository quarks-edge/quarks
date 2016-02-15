/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.oplet.plumbing;

import java.util.concurrent.ScheduledExecutorService;

import quarks.function.Functions;
import quarks.oplet.OpletContext;
import quarks.oplet.core.Pipe;

/**
 * Isolate upstream processing from downstream
 * processing without guaranteeing tuple order.
 * An executor is used for downstream processing
 * thus tuple order cannot be guaranteed as the
 * scheduler does not guarantee execution order.
 *
 * @param <T> Type of the tuple.
 */
public class UnorderedIsolate<T> extends Pipe<T,T> {
    private static final long serialVersionUID = 1L;
    
    private ScheduledExecutorService executor;
    
    @Override
    public void initialize(OpletContext<T, T> context) {
        super.initialize(context);
        executor = context.getService(ScheduledExecutorService.class);
    }

    @Override
    public void accept(T tuple) {
        executor.execute(Functions.delayedConsume(getDestination(), tuple));      
    }
    
    @Override
    public void close() throws Exception {
    }
}
