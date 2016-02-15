/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.graph.spi;

import java.util.Set;

import quarks.graph.Connector;
import quarks.graph.Edge;
import quarks.graph.Vertex;

/**
 * This class provides a simple implementation of the {@link Edge} interface in
 * the context of a {@code DirectProvider}.
 */
public class DirectEdge implements Edge {
	
    private final Connector<?> connector;
	private final Vertex<?, ?, ?> source;
	private final int sourcePort;
	
	private final Vertex<?, ?, ?> target;
	private final int targetPort;
	
	public DirectEdge(
			Connector<?> connector,
			Vertex<?, ?, ?> source, int sourcePort,
			Vertex<?, ?, ?> target, int targetPort) {
	    this.connector = connector;
		this.source = source;
		this.sourcePort = sourcePort;
		this.target = target;
		this.targetPort = targetPort;
	}

	/**
	 * Create disconnected edge.
	 */
	public DirectEdge() {
        this.connector = null;
        this.source = null;
        this.sourcePort = 0;
        this.target = null;
        this.targetPort = 0;
    }

    @Override
	public Vertex<?, ?, ?> getSource() {
		return source;
	}

	@Override
	public int getSourceOutputPort() {
		return sourcePort;
	}

	@Override
	public Vertex<?, ?, ?> getTarget() {
		return target;
	}

	@Override
	public int getTargetInputPort() {
		return targetPort;
	}

    @Override
    public Set<String> getTags() {
        return connector.getTags();
    }
}
