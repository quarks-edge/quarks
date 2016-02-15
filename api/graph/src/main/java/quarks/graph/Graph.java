/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.graph;

import java.util.Collection;

import quarks.function.Predicate;
import quarks.function.Supplier;
import quarks.oplet.Oplet;
import quarks.oplet.core.Peek;
import quarks.oplet.core.Source;

/**
 * A generic directed graph of vertices, connectors and edges.
 * <p>
 * The graph consists of {@link Vertex} objects, each having
 * 0 or more input and/or output {@link Connector} objects.
 * {@link Edge} objects connect an output connector to
 * an input connector.
 * <p>
 * A vertex has an associated {@link Oplet} instance that will be executed at runtime..
 * <p>
 * 
 */
public interface Graph {

    /**
     * Add a new unconnected {@code Vertex} into the graph.
     * <p>
     * 
     * @param oplet the oplet to associate with the new vertex
     * @param inputs the number of input connectors for the new vertex
     * @param outputs the number of output connectors for the new vertex
     * @return the newly created {@code Vertex} for the oplet
     */
    <N extends Oplet<C, P>, C, P> Vertex<N, C, P> insert(N oplet, int inputs, int outputs);

    /**
     * Create a new unconnected {@link Vertex} associated with the
     * specified source {@link Oplet}.
     * 
     * 
     * <p>
     * The {@code Vertex} for the oplet has 0 input connectors and one output connector.
     * @param oplet the source oplet
     * @return the output connector for the newly created vertex.
     */
    <N extends Source<P>, P> Connector<P> source(N oplet);

    /**
     * Create a new connected {@link Vertex} associated with the
     * specified {@link Oplet}.
     * <p>
     * The new {@code Vertex} has one input and one output {@code Connector}.
     * An {@link Edge} is created connecting the specified output connector to
     * the new vertice's input connector.
     * 
     * @param output
     * @param oplet the oplet to associate with the new {@code Vertex}
     * @return the output connector for the new {@code Vertex}
     */
	<N extends Oplet<C, P>, C, P> Connector<P> pipe(Connector<C> output, N oplet);

    /**
     * Insert Peek oplets returned by the specified {@code Supplier} into 
     * the outputs of all of the oplets which satisfy the specified 
     * {@code Predicate}.
     * 
     * @param supplier
     *            Function which provides a Peek oplet to insert
     * @param select
     *            Predicate to determine determines whether a Peek oplet will
     *            be inserted on the outputs of the vertex passed as parameter
     */
    void peekAll(Supplier<? extends Peek<?>> supplier, Predicate<Vertex<?, ?, ?>> select);

    /**
     * Return an unmodifiable view of all vertices in this graph.
     * 
     * @return unmodifiable view of all vertices in this graph
     */
    Collection<Vertex<? extends Oplet<?, ?>, ?, ?>> getVertices();
    
    /**
     * Return an unmodifiable view of all edges in this graph.
     */
    Collection<Edge> getEdges();
}

