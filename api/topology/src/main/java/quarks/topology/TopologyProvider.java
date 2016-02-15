/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology;

/**
 * Provider (factory) for creating topologies.
 *
 */
public interface TopologyProvider {

    /**
     * Create a new topology with a given name.
     * 
     * @return A new topology.
     */
    Topology newTopology(String name);

    /**
     * Create a new topology with a generated name.
     * 
     * @return A new topology.
     */
    Topology newTopology();
}
