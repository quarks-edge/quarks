/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.topology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static quarks.function.Functions.identity;
import static quarks.function.Functions.unpartitioned;
import static quarks.function.Functions.zero;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import quarks.topology.TStream;
import quarks.topology.TWindow;
import quarks.topology.Topology;
import quarks.topology.tester.Condition;

@Ignore
public abstract class TWindowTest extends TopologyAbstractTest{

    @Test
    public void testKeyedWindowSum() throws Exception {
        Topology t = newTopology();
        
        TStream<Integer> integers = t.collection(Arrays.asList(1,2,3,4,4,3,4,4,3));
        TWindow<Integer, Integer> window = integers.last(9, identity());
        assertSame(identity(), window.getKeyFunction());
        assertSame(t, window.topology());
        assertSame(integers, window.feeder());

        TStream<Integer> sums = window.aggregate((tuples, key) -> {
            // All tuples in a partition are equal due to identity
            assertEquals(1, new HashSet<>(tuples).size());
            int sum = 0;
            for(Integer tuple : tuples)
                sum+=tuple;
            return sum;
        });
        
        Condition<Long> tc = t.getTester().tupleCount(sums, 9);
        Condition<List<Integer>> contents = t.getTester().streamContents(sums, 
                1, 2, 3, 4, 8, 6, 12, 16, 9);
        complete(t, tc);

        assertTrue(contents.valid());
    }
    
    @Test
    public void testWindowSum() throws Exception {
        Topology t = newTopology();
        
        TStream<Integer> integers = t.collection(Arrays.asList(1,2,3,4));
        TWindow<Integer, Integer> window = integers.last(4, unpartitioned());
        assertSame(unpartitioned(), window.getKeyFunction());
        TStream<Integer> sums = window.aggregate((tuples, key) -> {
            assertEquals(Integer.valueOf(0), key);
            int sum = 0;
            for(Integer tuple : tuples)
                sum+=tuple;
            return sum;
        });

        Condition<Long> tc = t.getTester().tupleCount(sums, 4);
        Condition<List<Integer>> contents = t.getTester().streamContents(sums, 1, 3, 6, 10);
        complete(t, tc);

        assertTrue(contents.valid());
    }
    
    @Test
    public void testTimeWindowTimeDiff() throws Exception {
        Topology t = newTopology();
        
        // Define data
        ConcurrentLinkedQueue<Long> diffs = new ConcurrentLinkedQueue<>();
        
        // Poll data
        TStream<Long> times = t.poll(() -> {   
            return System.currentTimeMillis();
        }, 1, TimeUnit.MILLISECONDS);

        TWindow<Long, Integer> window = times.last(1, TimeUnit.SECONDS, unpartitioned());
        assertSame(zero(), window.getKeyFunction());
        TStream<Long> diffStream = window.aggregate((tuples, key) -> {
            assertEquals(Integer.valueOf(0), key);
            if(tuples.size() < 2){
                return (long)0;
            }
            return tuples.get(tuples.size() -1) - (long)tuples.get(0);
        });
        
        diffStream.sink((tuple) -> diffs.add(tuple));
        
        Condition<Long> tc = t.getTester().tupleCount(diffStream, 5000);
        complete(t, tc);
        
        for(Long diff : diffs){
            assertTrue(diff < 1040);
        }
        
    }
    
    public static boolean withinTolerance(double expected, Double actual, double tolerance) {
        double lowBound = (1.0 - tolerance) * expected;
        double highBound = (1.0 + tolerance) * expected;
        return (actual < highBound && actual > lowBound);
    }
}
