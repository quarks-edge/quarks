package quarks.test.providers.dev;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import quarks.graph.Graph;
import quarks.graph.Vertex;
import quarks.metrics.oplets.CounterOp;
import quarks.oplet.Oplet;
import quarks.test.topology.TopologyAbstractTest;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.tester.Condition;

public class DevelopmentProviderTest extends TopologyAbstractTest implements DevelopmentTestSetup {

    // DevelopmentProvider inserts CounterOp metric oplets into the graph
    @Test
    public void testMetricsEverywhere() throws Exception {

        Topology t = newTopology();
        TStream<String> s = t.strings("a", "b", "c");

        // Condition inserts a sink
        Condition<Long> tc = t.getTester().tupleCount(s, 3);

        Graph g = t.graph();
        Collection<Vertex<? extends Oplet<?, ?>, ?, ?>> vertices = g.getVertices();
        
        // Two vertices before submission
        assertEquals(2, vertices.size());

        complete(t, tc);
  
        // Three vertices after submission
        Collection<Vertex<? extends Oplet<?, ?>, ?, ?>> verticesAfterSubmit = g.getVertices();
        assertEquals(3, verticesAfterSubmit.size());
        
        // The new vertex is for a metric oplet
        boolean found = false;
        for (Vertex<? extends Oplet<?, ?>, ?, ?> v : verticesAfterSubmit) {
            Oplet<?,?> oplet = v.getInstance();
            if (oplet instanceof CounterOp) {
                found = true;
            }
        }
        assertTrue(found);
    }
}
