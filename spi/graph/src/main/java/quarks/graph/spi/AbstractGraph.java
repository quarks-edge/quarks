/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.graph.spi;

import java.util.ArrayList;
import java.util.List;

import quarks.function.Predicate;
import quarks.function.Supplier;
import quarks.graph.Connector;
import quarks.graph.Edge;
import quarks.graph.Graph;
import quarks.graph.Vertex;
import quarks.oplet.Oplet;
import quarks.oplet.core.Peek;
import quarks.oplet.core.Source;

/**
 * Placeholder for a skeletal implementation of the {@link Graph} interface,
 * to minimize the effort required to implement the interface.
 */
public abstract class AbstractGraph<G> implements Graph {
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
    @Override
    public <N extends Source<P>, P> Connector<P> source(N oplet) {
        return insert(oplet, 0, 1).getConnectors().get(0);
    }

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
    @Override
    public <N extends Oplet<C, P>, C, P> Connector<P> pipe(Connector<C> output, N oplet) {
        Vertex<N, C, P> pipeVertex = insert(oplet, 1, 1);
        output.connect(pipeVertex, 0);

        return pipeVertex.getConnectors().get(0);
    }

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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void peekAll(Supplier<? extends Peek<?>> supplier, Predicate<Vertex<?, ?, ?>> select) {
        // Select vertices which satisfy the specified predicate
        List<Vertex<?, ?, ?>> vertices = new ArrayList<>();
        for (Vertex<?, ?, ?> v : getVertices()) {
            if (select.test(v)) {
                vertices.add(v);
            }
        }
        // Insert metric oplets
        for (Vertex<?, ?, ?> v : vertices) {
            List<? extends Connector<?>> connectors = v.getConnectors();
            for (Connector<?> c : connectors) {
                if (c.isConnected()) {
                    Peek<?> oplet = supplier.get();
                    c.peek((Peek) oplet);
                }
            }
        }
    }
}
