/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.window;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import quarks.function.BiConsumer;
import quarks.function.BiFunction;
import quarks.function.Consumer;
import quarks.function.Supplier;

/**
 * Common window policies.
 *
 */
public class Policies {
    
    /**
     * A policy which schedules a future partition eviction if the partition is empty.
     * This can be used as a contents policy that is scheduling the eviction of
     * the tuple just about to be inserted.
     * @param time The time span in which tuple are permitted in the partition.
     * @param unit The units of time.
     * @return The time-based contents policy.
     */
    public static <T, K, L extends List<T>> BiConsumer<Partition<T, K, L>, T> scheduleEvictIfEmpty(long time, TimeUnit unit){
        return (partition, tuple) -> {          
            if(partition.getContents().isEmpty()){
                ScheduledExecutorService ses = partition.getWindow().getScheduledExecutorService();
                ses.schedule(() -> partition.evict(), time, unit);
            }
        };
    }
    
    /**
     * A policy which schedules a future partition eviction on the first insert.
     * This can be used as a contents policy that schedules the eviction of tuples
     * as a batch.
     * @param time The time span in which tuple are permitted in the partition.
     * @param unit The units of time.
     * @return The time-based contents policy.
     */
    @SuppressWarnings("serial")
    public static <T, K, L extends List<T>> BiConsumer<Partition<T, K, L>, T> scheduleEvictOnFirstInsert(long time, TimeUnit unit){
        
        // Can't use lambda since state is required
        return new BiConsumer<Partition<T,K,L>, T>() {
            boolean first = true;
            @Override
            public void accept(Partition<T, K, L> partition, T tuple) {
                if(first){
                    first = false;
                    ScheduledExecutorService ses = partition.getWindow().getScheduledExecutorService();
                    ses.schedule(() -> partition.evict(), time, unit);
                }    
            }
        };
    }
    
    /**
     * An eviction policy which evicts all tuples that are older than a specified time.
     * If any tuples remain in the partition, it schedules their eviction after
     * an appropriate interval.
     * @param time The timespan in which tuple are permitted in the partition.
     * @param unit The units of time.
     * @return The time-based eviction policy.
     */ 
    public static <T, K> Consumer<Partition<T, K, InsertionTimeList<T>> > evictOlderWithProcess(long time, TimeUnit unit){
        
        long timeMs = TimeUnit.MILLISECONDS.convert(time, unit);

        return (partition) -> {
            ScheduledExecutorService ses = partition.getWindow().getScheduledExecutorService();
            InsertionTimeList<T> tuples = partition.getContents();
            long evictTime = System.currentTimeMillis() - timeMs;
            
            tuples.evictOlderThan(evictTime);

            partition.process();
            
            if(!tuples.isEmpty()){
                ses.schedule(() -> partition.evict(), tuples.nextEvictDelay(timeMs), TimeUnit.MILLISECONDS);
            }
        };
    }
    
    /**
     * An eviction policy which evicts all tuples, and schedules the next eviction
     * after the appropriate interval.
     * @param time The timespan in which tuple are permitted in the partition.
     * @param unit The units of time.
     * @return The time-based eviction policy.
     */ 
    public static <T, K> Consumer<Partition<T, K, List<T>> > evictAllAndScheduleEvict(long time, TimeUnit unit){
        
        long timeMs = TimeUnit.MILLISECONDS.convert(time, unit);
        return (partition) -> {
            ScheduledExecutorService ses = partition.getWindow().getScheduledExecutorService();
            List<T> tuples = partition.getContents(); 

            partition.process();
            tuples.clear();
                        
            ses.schedule(() -> partition.evict(), timeMs, TimeUnit.MILLISECONDS);       
        };
    }
    
    
    /**
     * Returns an insertion policy that indicates the tuple
     * is to be inserted into the partition.
     * 
     * @param <T> Tuple type
     * @param <K> Key type
     * @param <L> List type for the partition contents.
     * 
     * @return An insertion policy that always inserts.
     */
    public static <T, K, L extends List<T>> BiFunction<Partition<T, K, L>, T, Boolean> alwaysInsert(){
        return (partition, tuple) -> true;
    }
    
    /**
     * Returns a count-based contents policy.
     * If, when called, the number of tuples in the partition is
     * greater than equal to {@code count} then {@code partition.evict()}
     * is called.
     * @return A count-based contents policy.
     */
    public static <T, K, L extends List<T>> BiConsumer<Partition<T, K, L>, T> countContentsPolicy(final int count){
        return (partition, tuple) -> {
            if (partition.getContents().size() >= count)
                partition.evict();
        };
    }
    
    /**
     * Returns a Consumer representing an evict determiner that evict all tuples
     * from the window.
     * @return An evict determiner that evicts all tuples.
     */
    public static <T, K, L extends List<T>> Consumer<Partition<T, K, L> > evictAll(){
        return partition -> partition.getContents().clear();
    }
    
    /**
     * Returns an evict determiner that evicts the oldest tuple.
     * @return A evict determiner that evicts the oldest tuple.
     */
    public static <T, K, L extends List<T>> Consumer<Partition<T, K, L> > evictOldest(){
        return partition -> partition.getContents().remove(0);
    }
    
    /**
     * Returns a trigger policy that triggers
     * processing on every insert.
     * @return A trigger policy that triggers processing on every insert.
     */ 
    public static <T, K, L extends List<T>> BiConsumer<Partition<T, K, L>, T> processOnInsert(){
        return (partition, tuple) -> partition.process();
    }
    
    /**
     * Returns a trigger policy that triggers when the size of a partition
     * equals or exceeds a value.
     * @return A trigger policy that triggers processing when the size of 
     * the partition equals or exceets a value.
     */ 
    public static <T, K, L extends List<T>> BiConsumer<Partition<T, K, L>, T> processWhenFull(final int size){
        return (partition, tuple) -> {
            if(partition.getContents().size() >= size){
                partition.process();
            }
        };
    }
    
    /**
     * A {@link BiConsumer} policy which does nothing.
     * @return A policy which does nothing.
     */
    public static <T, K, L extends List<T>> BiConsumer<Partition<T, K, L>, T> doNothing(){
        return (partition, key) -> {};
    }
    
    public static <T> Supplier<InsertionTimeList<T>> insertionTimeList() {
        return () -> new InsertionTimeList<>();
    }
}
