/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology.spi.graph;

import static quarks.window.Policies.alwaysInsert;
import static quarks.window.Policies.processOnInsert;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import quarks.function.BiFunction;
import quarks.function.Function;
import quarks.function.Functions;
import quarks.oplet.window.Aggregate;
import quarks.topology.TStream;
import quarks.window.Policies;
import quarks.window.Window;
import quarks.window.Windows;

public class TWindowImpl<T, K> extends AbstractTWindow<T, K> {
    private final int size;
    
    TWindowImpl(int size, TStream<T> feed, Function<T, K> keyFunction){
        super(feed, keyFunction);
        this.size = size;
    }

    @Override
    public <U> TStream<U> aggregate(BiFunction<List<T>,K, U> processor) { 
        processor = Functions.synchronizedBiFunction(processor);
        Window<T, K, LinkedList<T>> window = Windows.lastNProcessOnInsert(size, getKeyFunction());
        Aggregate<T,U,K> op = new Aggregate<T,U,K>(window, processor);
        return feeder().pipe(op); 
    }

    @Override
    public <U> TStream<U> batch(BiFunction<List<T>, K, U> batcher) {
        batcher = Functions.synchronizedBiFunction(batcher);
        Window<T, K, List<T>> window =
                Windows.window(
                        alwaysInsert(),
                        Policies.countContentsPolicy(size),
                        Policies.evictAll(),
                        Policies.processWhenFull(size),
                        getKeyFunction(),
                        () -> new ArrayList<T>());
        
        Aggregate<T,U,K> op = new Aggregate<T,U,K>(window, batcher);
        return feeder().pipe(op); 
    }
}
