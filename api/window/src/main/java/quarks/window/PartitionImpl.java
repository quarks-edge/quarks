/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.window;

import java.util.Collections;
import java.util.List;

import quarks.function.Consumer;

@SuppressWarnings("serial")
class PartitionImpl<T, K, L extends List<T>> implements Partition<T, K, L> {
    private final L tuples;
    private final List<T> unmodifiableTuples;
    private final Window<T, K, L> window;
    private final K key;
    
    PartitionImpl(Window<T, K, L> window, L tuples, K key){
        this.window = window;
        this.tuples = tuples;
        this.unmodifiableTuples = Collections.unmodifiableList(tuples);
        this.key = key;
    }

    @Override
    public synchronized boolean insert(T tuple) {        
        
        if (getWindow().getInsertionPolicy().apply(this, tuple)) {
            getWindow().getContentsPolicy().accept(this, tuple);
            this.tuples.add(tuple);
            // Trigger
            getWindow().getTriggerPolicy().accept(this, tuple);
            return true;
        }

        return true;
    }
    
    @Override
    public synchronized void process() {
        window.getPartitionProcessor().accept(unmodifiableTuples, key);
    }

    @Override
    public synchronized L getContents() {
        return tuples;
    }

    @Override
    public Window<T, K, L> getWindow() {
        return window;
    }
    
    @Override
    public K getKey() {
        return key;
    }

    @Override
    public synchronized void evict() {
        Consumer<Partition<T, K, L>> evictDeterminer = window.getEvictDeterminer();
        evictDeterminer.accept(this);
    }
}
