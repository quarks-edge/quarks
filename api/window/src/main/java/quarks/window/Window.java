/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.window;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import quarks.function.BiConsumer;
import quarks.function.BiFunction;
import quarks.function.Consumer;
import quarks.function.Function;


/**
 * Partitioned window of tuples.
 * Conceptually a window maintains a continuously
 * changing subset of tuples on a stream, such as the last ten tuples
 * or tuples that have arrived in the last five minutes.
 * <P>
 * {@code Window} is partitioned by keys obtained
 * from tuples using a key function. Each tuple is
 * inserted into a partition containing all tuples
 * with the same key (using {@code equals()}).
 * Each partition independently maintains the subset of
 * tuples defined by the windows policies.
 * <BR>
 * An unpartitioned is created by using a key function that
 * returns a constant, to force all tuples to be inserted
 * into a single partition. A convenience function
 * {@link quarks.function.Functions#unpartitioned() unpartitioned()} is
 * provided that returns zero as the fixed key.
 * </P>   
 * <P>
 * The window's policies are flexible to allow any definition of
 * what tuples each partition will contain, and how the
 * partition is processed.
 * </P>
 * @param <T> type of tuples in the window
 * @param <K> type of the window's key
 * @param <L> type of the list used to contain tuples.
 */
public interface Window<T, K, L extends List<T>>{
    
    /**
     * Attempts to insert the tuple into its partition.
     * Tuple insertion performs the following actions in order:
     * <OL>
     * <LI>Call {@code K key = getKeyFunction().apply(tuple)} to obtain the partition key.</LI>
     * <LI>Get the partition for {@code key} creating an empty partition if one did not exist for {@code key}. </LI>
     * <LI>Apply the insertion policy, calling {@code getInsertionPolicy().apply(partition, tuple)}. If it returns false then return false from this method,
     * otherwise continue.</LI>
     * <LI>Apply the contents policy, calling {@code getContentsPolicy().apply(partition, tuple)}.
     * This is a pre-insertion action that allows any action. Some policies may request room to be made for
     * the insertion by calling {@link Partition#evict() evict()} which will result in a call to the evict determiner.</LI>
     * <LI>Add {@code tuple} to the contents of the partition.</LI>
     * <LI>Apply the trigger policy, calling {@code getTriggerPolicy().apply(partition, tuple)}.
     * This is a post-insertion that action allows any action. A typical implementation is to call
     * {@link Partition#process() partition.process()} to perform processing of the window.
     * </OL>
     * 
     * 
     * 
     * @param tuple
     * @return true, if the tuple was successfully inserted. Otherwise, false.
     */
    boolean insert(T tuple);
    
    /**
     * Register a WindowProcessor.
     * @param windowProcessor
     */
    void registerPartitionProcessor(BiConsumer<List<T>, K> windowProcessor);
    
    /**
     * Register a ScheduledExecutorService.
     * @param ses
     */
    void registerScheduledExecutorService(ScheduledExecutorService ses);
    
    /**
     * Returns the insertion policy of the window.
     *  is called
     * 
     * @return The insertion policy.
     */
    BiFunction<Partition<T, K, L>, T, Boolean> getInsertionPolicy();
    
    /**
     * Returns the contents policy of the window.
     * The contents policy is invoked before a tuple
     * is inserted into a partition.
     * 
     * @return contents policy for this window.
     */
    BiConsumer<Partition<T, K, L>, T> getContentsPolicy();

    /**
     * Returns the window's trigger policy.
     * The trigger policy is invoked (triggered) by
     * the insertion of a tuple into a partition.
     * 
     * @return trigger policy for this window.
     */
    BiConsumer<Partition<T, K, L>, T> getTriggerPolicy();

    /**
     * Returns the partition processor associated with the window.
     * @return partitionProcessor
     */
    BiConsumer<List<T>, K> getPartitionProcessor();
    
    /**
     * Returns the ScheduledExecutorService associated with the window.
     * @return ScheduledExecutorService
     */
    ScheduledExecutorService getScheduledExecutorService();
    
    /**
     * Returns the window's eviction determiner.
     * The evict determiner is responsible for
     * determining which tuples in a window need
     * to be evicted.
     * <BR>
     * Calls to {@link Partition#evict()} result in
     * {@code getEvictDeterminer().accept(partition)} being
     * called.
     * In some cases this may not result in tuples being
     * evicted from the partition.
     * <P>
     * An evict determiner evicts tuples from the partition
     * by removing them from the list returned by
     * {@link Partition#getContents()}.
     * 
     * @return evict determiner for this window.
     */
    Consumer<Partition<T, K, L>> getEvictDeterminer();
    
    /**
     * Returns the keyFunction of the window
     * @return The window's keyFunction.
     */
    Function<T, K> getKeyFunction();

}
