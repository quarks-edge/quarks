/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.runtime.etiao.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import quarks.function.Consumer;
import quarks.graph.Edge;
import quarks.graph.spi.AbstractVertex;
import quarks.graph.spi.DirectEdge;
import quarks.oplet.Oplet;
import quarks.runtime.etiao.Invocation;

public class ExecutableVertex<N extends Oplet<C, P>, C, P> extends AbstractVertex<N, C, P> {

    private static final Edge DISCONNECTED = new DirectEdge();
    private DirectGraph graph;
    private final Invocation<N, C, P> invocation;
    private final List<EtiaoConnector<P>> connectors;
    private final List<Edge> edges;
    
    ExecutableVertex(DirectGraph graph, Invocation<N, C, P> invocation) {
        this.graph = graph;
        this.invocation = invocation;
        connectors = new ArrayList<>(invocation.getOutputCount());
        for (int i = 0; i < invocation.getOutputCount(); i++) {
            addConnector(i);
        }
        edges = new ArrayList<>(invocation.getOutputCount());
        for (int i = 0; i < invocation.getOutputCount(); i++) {
            edges.add(DISCONNECTED);
        }
    }

    private EtiaoConnector<P> addConnector(int index) {
		EtiaoConnector<P> connector = new EtiaoConnector<>(this, index);
        connectors.add(connector);  
        return connector;
    }

    @Override
    public DirectGraph graph() {
        return graph;
    }

    @Override
    public N getInstance() {
        return invocation.getOplet();
    }

    @Override
    public EtiaoConnector<P> addOutput() {
        int outputPort = invocation.addOutput();
        int edgeIndex = addEdge();

        assert outputPort == edgeIndex;

        return addConnector(outputPort);
    }


    @Override
    public List<EtiaoConnector<P>> getConnectors() {
        return Collections.unmodifiableList(connectors);
    }

    public String getInvocationId() {
        return invocation.getId();
    }

	void disconnect(int sourcePort) {
		invocation.disconnect(sourcePort);
		edges.set(sourcePort, DISCONNECTED);
	}

	/**
	 * Connect this Vertex's source port to a target Vertex input port
	 * using the given edge.
	 * 
	 * @param sourcePort
	 * @param target
	 * @param edge
	 */
    void connect(int sourcePort, Target<P> target, Edge edge) {
        if (edge == null)
            throw new NullPointerException();
        Consumer<P> input = target.vertex.invocation.getInputs().get(target.port);
        invocation.setTarget(sourcePort, input);
        edges.set(sourcePort, edge);
    }

    int addEdge() {
        int index = edges.size();
        edges.add(DISCONNECTED);
        return index;
    }

    List<Edge> getEdges() {
        List<Edge> connectedEdges = new ArrayList<>();
        for (Edge de : edges) {
            if (de != DISCONNECTED)
                connectedEdges.add(de);
        }
        return Collections.unmodifiableList(connectedEdges);
    }
}
