/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.window;

import java.io.Serializable;
import java.util.List;

/**
 * A partition within a {@code Window}.
 *
 * @param <T> Type of tuples in the partition.
 * @param <K> Type of the partition's key.
 * @param <L> Type of the list holding the partition's tuples.
 * 
 * @see Window
 */
public interface Partition<T, K, L extends List<T>> extends Serializable{    
    /**
     * Offers a tuple to be inserted into the partition.
     * @param tuple Tuple to be offered.
     * @return True if the tuple was inserted into this partition, false if it was rejected.
     */
    boolean insert(T tuple);
        
    /**
     * Invoke the WindowProcessor's processWindow method. A partition processor
     * must be registered prior to invoking process().
     */
    void process();
    
    /** 
     * Calls the partition's evictDeterminer.
     */
    void evict();
    
    /**
     * Retrieves the window contents.
     * @return list of partition contents
     */
    L getContents();
    
    /**
     * Return the window in which this partition is contained.
     * @return the partition's window
     */
    Window<T, K, L> getWindow();
    
    /**
     * Returns the key associated with this partition
     * @return The key of the partition.
     */
    K getKey();
}
