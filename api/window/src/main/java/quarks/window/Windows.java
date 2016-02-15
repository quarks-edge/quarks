/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.window;

import static quarks.window.Policies.alwaysInsert;
import static quarks.window.Policies.countContentsPolicy;
import static quarks.window.Policies.evictOldest;
import static quarks.window.Policies.processOnInsert;

import java.util.LinkedList;
import java.util.List;

import quarks.function.BiConsumer;
import quarks.function.BiFunction;
import quarks.function.Consumer;
import quarks.function.Function;
import quarks.function.Supplier;

/**
 * Factory to create {@code Window} implementations.
 *
 */
public class Windows {  
    
    /**
     * Create a window using the passed in policies.
     * 
     * @param insertionPolicy Policy indicating if a tuple should be inserted
     * into the window.
     * @param contentsPolicy Contents policy called prior to insertion of a tuple.
     * @param evictDeterminer Policy that determines action to take when
     * {@link Partition#evict()} is called.
     * @param triggerPolicy Trigger policy that is invoked after the insertion
     * of a tuple into a partition.
     * @param keyFunction Function that gets the partition key from a tuple.
     * @param listSupplier Supplier function for the {@code List} that holds
     * tuples within a partition.
     * @return Window using the passed in policies.
     */
    public static  <T, K, L extends List<T>> Window<T, K, L> window(
            BiFunction<Partition<T, K, L>, T, Boolean> insertionPolicy,
            BiConsumer<Partition<T, K, L>, T> contentsPolicy,
            Consumer<Partition<T, K, L> > evictDeterminer,
            BiConsumer<Partition<T, K, L>, T> triggerPolicy,
            Function<T, K> keyFunction,
            Supplier<L> listSupplier){
        
        return new WindowImpl<>(insertionPolicy, contentsPolicy, evictDeterminer, triggerPolicy, keyFunction, listSupplier);
    }
    
    /**
     * Return a window that maintains the last {@code count} tuples inserted
     * with processing triggered on every insert. This provides 
     * a continuous processing, where processing is invoked every
     * time the window changes. Since insertion drives eviction
     * there is no need to process on eviction, thus once the window
     * has reached {@code count} tuples, each insertion results in an
     * eviction followed by processing of {@code count} tuples
     * including the tuple just inserted, which is the definition of
     * the window.
     * 
     * @param <T> Tuple type.
     * @param <K> Key type.
     * 
     * @param count Number of tuple to maintain per partition
     * @param keyFunction Tuple partitioning key function
     * @return window that maintains the last {@code count} tuples on a stream
     */
    public static <T, K> Window<T, K, LinkedList<T>> lastNProcessOnInsert(final int count,
            Function<T, K> keyFunction) {

        Window<T, K, LinkedList<T>> window = Windows.window(
                alwaysInsert(),
                countContentsPolicy(count), 
                evictOldest(), 
                processOnInsert(), 
                keyFunction, 
                () -> new LinkedList<T>());

        return window;
    }
    
}
