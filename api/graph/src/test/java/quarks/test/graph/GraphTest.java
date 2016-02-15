/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.test.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import quarks.function.Consumer;
import quarks.graph.Edge;
import quarks.graph.Graph;
import quarks.graph.Vertex;
import quarks.oplet.Oplet;
import quarks.oplet.core.AbstractOplet;

@Ignore
public abstract class GraphTest extends GraphAbstractTest {

    @Test
    public void testEmptyGraph() {
        assertTrue(getGraph().getVertices().isEmpty());
    }

    @Test
    public void testGraphAccess() {
        Graph g = getGraph();
        
        TestOp<String, Integer> op = new TestOp<>();

        Vertex<TestOp<String, Integer>, String, Integer> v = g.insert(op, 1, 1);
        assertNotNull(v);
        assertSame(op, v.getInstance());

        Collection<Vertex<? extends Oplet<?, ?>, ?, ?>> c = getGraph().getVertices();
        assertNotNull(c);

        assertFalse(c.isEmpty());
        assertEquals(1, c.size());

        assertSame(v, c.toArray()[0]);
        
        assertTrue(getGraph().getEdges().isEmpty());

        try {
            c.clear();
            fail("Was able to modify graph collection");
        } catch (UnsupportedOperationException e) {
            // ok - expected
        }
        
        
        TestOp<Integer, Void> op2 = new TestOp<>();
        Vertex<TestOp<Integer, Void>, Integer, Void> v2 = g.insert(op2, 1, 0);
        
        c = getGraph().getVertices();
        assertNotNull(c);

        assertFalse(c.isEmpty());
        assertEquals(2, c.size());

        assertSame(v, c.toArray()[0]);
        assertSame(v2, c.toArray()[1]);
        
        assertTrue(getGraph().getEdges().isEmpty());
        
        v.getConnectors().get(0).connect(v2, 0);
        
        Collection<Edge> edges = getGraph().getEdges();
        assertFalse(edges.isEmpty());
        assertEquals(1, edges.size());
        
        Edge vtov2 = (Edge) edges.toArray()[0];
        assertSame(v, vtov2.getSource());
        assertEquals(0, vtov2.getSourceOutputPort());
        
        assertSame(v2, vtov2.getTarget());
        assertEquals(0, vtov2.getTargetInputPort());
    }

    private static class TestOp<I, O> extends AbstractOplet<I, O> {

        @Override
        public void start() {
        }

        @Override
        public List<? extends Consumer<I>> getInputs() {
            return null;
        }

        @Override
        public void close() throws Exception {
        }
    }
}
