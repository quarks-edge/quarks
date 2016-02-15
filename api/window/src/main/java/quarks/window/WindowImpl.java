/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import quarks.function.BiConsumer;
import quarks.function.BiFunction;
import quarks.function.Consumer;
import quarks.function.Function;
import quarks.function.Supplier;


class WindowImpl<T, K, L extends List<T>> implements Window<T, K, L> {
    private final BiFunction<Partition<T, K, L>, T, Boolean> insertionPolicy;
    private final BiConsumer<Partition<T, K, L>, T> contentsPolicy;
    private final Consumer<Partition<T, K, L> > evictDeterminer;
    private final BiConsumer<Partition<T, K, L>, T> triggerPolicy;
    private BiConsumer<List<T>, K> partitionProcessor;
    
    private ScheduledExecutorService ses;
    
    protected Supplier<L> listSupplier;
    protected Function<T, K> keyFunction;
    
    protected Map<K, Partition<T, K, L> > partitions = new HashMap<K, Partition<T, K, L> >();
    
    
    WindowImpl(BiFunction<Partition<T, K, L>, T, Boolean> insertionPolicy, BiConsumer<Partition<T, K, L>, T> contentsPolicy,
            Consumer<Partition<T, K, L> > evictDeterminer, BiConsumer<Partition<T, K, L>, T> triggerPolicy,
            Function<T, K> keyFunction, Supplier<L> listSupplier){
        this.insertionPolicy = insertionPolicy;
        this.contentsPolicy = contentsPolicy;
        this.evictDeterminer = evictDeterminer;
        this.triggerPolicy = triggerPolicy;
        this.keyFunction = keyFunction;
        this.listSupplier = listSupplier;
    }

    @Override
    public boolean insert(T tuple) {
        K key = keyFunction.apply(tuple);
        Partition<T, K, L> partition;
        
        synchronized (partitions) {
            partition = partitions.get(key);
            if (partition == null) {
                partition = new PartitionImpl<T, K, L>(this, listSupplier.get(), key);
                partitions.put(key, partition);
            }
        }
        
        return partition.insert(tuple);      
    }

   
    @Override
    public synchronized void registerPartitionProcessor(BiConsumer<List<T>, K> partitionProcessor){
            this.partitionProcessor = partitionProcessor;
    }

    @Override
    public BiConsumer<Partition<T, K, L>, T> getContentsPolicy() {
        return contentsPolicy;
    }

    @Override
    public BiConsumer<Partition<T, K, L>, T> getTriggerPolicy() {
        return triggerPolicy;
    }

    @Override
    public synchronized BiConsumer<List<T>, K> getPartitionProcessor() {
            return partitionProcessor;    
    }

    @Override
    public BiFunction<Partition<T, K, L>, T, Boolean> getInsertionPolicy() {
        return insertionPolicy;
    }

    @Override
    public Consumer<Partition<T, K, L> > getEvictDeterminer() {
        return evictDeterminer;
    }

    @Override
    public Function<T, K> getKeyFunction() {
        return keyFunction;
    }

    @Override
    public synchronized void registerScheduledExecutorService(ScheduledExecutorService ses) {
        this.ses = ses;
        
    }

    @Override
    public synchronized ScheduledExecutorService getScheduledExecutorService() {
        return this.ses;
    }

}
