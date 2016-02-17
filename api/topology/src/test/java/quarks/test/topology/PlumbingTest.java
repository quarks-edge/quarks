/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.topology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;

import quarks.function.Functions;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.plumbing.PlumbingStreams;
import quarks.topology.tester.Condition;

@Ignore
public abstract class PlumbingTest extends TopologyAbstractTest {

    @Test
    public void testBlockingDelay() throws Exception {

        Topology topology = newTopology();
        
        TStream<String> strings = topology.strings("a", "b", "c", "d");
        
        TStream<Long> starts = strings.map(v -> System.currentTimeMillis());
        
        // delay stream
        starts = PlumbingStreams.blockingDelay(starts, 300, TimeUnit.MILLISECONDS);
        
        // calculate display
        starts = starts.modify(v -> System.currentTimeMillis() - v);
        
        starts = starts.filter(v -> v >= 300);
        
        Condition<Long> tc = topology.getTester().tupleCount(starts, 4);
        complete(topology, tc);
        assertTrue("valid:" + tc.getResult(), tc.valid());
    }

    @Test
    public void testBlockingThrottle() throws Exception {

        Topology topology = newTopology();
        
        TStream<String> strings = topology.strings("a", "b", "c", "d");

        TStream<Long> emittedDelays = strings.map(v -> 0L);
        
        // throttle stream
        long[] lastEmittedTimestamp = { 0 };
        emittedDelays = PlumbingStreams.blockingThrottle(emittedDelays, 300, TimeUnit.MILLISECONDS)
                .map(t -> {
                    // compute the delay since the last emitted tuple
                    long now = System.currentTimeMillis();
                    if (lastEmittedTimestamp[0] == 0)
                        lastEmittedTimestamp[0] = now;
                    t = now - lastEmittedTimestamp[0];
                    lastEmittedTimestamp[0] = now;
                    // System.out.println("### "+t);
                    return t;
                    })
                .map(t -> {
                    // simulate 200ms downstream processing delay
                    try {
                        Thread.sleep(TimeUnit.MILLISECONDS.toMillis(200));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    } return t;
                    }) ;

        // should end up with throttled delays close to 300 (not 500 like
        // a blockingDelay() under these same conditions would yield)
        emittedDelays = emittedDelays.filter(v -> v <= 320);
        
        Condition<Long> tc = topology.getTester().tupleCount(emittedDelays, 4);
        complete(topology, tc);
        assertTrue("valid:" + tc.getResult(), tc.valid());
    }

    @Test
    public void testOneShotDelay() throws Exception {

        Topology topology = newTopology();
        
        TStream<String> strings = topology.strings("a", "b", "c", "d");
        
        TStream<Long> starts = strings.map(v -> System.currentTimeMillis());
        
        // delay stream
        starts = PlumbingStreams.blockingOneShotDelay(starts, 300, TimeUnit.MILLISECONDS);
        
        // calculate display
        starts = starts.modify(v -> System.currentTimeMillis() - v);
        
        // the first tuple shouldn't satisfy the predicate
        starts = starts.filter(v -> v < 300);
        
        Condition<Long> tc = topology.getTester().tupleCount(starts, 3);
        complete(topology, tc);
        assertTrue("valid:" + tc.getResult(), tc.valid());
    }

    public static class TimeAndId {
    	private static AtomicInteger ids = new AtomicInteger();
    	long ms;
    	final int id;
    	
    	public TimeAndId() {
    		this.ms = System.currentTimeMillis();
    		this.id = ids.incrementAndGet();
    	}
    	public TimeAndId(TimeAndId tai) {
    		this.ms = System.currentTimeMillis() - tai.ms;
    		this.id = tai.id;
    	}
    	@Override
    	public String toString() {
    		return "TAI:" + id + "@" + ms;
    	}
    	
    }
    
    @Test
    public void testPressureReliever() throws Exception {

        Topology topology = newTopology();
        
        TStream<TimeAndId> raw = topology.poll(() -> new TimeAndId(), 10, TimeUnit.MILLISECONDS);
           
        
        TStream<TimeAndId> pr = PlumbingStreams.pressureReliever(raw, Functions.unpartitioned(), 5);
        
        // insert a blocking delay acting as downstream operator that cannot keep up
        TStream<TimeAndId> slow = PlumbingStreams.blockingDelay(pr, 200, TimeUnit.MILLISECONDS);
        
        // calculate the delay
        TStream<TimeAndId> slowM = slow.modify(v -> new TimeAndId(v));
        
        // Also process raw that should be unaffected by the slow path
        TStream<String> processed = raw.asString();
        
        
        Condition<Long> tcSlowCount = topology.getTester().atLeastTupleCount(slow, 20);
        Condition<List<TimeAndId>> tcRaw = topology.getTester().streamContents(raw);
        Condition<List<TimeAndId>> tcSlow = topology.getTester().streamContents(slow);
        Condition<List<TimeAndId>> tcSlowM = topology.getTester().streamContents(slowM);
        Condition<List<String>> tcProcessed = topology.getTester().streamContents(processed);
        complete(topology, tcSlowCount);
        
        assertTrue(tcProcessed.getResult().size() > tcSlowM.getResult().size());
        for (TimeAndId delay : tcSlowM.getResult())
            assertTrue(delay.ms < 300);

        // Must not lose any tuples in the non relieving path
        Set<TimeAndId> uniq = new HashSet<>(tcRaw.getResult());
        assertEquals(tcRaw.getResult().size(), uniq.size());

        // Must not lose any tuples in the non relieving path
        Set<String> uniqProcessed = new HashSet<>(tcProcessed.getResult());
        assertEquals(tcProcessed.getResult().size(), uniqProcessed.size());
        
        assertEquals(uniq.size(), uniqProcessed.size());
           
        // Might lose tuples, but must not have send duplicates
        uniq = new HashSet<>(tcSlow.getResult());
        assertEquals(tcSlow.getResult().size(), uniq.size());
    }
    
    @Test
    public void testPressureRelieverWithInitialDelay() throws Exception {

        Topology topology = newTopology();
        
        
        TStream<String> raw = topology.strings("A", "B", "C", "D", "E", "F", "G", "H");
        
        TStream<String> pr = PlumbingStreams.pressureReliever(raw, v -> 0, 100);
        
        TStream<String> pr2 = PlumbingStreams.blockingOneShotDelay(pr, 5, TimeUnit.SECONDS);
        
        Condition<Long> tcCount = topology.getTester().tupleCount(pr2, 8);
        Condition<List<String>> contents = topology.getTester().streamContents(pr2, "A", "B", "C", "D", "E", "F", "G", "H");
        complete(topology, tcCount);
        
        assertTrue(tcCount.valid());
        assertTrue(contents.valid());
    }
}
