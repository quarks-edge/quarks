/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.graph.spi;

import quarks.graph.Vertex;
import quarks.oplet.Oplet;

/**
 * Placeholder for a skeletal implementation of the {@link Vertex} interface,
 * to minimize the effort required to implement the interface.
 *
 * @param <OP>
 *            Oplet type associated with this vertex.
 * @param <I>
 *            Data container type for input tuples.
 * @param <O>
 *            Data container type for output tuples.
 */
public abstract class AbstractVertex<OP extends Oplet<I, O>, I, O> implements Vertex<OP, I, O> {
}
