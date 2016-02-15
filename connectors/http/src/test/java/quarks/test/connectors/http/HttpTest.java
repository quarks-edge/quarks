/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.connectors.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import com.google.gson.JsonObject;

import quarks.connectors.http.HttpClients;
import quarks.connectors.http.HttpResponders;
import quarks.connectors.http.HttpStreams;
import quarks.providers.direct.DirectProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.tester.Condition;
import quarks.topology.tester.Tester;

/**
 * These tests go against http://httpbin.org
 * a freely available web-server for testing requests.
 *
 */
public class HttpTest {

    @Test
    public void testGet() throws Exception {
        
        DirectProvider ep = new DirectProvider();
        
        Topology topology = ep.newTopology();
        
        String url = "http://httpbin.org/get";
        
        TStream<String> rc = HttpStreams.<String,String>requests(
                topology.strings(url),
                HttpClients::noAuthentication,
                t-> HttpGet.METHOD_NAME,
                t->t,
                HttpResponders.inputOn200());
        
        Tester tester =  topology.getTester();
        
        Condition<List<String>> endCondition = tester.streamContents(rc, url);
        
        tester.complete(ep, new JsonObject(), endCondition, 10, TimeUnit.SECONDS);
        
        assertTrue(endCondition.valid());
    }
    
    @Test
    public void testStatusCode() throws Exception {
        
        DirectProvider ep = new DirectProvider();
        
        Topology topology = ep.newTopology();
        
        String url = "http://httpbin.org/status/";
        
        TStream<Integer> rc = HttpStreams.<Integer,Integer>requests(
                topology.collection(Arrays.asList(200, 404, 202)),
                HttpClients::noAuthentication,
                t-> HttpGet.METHOD_NAME,
                t-> url + Integer.toString(t),
                (t,resp) -> resp.getStatusLine().getStatusCode());
        
        Tester tester =  topology.getTester();
        
        Condition<List<Integer>> endCondition = tester.streamContents(rc, 200, 404, 202);
        
        tester.complete(ep,  new JsonObject(), endCondition, 10, TimeUnit.SECONDS);
        
        assertTrue(endCondition.valid());
    }
    
    /**
     * Test basic authentication, first with valid user/password
     * and then with invalid (results in 401).
     * @throws Exception
     */
    @Test
    public void testBasicAuthentication() throws Exception {
        
        DirectProvider ep = new DirectProvider();
        
        Topology topology = ep.newTopology();
        
        String url = "http://httpbin.org/basic-auth/";
        
        TStream<Integer> rc = HttpStreams.<String,Integer>requests(
                topology.strings("A", "B"),
                () -> HttpClients.basic("usA", "pwdA4"),
                t-> HttpGet.METHOD_NAME,
                t-> url + "us" + t + "/pwd" + t + "4",
                (t,resp) -> resp.getStatusLine().getStatusCode());
        
        Tester tester =  topology.getTester();
        
        Condition<List<Integer>> endCondition = tester.streamContents(rc, 200, 401);
        
        tester.complete(ep,  new JsonObject(), endCondition, 10, TimeUnit.SECONDS);
        
        assertTrue(endCondition.getResult().toString(), endCondition.valid());
    }
    
    
    @Test
    public void testJsonGet() throws Exception {
        
        DirectProvider ep = new DirectProvider();
        
        Topology topology = ep.newTopology();
        
        final String url = "http://httpbin.org/get?";
        
        JsonObject request1 = new JsonObject();
        request1.addProperty("a", "abc");
        request1.addProperty("b", "42");
        
        TStream<JsonObject> rc = HttpStreams.getJson(
                topology.collection(Arrays.asList(request1)),
                HttpClients::noAuthentication,
                t-> url + "a=" + t.get("a").getAsString() + "&b=" + t.get("b").getAsString()
                );
        
        TStream<Boolean> resStream = rc.map(j -> {
            assertTrue(j.has("request"));
            assertTrue(j.has("response"));
            JsonObject req = j.getAsJsonObject("request");
            JsonObject res = j.getAsJsonObject("response");
            
            assertTrue(res.has("status"));
            assertTrue(res.has("entity"));           
            
            assertEquals(req, res.getAsJsonObject("entity").getAsJsonObject("args"));
            return true;
        }
        );
        
        rc.print();
         
        Tester tester =  topology.getTester();
        
        Condition<List<Boolean>> endCondition = tester.streamContents(resStream, true);
        
        tester.complete(ep,  new JsonObject(), endCondition, 10, TimeUnit.SECONDS);
        
        assertTrue(endCondition.getResult().toString(), endCondition.valid());
    }
}
