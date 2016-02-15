/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.runtime.etiao.graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import quarks.graph.Connector;
import quarks.graph.Edge;
import quarks.graph.Vertex;
import quarks.graph.spi.DirectEdge;
import quarks.oplet.core.FanOut;
import quarks.oplet.core.Peek;

class EtiaoConnector<P> implements Connector<P> {

	/**
	 * The original port for this connector.
	 */
	@SuppressWarnings("unused")
    private final ExecutableVertex<?, ?, P> originalVertex;
	@SuppressWarnings("unused")
    private final int originalPort;

	/**
	 * The active port for this connector. active is different to original when
	 * a peek has been inserted.
	 */
	private ExecutableVertex<?, ?, P> activeVertex;
	private int activePort;

	private Target<P> target;

	/**
	 * Fanout vertex. When the output port is logically connected to multiple
	 * inputs activeVertex will be connected to fanOutVertex and logical inputs
	 * are connected to fanOutVertex.
	 */
    private ExecutableVertex<FanOut<P>, P, P> fanOutVertex;

	public EtiaoConnector(ExecutableVertex<?, ?, P> originalVertex, int originalPort) {
		this.originalVertex = originalVertex;
		this.originalPort = originalPort;

		this.activeVertex = originalVertex;
		this.activePort = originalPort;

    }

    @Override
	public DirectGraph graph() {
        return activeVertex.graph();
    }

    @Override
    public boolean isConnected() {
		return target != null;
    }

    private boolean isFanOut() {
        return fanOutVertex != null;
    }

	Target<P> disconnect() {
		assert isConnected();

		activeVertex.disconnect(activePort);
		Target<P> target = this.target;
		this.target = null;
		assert !isConnected();
		
		return target;
    }

	/**
	 * Take other's connection(s) leaving it disconnected.
	 */
	private void take(EtiaoConnector<P> other) {
		connectDirect(other.disconnect());
    }

	private void connectDirect(Target<P> target) {
		assert !isConnected();
		
        activeVertex.connect(activePort, target, newEdge(target));
		this.target = target;
	}

    @Override
    public void connect(Vertex<?, P, ?> target, int targetPort) {
		if (!isConnected()) {

			connectDirect(new Target<>((ExecutableVertex<?, P, ?>) target, targetPort));
			return;
        }

		if (!isFanOut()) {

            // Insert a FanOut oplet, initially with a single output port
			fanOutVertex = graph().insert(new FanOut<P>(), 1, 1);
            
            // Connect the FanOut's first port to the previous target
            EtiaoConnector<P> fanOutConnector = fanOutVertex.getConnectors().get(0);
            fanOutConnector.take(this);
            
			// Connect this to the FanOut oplet
			assert !isConnected();
            connect(fanOutVertex, 0);
        }

        // Add another output port to the fan out oplet.
        Connector<P> fanOutConnector = fanOutVertex.addOutput();
        fanOutConnector.connect(target, targetPort);
    }

    @Override
    public <N extends Peek<P>> Connector<P> peek(N oplet) {
        ExecutableVertex<N, P, P> peekVertex = graph().insert(oplet, 1, 1);

        // Have the output of the peek take over the connections of the current output.
        EtiaoConnector<P> peekConnector = peekVertex.getConnectors().get(0);

        if (isConnected())
            peekConnector.take(this);
        
        Target<P> target = new Target<P>(peekVertex, 0);
        activeVertex.connect(activePort, target, newEdge(target));

        activeVertex = peekVertex;
        activePort = 0;

        return this;
    }

	private Edge newEdge(Target<?> target) {
	    return new DirectEdge(this, activeVertex, activePort, target.vertex, target.port);
	}

    private Set<String> tags = new HashSet<>();

    @Override
    public void tag(String... values) {
        for (String v : values)
            tags.add(v);
    }

    @Override
    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }
}
