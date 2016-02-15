/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.runtime.etiao.graph.model;

import quarks.graph.Vertex;
import quarks.oplet.Oplet;
import quarks.runtime.etiao.graph.ExecutableVertex;

/**
 * A {@code VertexType} in a graph.
 * <p>
 * A {@code VertexType} has an {@link InvocationType} instance.
 * 
 * @param <I> Data type the oplet consumes on its input ports.
 * @param <O> Data type the oplet produces on its output ports.
 */
public class VertexType<I, O> {

    /**
     * Vertex identifier, unique within the {@code GraphType} this vertex 
     * belongs to.
     */
    private final String id;

    /**
     * The oplet invocation that is being executed.
     */
    private final InvocationType<I, O> invocation;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public VertexType(Vertex<? extends Oplet<?, ?>, ?, ?> value, IdMapper<String> ids) {
        this.id = (value instanceof ExecutableVertex) ?
            ids.add(value, ((ExecutableVertex) value).getInvocationId()) :
            // Can't get an id from the vertex, generate unique value
            ids.add(value);
        this.invocation = new InvocationType(value.getInstance());
    }

    public VertexType() {
        this.id = null;
        this.invocation = null;
    }

    public String getId() {
        return id;
    }

    public InvocationType<I, O> getInvocation() {
        return invocation;
    }
}
