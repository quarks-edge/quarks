/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.graph;

import java.util.List;

import quarks.oplet.Oplet;

/**
 * A {@code Vertex} in a graph.
 * <p>
 * A {@code Vertex} has an {@link Oplet} instance
 * that will be executed at runtime and zero or
 * more input ports and zero or more output ports.
 * Each output port is represented by a {@link Connector} instance.
 * 
 * @param <N> the type of the {@code Oplet}
 * @param <C> Data type the oplet consumes in its input ports.
 * @param <P> Data type the oplet produces on its output ports.
 */
public interface Vertex<N extends Oplet<C, P>, C, P> {

	/**
	 * Get the vertice's {@link Graph}.
	 * @return the graph
	 */
    Graph graph();

    /**
     * Get the instance of the oplet that will be executed.
     * 
     * @return the oplet
     */
    N getInstance();

    /**
     * Get the vertice's collection of output connectors.
     * @return an immutable collection of the output connectors.
     */
    List<? extends Connector<P>> getConnectors();

    /**
     * Add an output port to the vertex.
     * @return {@code Connector} representing the output port.
     */
    Connector<P> addOutput();
}
