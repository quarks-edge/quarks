/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.topology;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.TopologyElement;

public class TopologyElementTest {

    @Test
    public void testHierachy() {
        assertTrue(TopologyElement.class.isAssignableFrom(Topology.class));
        assertTrue(TopologyElement.class.isAssignableFrom(TStream.class));
        assertTrue(TopologyElement.class.isAssignableFrom(TSink.class));
    }
}
