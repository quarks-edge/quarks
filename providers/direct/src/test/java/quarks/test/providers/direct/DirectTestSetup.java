/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.providers.direct;

import quarks.execution.Job;
import quarks.execution.Submitter;
import quarks.providers.direct.DirectProvider;
import quarks.test.topology.TopologyTestSetup;
import quarks.topology.Topology;

public interface DirectTestSetup extends TopologyTestSetup {
    @Override
    default DirectProvider createTopologyProvider() {
        return new DirectProvider();
    }

    @Override
    default Submitter<Topology, Job> createSubmitter() {
        return (DirectProvider) getTopologyProvider();
    }

}
