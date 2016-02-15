/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.runtime.etiao;

import quarks.function.Consumer;
import quarks.function.Functions;

/**
 * A forwarding Streamer whose destination
 * can be changed.
 * External synchronization or happens-before
 * guarantees must be provided by the object
 * owning an instance of {@code SettableForwarder}.
 *
 * @param <T> Type of data on the stream.
 */
public final class SettableForwarder<T> implements Consumer<T> {
    private static final long serialVersionUID = 1L;
    private Consumer<T> destination;

    /**
     * Create with the destination set to {@link Functions#discard()}.
     */
    public SettableForwarder() {
        this.destination = Functions.discard();
    }

    /**
     * Create with the specified destination.
     * @param destination Stream destination.
     */
    public SettableForwarder(Consumer<T> destination) {
        this.destination = destination;
    }
    
    @Override
    public void accept(T item) {
        getDestination().accept(item);
    }

    /**
     * Change the destination.
     * No synchronization is taken.
     * @param destination Stream destination.
     */
    public void setDestination(Consumer<T> destination) {
        this.destination = destination;
    }

    /**
     * Get the current destination.
     * No synchronization is taken.
     */
    public final Consumer<T> getDestination() {
        return destination;
    }
}
