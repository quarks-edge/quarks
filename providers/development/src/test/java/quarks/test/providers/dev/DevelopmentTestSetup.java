/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.test.providers.dev;

import quarks.execution.Job;
import quarks.execution.Submitter;
import quarks.providers.development.DevelopmentProvider;
import quarks.test.topology.TopologyTestSetup;
import quarks.topology.Topology;

public interface DevelopmentTestSetup extends TopologyTestSetup {
    @Override
    default DevelopmentProvider createTopologyProvider() {
        try {
            return new DevelopmentProvider();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    default Submitter<Topology, Job> createSubmitter() {
        return (DevelopmentProvider) getTopologyProvider();
    }
}
