/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.graph;

import java.util.Set;

import quarks.oplet.core.Peek;

/**
 * A {@code Connector} represents an output port of a {@code Vertex}.
 * 
 * A {@code Connector} supports two methods to add processing for tuples
 * submitted to the port:
 * <UL>
 * <LI>{@link #connect(Vertex, int)} : Connect this to an input port of another
 * {@code Vertex}. Any number of connections can be made. Any tuple submitted by
 * the output port will appear on all connections made through this method. For
 * any tuple {@code t} ordering of appearance across the connected input ports
 * is not guaranteed.</LI>
 * <LI>{@link #peek(Peek)} : Insert a peek after the output port and before any
 * connections made by {@link #connect(Vertex, int)}. Multiple peeks can be
 * inserted. A tuple {@code t} submitted by the output port will be seen by all
 * peek oplets. The ordering of the peek is guaranteed such that the peeks
 * are processed in the order they were added to this {@code Connector} with the
 * {@code t} being seen first by the first peek added.
 * <LI>
 * </UL>
 * For example with peeks {@code P1,P2,P3} added in that order and connections
 * {@code C1,C2} added, the graph will be logically:
 * 
 * <pre>
 * <code>
 *                      -->C1
 * port-->P1-->P2-->P3--|
 *                      -->C2
 * </code>
 * </pre>
 * 
 * A tuple {@code t} submitted by the port will be peeked at by {@code P1}, then
 * {@code P2} then {@code P3}. After {@code P3} peeked at the tuple, {@code C1}
 * and {@code C2} will process the tuple in an arbitrary order.
 * 
 * @param <T>
 *            Type of the data item produced by the output port
 */
public interface Connector<T> {
	
	/**
	 * Gets the {@code Graph} for this {@code Connector}.
	 * 
	 * @return the {@code Graph} for this {@code Connector}.
	 */
    Graph graph();

    /**
     * Connect this {@code Connector} to the specified target's input. This
     * method may be called multiple times to fan out to multiple input ports.
     * Each tuple submitted to this output port will be processed by all
     * connections.
     * 
     * @param target
     *            the {@code Vertex} to connect to
     * @param inputPort
     *            the index of the target's input port to connect to.
     */
    void connect(Vertex<?, T, ?> target, int inputPort);
    
	/**
	 * Is my output port connected to any input port.
	 * 
	 * @return true if connected
	 */
    boolean isConnected();

	/**
     * Inserts a {@code Peek} oplet between an output port and its
     * connections. This method may be called multiple times to insert multiple
     * peeks. Each tuple submitted to this output port will be seen by all peeks
     * in order of their insertion, starting with the first peek inserted.
     * 
     * @param oplet
     *            Oplet to insert.
     * @return {@code output}
     */
	<N extends Peek<T>> Connector<T> peek(N oplet);

    /**
     * Adds the specified tags to the connector.  Adding the same tag 
     * multiple times will not change the result beyond the initial 
     * application. An unconnected connector can be tagged.
     * 
     * @param values
     *            Tag values.
     */
    void tag(String... values);

    /**
     * Returns the set of tags associated with this connector.
     * 
     * @return set of tag values.
     */
    Set<String> getTags(); 
}
