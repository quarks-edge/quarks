/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology.spi;

import java.util.UUID;

import quarks.topology.Topology;
import quarks.topology.TopologyProvider;

public abstract class AbstractTopologyProvider<T extends Topology> implements TopologyProvider {

    @Override
    public abstract T newTopology(String name);

    @Override
    public T newTopology() {
        return newTopology(UUID.randomUUID().toString());
    }
}
