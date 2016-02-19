/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.test.analytics.sensors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static quarks.function.Functions.identity;

import java.util.List;

import org.junit.Test;

import com.google.gson.JsonObject;

import quarks.analytics.sensors.Filters;
import quarks.test.providers.direct.DirectTestSetup;
import quarks.test.topology.TopologyAbstractTest;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.tester.Condition;

public class FiltersTest  extends TopologyAbstractTest implements DirectTestSetup {
	@Test
	public void testDeadbandIdentity() throws Exception {
	    Topology topology = newTopology("testDeadband");
	    
	    TStream<Double> values = topology.of(12.9, 3.4, 12.3, 15.6, 18.4, -3.7, -4.5, 15.0, 16.0, 30.0, 42.0 );
	    
	    TStream<Double> filtered = Filters.deadband(values, identity(),
	    		v -> v >= 10.0 && v <= 30.0);
	    
        Condition<Long> count = topology.getTester().tupleCount(filtered, 7);
        Condition<List<Double>> contents = topology.getTester().streamContents(filtered, 12.9, 3.4, 12.3, -3.7, -4.5, 15.0, 42.0 );
        complete(topology, count);
        assertTrue(count.valid());
        assertTrue(contents.valid());
	}
	@Test
	public void testDeadbandFunction() throws Exception {
	    Topology topology = newTopology("testDeadbandFunction");
	    
	    TStream<Double> values = topology.of(3.4, 12.3, 15.6, 18.4, -3.7, -4.5, 15.0, 16.0, 30.0, 42.0 );
	    
	    TStream<JsonObject> vj = values.map(d -> {JsonObject j = new JsonObject(); j.addProperty("id", "A"); j.addProperty("reading", d);return j;});
	    
	    TStream<JsonObject> filtered = Filters.deadband(vj,
	    		tuple -> tuple.get("reading").getAsDouble(),
	    		v -> v >= 10.0 && v <= 30.0);
	    
        Condition<Long> count = topology.getTester().tupleCount(filtered, 6);
        Condition<List<JsonObject>> contents = topology.getTester().streamContents(filtered);
        complete(topology, count);
        assertTrue(count.valid());
        
        List<JsonObject> results = contents.getResult();
        assertEquals(6, results.size());
        
        assertEquals("A", results.get(0).get("id").getAsString());
        assertEquals(3.4, results.get(0).get("reading").getAsDouble(), 0.0);
        
        // First value after a period out of range
        assertEquals("A", results.get(1).get("id").getAsString());
        assertEquals(12.3, results.get(1).get("reading").getAsDouble(), 0.0);
        
        assertEquals("A", results.get(2).get("id").getAsString());
        assertEquals(-3.7, results.get(2).get("reading").getAsDouble(), 0.0);
        
        assertEquals("A", results.get(3).get("id").getAsString());
        assertEquals(-4.5, results.get(3).get("reading").getAsDouble(), 0.0);
        
        assertEquals("A", results.get(4).get("id").getAsString());
        assertEquals(15.0, results.get(4).get("reading").getAsDouble(), 0.0);
        
        assertEquals("A", results.get(5).get("id").getAsString());
        assertEquals(42.0, results.get(5).get("reading").getAsDouble(), 0.0);
	}
}
