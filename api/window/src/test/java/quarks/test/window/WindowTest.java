/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.window;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static quarks.function.Functions.unpartitioned;
import static quarks.window.Policies.alwaysInsert;
import static quarks.window.Policies.evictOlderWithProcess;
import static quarks.window.Policies.insertionTimeList;
import static quarks.window.Policies.processOnInsert;
import static quarks.window.Policies.scheduleEvictIfEmpty;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import quarks.function.BiConsumer;
import quarks.function.BiFunction;
import quarks.window.InsertionTimeList;
import quarks.window.Policies;
import quarks.window.Window;
import quarks.window.Windows;


public class WindowTest {
	
    /**
     * Verifies that the state of the window is correct after each tuple offer.
     */
    @Test
    public void lastCountTest(){
        final int COUNT = 100;
        // The window implementation
        Window<Integer, Integer, ? extends List<Integer>> window = Windows.lastNProcessOnInsert(10, unpartitioned());
        // The states of the window as it slides
        LinkedList<List<Integer> > incrementalWindowStates = new LinkedList<>();
        
        // A processor that records the states of the window
        BiConsumer<List<Integer>, Integer> wp = (tuples, key) -> {
            incrementalWindowStates.addLast(new LinkedList<Integer>(tuples));
        };
        window.registerPartitionProcessor(wp);
        
        // Generate sliding window correct incremental state to compare
        // against the window's
        LinkedList<LinkedList<Integer>> correctWindowStates = new LinkedList<>();
        LinkedList<Integer> previous = null;
        LinkedList<Integer> current = null;
        for(int i = 0; i < COUNT; i++){
            current = new LinkedList<>();         
            if(previous != null)
                current.addAll(previous);
            
            current.addLast(i);
            if(current.size() > 10){
                current.removeFirst();
            }
            previous = current;
            correctWindowStates.addLast(current);              
        }
        
        // Add tuples to window, populating the incrementalWindowStates list.
        for(int i = 0; i < COUNT; i++){
            window.insert(i);
        }

        // Compare correct window states to the window implementation's
        assertTrue(correctWindowStates.size() == incrementalWindowStates.size());
        for(int i = 0; i < correctWindowStates.size(); i++){
            assertTrue(correctWindowStates.get(i).containsAll(incrementalWindowStates.get(i)));
            assertTrue(incrementalWindowStates.get(i).containsAll(correctWindowStates.get(i)));
        }
    }
    
    @Test
    public void keyedWindowTest(){
        final int COUNT = 1000;
        // The window implementation
     // The window implementation
        Window<Integer, Integer, ? extends List<Integer>> window = Windows.lastNProcessOnInsert(10, tuple->tuple%10);
        
        
        // The states of the window as it slides
        LinkedList<List<Integer> > incrementalWindowStates = new LinkedList<>();
        
        // A processor that records the states of the window
        BiConsumer<List<Integer>, Integer> wp = (tuples, key) -> {
            incrementalWindowStates.addLast(new LinkedList<Integer>(tuples));
        };    
        window.registerPartitionProcessor(wp); 
        
        Map<Integer, LinkedList<Integer>> correctPartitionedStates = new HashMap<>();
        List<List<Integer> > correctWindowStates = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            correctPartitionedStates.put(i, new LinkedList<>());
        }
        for(int i = 0; i < COUNT; i++){
            correctPartitionedStates.get(i%10).add(i);
            if(correctPartitionedStates.get(i%10).size() > 10){
                correctPartitionedStates.get(i%10).removeFirst();
            }
            correctWindowStates.add(new ArrayList<>(correctPartitionedStates.get(i%10)));
            window.insert(i);
        }
        
        // Compare correct window states to the window implementation's
        assertTrue(correctWindowStates.size() == incrementalWindowStates.size());
        for(int i = 0; i < correctWindowStates.size(); i++){
            assertTrue(correctWindowStates.get(i).containsAll(incrementalWindowStates.get(i)));
            assertTrue(incrementalWindowStates.get(i).containsAll(correctWindowStates.get(i)));
        }    
    }
    
    @Test
    public void accessPartitionKeyTest(){
        LinkedList<List<Integer> > incrementalWindowStates = new LinkedList<>();
        
        Window<Integer, Integer, ? extends List<Integer>> window = Windows.<Integer, Integer, LinkedList<Integer>>window(
        (partition, tuple) -> {
            if (partition.getKey().equals(1) || partition.getKey().equals(3)) {
                return false;
            }
            return true;
        },
        (partition, tuple) -> { // Contents policy

                }, 
        (partition) -> { // Evict determiner
                    partition.getContents().clear();
                }, 
        Policies.processOnInsert(), 
        tuple -> tuple, 
        () -> new LinkedList<Integer>());

        // A processor that records the states of the window
        BiConsumer<List<Integer>, Integer> wp = (tuples, key) -> {
            incrementalWindowStates.addLast(new LinkedList<Integer>(tuples));
        };    
        window.registerPartitionProcessor(wp); 
        
        
        for(Integer i = 0; i < 5; i++){
            window.insert(i);
        }
        
        assertTrue(incrementalWindowStates.size() == 3);
        assertTrue(incrementalWindowStates.get(0).get(0)==0);
        assertTrue(incrementalWindowStates.get(1).get(0)==2);
        assertTrue(incrementalWindowStates.get(2).get(0)==4);
        
    }
    
    @Test
    public void concurrentWindowAccessTest() throws InterruptedException {
       
        Window<Integer, Integer, ? extends List<Integer>> window = Windows.lastNProcessOnInsert(10, tuple -> 0);
        
        window.registerPartitionProcessor((tuples, key) -> {
            // ensure that the window state doesn't change after .05 seconds
            // Copy window state
            LinkedList<Integer> list_copy = new LinkedList<Integer>(tuples);
            
            // Wait .05 seconds
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify that the partition is unchanged
            assertTrue(list_copy.containsAll(tuples));
            assertTrue(tuples.containsAll(list_copy));
        });
           
        
        // Run for five seconds.
        long finishTime = System.currentTimeMillis() + 3000;
        
        List<Thread> threads = new ArrayList<Thread>();
        
        // Ten threads concurrently attempt to insert tuples into the window
        for(int i = 0; i < 10; i++){
            threads.add(new Thread(new Runnable(){
                Random r = new Random();
                @Override
                public void run() {
                    
                    while(System.currentTimeMillis() < finishTime){                       
                        try{
                            window.insert(r.nextInt());
                        }
                        catch(ConcurrentModificationException cme){
                            org.junit.Assert.fail("State of window changed while processing");
                        }
                    }  
                }
                
            }));
        }
        for(Thread thread : threads){
            thread.start();
            Thread.sleep(10);
        }
        for(Thread thread : threads)
            thread.join();
    }
    
    
    @Test
    public void noWaitConcurrentWindowAccessTest() throws InterruptedException {
        Window<Integer, Integer, ? extends List<Integer>> window = Windows.lastNProcessOnInsert(100, tuple -> 0);
        window.registerPartitionProcessor((tuples, key) -> {});
        long finishTime = System.currentTimeMillis() + 3000;
        
        List<Thread> threads = new ArrayList<Thread>();
        
        // Ten threads concurrently attempt to insert tuples into the window
        for(int i = 0; i < 10; i++){
            threads.add(new Thread(new Runnable(){
                Random r = new Random();
                @Override
                public void run() {
                    while(System.currentTimeMillis() < finishTime){                       
                        try{
                            window.insert(r.nextInt());
                        }
                        catch(ConcurrentModificationException cme){
                            org.junit.Assert.fail("State of window changed while processing");
                        }
                    }  
                }
                
            }));
        }
        for(Thread thread : threads){
            thread.start();
            Thread.sleep(10);
        }
        for(Thread thread : threads)
            thread.join();
    }
    
    @Test
    public void timeActionTest() throws InterruptedException {
		// Timing variances on shared machines can cause this test to fail
    	assumeTrue(!Boolean.getBoolean("quarks.build.ci"));

        List<Long> diffs = new ArrayList<>();
        List<Boolean> initialized = new ArrayList<>();
        initialized.add(false);
        
        Window<Long, Integer, InsertionTimeList<Long>> window = // new TimeWindowImpl<Long, Integer, LinkedList<Long>>(
        Windows.window(
                Policies.alwaysInsert(), // insertion policy
                Policies.scheduleEvictIfEmpty(1000, TimeUnit.MILLISECONDS), 
                // Policies.evictOlderThan(1000, TimeUnit.MILLISECONDS), 
                Policies.evictOlderWithProcess(1000, TimeUnit.MILLISECONDS), 
                (partition, tuple) -> {
                    if(initialized.get(0).booleanValue() == false){
                        initialized.set(0, true);
                        ScheduledExecutorService ses = partition.getWindow().getScheduledExecutorService();
                        ses.scheduleAtFixedRate(() -> {partition.process();}, 0, 1000, TimeUnit.MILLISECONDS);
                    }}, 
                unpartitioned(),
                () -> new InsertionTimeList<Long>());
        
        window.registerPartitionProcessor((tuples, key) -> {
            if(tuples.size() > 1)
                diffs.add(tuples.get(tuples.size()-1) - tuples.get(0));
        });
        
        window.registerScheduledExecutorService(new ScheduledThreadPoolExecutor(5));
        
        long endTime = System.currentTimeMillis() + 8000;
        List<Thread> threads = new ArrayList<>();
        int NUM_THREADS = 10;
        // Create 10 threads. Each inserts at 1,000 Hz
        for(int i = 0; i < NUM_THREADS; i++){
            threads.add(new Thread(new Runnable() {     
                @Override
                public void run() {
                    while(System.currentTimeMillis() < endTime){
                        window.insert(System.currentTimeMillis());
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }));
        }
        for(int i = 0; i < NUM_THREADS; i++){
            threads.get(i).start();
        }
        for(int i = 0; i < NUM_THREADS; i++){
            threads.get(i).join();
        }
        assertOnTimeEvictions(diffs);
        
    }
    
    @Test
    public void countBatchWindowTest(){
        List<Integer> numBatches = new LinkedList();
        Window<Integer, Integer, List<Integer>> window =
                Windows.window(
                        alwaysInsert(),
                        Policies.countContentsPolicy(113),
                        Policies.evictAll(),
                        Policies.processWhenFull(113),
                        tuple -> 0,
                        () -> new ArrayList<Integer>());
        window.registerPartitionProcessor(new BiConsumer<List<Integer>, Integer>() {
            int count = 0;
            @Override
            public void accept(List<Integer> t, Integer u) {
                count++;
                numBatches.add(1);
            }
        });
        for(int i = 0; i < 1000; i++){
            window.insert(i);
        }
        
        assertTrue(numBatches.size() == 8);
    }

    @Test
    public void timeBatchWindowTest() throws InterruptedException{
        List<Long> numBatches = new LinkedList();
        
        Window<Integer, Integer, List<Integer>> window =
                Windows.window(
                        alwaysInsert(),
                        Policies.scheduleEvictOnFirstInsert(1, TimeUnit.SECONDS),
                        Policies.evictAllAndScheduleEvict(1, TimeUnit.SECONDS),
                        (partiton, tuple) -> {},
                        tuple -> 0,
                        () -> new ArrayList<Integer>());
        
        window.registerPartitionProcessor(new BiConsumer<List<Integer>, Integer>() {
            int count = 0;
            @Override
            public void accept(List<Integer> t, Integer u) {
                numBatches.add((long)t.size());
            }
        });
        
        window.registerScheduledExecutorService(new ScheduledThreadPoolExecutor(5));
        
        for(int i = 0; i < 1000; i++){
            window.insert(i);
            Thread.sleep(10);
        }
        Thread.sleep(10);
        double tolerance = .04;
        for(int i = 0; i < numBatches.size(); i++){
            assertTrue(withinTolerance(100.0, numBatches.get(i).doubleValue(), tolerance));
        }
        
    }
    
    private void assertOnTimeEvictions(List<Long> diffs) {
        double tolerance = .08;
        for(int i = 1; i < diffs.size(); i++){
            assertTrue(withinTolerance(1000.0, diffs.get(i).doubleValue(), tolerance));
        }
        
    }

    public static boolean withinTolerance(double expected, Double actual, double tolerance) {
        double lowBound = (1.0 - tolerance) * expected;
        double highBound = (1.0 + tolerance) * expected;
        return (actual < highBound && actual > lowBound);
    }

}
