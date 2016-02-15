/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.window;

import java.util.HashMap;
import java.util.Map;

import quarks.function.Supplier;

/**
 * Maintain partitioned state.
 * Abstract class that can be used to maintain state 
 * for each keyed partition in a {@link Window}.
 *
 * @param <K> Key type.
 * @param <S> State type.
 */
public abstract class PartitionedState<K, S> {

    private final Supplier<S> initialState;
    private final Map<K, S> states = new HashMap<>();

    /**
     * Construct with an initial state function.
     * @param initialState Function used to create the initial state for a key.
     * 
     * @see #getState(Object)
     */
    protected PartitionedState(Supplier<S> initialState) {
        this.initialState = initialState;
    }

    /**
     * Get the current state for {@code key}.
     * If no state is held then {@code initialState.get()}
     * is called to create the initial state for {@code key}.
     * @param key Partition key.
     * @return State for {@code key}.
     */
    protected synchronized S getState(K key) {
        S state = states.get(key);
        if (state == null)
            states.put(key, state = initialState.get());
        return state;
    }
    
    /**
     * Set the current state for {@code key}.
     * @param key Partition key.
     * @param state State for {@code key}
     * @return Previous state for {@code key}, will be null if no state was held.
     */
    protected synchronized S setState(K key, S state) {
        return states.put(key, state);
    }
    /**
     * 
     * @param key Partition key.
     * @return Removed state for {@code key}, will be null if no state was held.
     */
    protected synchronized S removeState(K key) {
        return states.remove(key);
    }
}
