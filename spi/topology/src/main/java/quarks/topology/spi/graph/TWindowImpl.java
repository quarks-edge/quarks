/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology.spi.graph;

import java.util.LinkedList;
import java.util.List;

import quarks.function.BiFunction;
import quarks.function.Function;
import quarks.function.Functions;
import quarks.oplet.window.Aggregate;
import quarks.topology.TStream;
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
}
