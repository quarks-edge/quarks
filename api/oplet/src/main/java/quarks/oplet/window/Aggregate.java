/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.window;

import static quarks.function.Functions.closeFunction;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import quarks.function.BiConsumer;
import quarks.function.BiFunction;
import quarks.oplet.OpletContext;
import quarks.oplet.core.Pipe;
import quarks.window.Window;

/**
 * Aggregate a window.
 * Window contents are aggregated by a
 * {@link BiFunction aggregator function}
 * passing the list of tuples in the window and
 * the partition key. The returned value
 * is submitted to the sole output port
 * if it is not {@code null}. 
 *
 * @param <T> Type of the input tuples.
 * @param <U> Type of the output tuples.
 * @param <K> Type of the partition key.
 */
public class Aggregate<T,U,K> extends Pipe<T, U> {
    private static final long serialVersionUID = 1L;
    private final Window<T,K, ? extends List<T>> window;
    /**
     * The aggregator provided by the user.
     */
    private final BiFunction<List<T>,K, U> aggregator;
    
    public Aggregate(Window<T,K, ? extends List<T>> window, BiFunction<List<T>,K, U> aggregator){
        this.aggregator = aggregator;
        BiConsumer<List<T>, K> partProcessor = (tuples, key) -> {
            U aggregateTuple = aggregator.apply(tuples, key);
            if (aggregateTuple != null)
                submit(aggregateTuple);
            };
            
        window.registerPartitionProcessor(partProcessor);
        this.window=window;
    }
    
    @Override
    public void initialize(OpletContext<T,U> context) {
        super.initialize(context);
        window.registerScheduledExecutorService(this.getOpletContext().getService(ScheduledExecutorService.class));
    }
    
    @Override
    public void accept(T tuple) {
        window.insert(tuple);   
    }

    @Override
    public void close() throws Exception {
        closeFunction(aggregator);
    }

}
