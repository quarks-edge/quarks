/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.topology;

import quarks.execution.Job;
import quarks.execution.Submitter;
import quarks.topology.Topology;
import quarks.topology.TopologyProvider;

public interface TopologyTestSetup {

    TopologyProvider createTopologyProvider();

    TopologyProvider getTopologyProvider();

    Submitter<Topology, Job> createSubmitter();
}
