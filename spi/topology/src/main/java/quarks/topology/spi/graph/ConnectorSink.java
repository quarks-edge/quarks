/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.topology.spi.graph;

import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.Topology;

class ConnectorSink<G extends Topology,T> implements TSink<T> {
    
    private final ConnectorStream<G,T> feed; 
    
    ConnectorSink(ConnectorStream<G,T> feed) {
        this.feed = feed;
    }

    @Override
    public Topology topology() {
        return feed.topology();
    }
    @Override
    public TStream<T> getFeed() {
        return feed;
    }
}
