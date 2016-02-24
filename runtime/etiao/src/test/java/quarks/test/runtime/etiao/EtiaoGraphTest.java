/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.runtime.etiao;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import quarks.execution.services.ServiceContainer;
import quarks.function.Consumer;
import quarks.graph.Connector;
import quarks.graph.Graph;
import quarks.graph.Vertex;
import quarks.oplet.core.AbstractOplet;
import quarks.oplet.core.Sink;
import quarks.oplet.core.Split;
import quarks.oplet.functional.SupplierPeriodicSource;
import quarks.runtime.etiao.graph.DirectGraph;
import quarks.runtime.etiao.graph.model.GraphType;
import quarks.test.graph.GraphTest;

public class EtiaoGraphTest extends GraphTest {

    @Override
    protected Graph createGraph() {
        return new DirectGraph(this.getClass().getSimpleName(), new ServiceContainer());
    }

    @Test
    public void testEmptyGraphToJson() {
        Graph g = getGraph();
        GraphType gt = new GraphType(g);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(gt);
        
        GraphType gt2 = new Gson().fromJson(json, GraphType.class);
        assertEquals(0, gt2.getVertices().size());
        assertEquals(0, gt2.getEdges().size());
    }

    @Test
    public void testGraphToJson() {
        Graph g = getGraph();
        TestOp<String, Integer> op = new TestOp<>();
        /* Vertex<TestOp<String, Integer>, String, Integer> v = */g.insert(op, 1, 1);
        GraphType gt = new GraphType(g);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(gt);
        
        GraphType gt2 = new Gson().fromJson(json, GraphType.class);
        assertEquals(1, gt2.getVertices().size());
        assertEquals(0, gt2.getEdges().size());
    }

    @Test
    public void testGraphToJson2() {
        Graph g = getGraph();

        TestOp<String, Integer> op1 = new TestOp<>();
        Vertex<TestOp<String, Integer>, String, Integer> v = g.insert(op1, 1, 1);
        
        TestOp<Integer, Integer> op2 = new TestOp<>();
        /*Connector<Integer> out2 = */g.pipe(v.getConnectors().get(0), op2);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(new GraphType(g));
        
        GraphType gt2 = new Gson().fromJson(json, GraphType.class);
        assertEquals(2, gt2.getVertices().size());
        assertEquals(1, gt2.getEdges().size());
    }

    @Test
    public void testGraphToJson4() {
        Graph g = getGraph();
        
        /*                                   /-- V2
         * V0(Integer)-- V1(Double)-- FanOut
         *                                   \-- V3 
         */
        Vertex<TestOp<String, Integer>, String, Integer> v0 = g.insert(new TestOp<>(), 1, 1);
        Connector<Integer> out0 = v0.getConnectors().get(0);
        Connector<Double> out1 = g.pipe(out0, new TestOp<Integer, Double>());
        Vertex<TestOp<Double, String>, Double, String> v2 = g.insert(new TestOp<Double, String>(), 1, 1);
        Vertex<TestOp<Double, String>, Double, String> v3 = g.insert(new TestOp<Double, String>(), 1, 1);
        out1.connect(v2, 0);
        out1.connect(v3, 0);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(new GraphType(g));
        
        GraphType gt2 = new Gson().fromJson(json, GraphType.class);
        assertEquals(5, gt2.getVertices().size());
        assertEquals(4, gt2.getEdges().size());
    }

    @Test
    public void testGraphToJson5() {
        Graph g = getGraph();
        
        /*                         /-- V2
         * V0(Double)-- V1(Double)---- V3
         *                         \-- V4 
         */
        Random r = new Random();
        Vertex<SupplierPeriodicSource<Double>, Void, Double> v0 = g.insert(
                new SupplierPeriodicSource<>(100, TimeUnit.MILLISECONDS, () -> (r.nextDouble() * 3)),
                0, 1);
        Connector<Double> out0 = v0.getConnectors().get(0);
        out0.tag("dots", "hashes", "ats");
        
        // Insert split - see ConnectorStream.split()
        Split<Double> splitOp = new Split<Double>(
                tuple -> {
                    switch (tuple.intValue()) {
                    case 0:
                        return 0;
                    case 1:
                        return 1;
                    default:
                        return 2;
                    }
                });
        Vertex<Split<Double>, Double, Double> v1 = g.insert(splitOp, 1, 3);
        out0.connect(v1, 0);

        // Insert and connect sinks
        Vertex<Sink<Double>, Double, Void> v2 = g.insert(
                new Sink<>(tuple -> System.out.print(".")), 1, 0);
        v1.getConnectors().get(0).connect(v2, 0);
        v1.getConnectors().get(0).tag("dots");

        Vertex<Sink<Double>, Double, Void> v3 = g.insert(
                new Sink<>(tuple -> System.out.print("#")), 1, 0);
        v1.getConnectors().get(1).connect(v3, 0);
        v1.getConnectors().get(1).tag("hashes");
        
        Vertex<Sink<Double>, Double, Void> v4 = g.insert(
                new Sink<>(tuple -> System.out.print("@")), 1, 0);
        v1.getConnectors().get(2).connect(v4, 0);
        v1.getConnectors().get(2).tag("ats");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(new GraphType(g));
        
        GraphType gt = new Gson().fromJson(json, GraphType.class);
        assertEquals(5, gt.getVertices().size());
        assertEquals(4, gt.getEdges().size());
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
