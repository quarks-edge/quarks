/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.test.providers.direct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.google.gson.JsonObject;

import quarks.execution.Job;
import quarks.graph.Vertex;
import quarks.oplet.Oplet;
import quarks.oplet.core.PeriodicSource;
import quarks.oplet.core.Pipe;
import quarks.providers.direct.DirectProvider;
import quarks.test.topology.TopologyAbstractTest;
import quarks.topology.TStream;
import quarks.topology.Topology;

// TODO factor out job submission, waiting for completion
// TODO create test in quarks.test.runtime.embedded 
public class DirectJobTest extends TopologyAbstractTest implements DirectTestSetup {
    @Test
    public void jobName0() throws Exception {
        String[] data = new String[] {};
        String topologyName = "topoName";
        Topology t = newTopology(topologyName);
        t.strings(data);
        Job job = awaitCompleteExecution(t);
        assertTrue(job.getName().startsWith(topologyName));
    }

    @Test
    public void jobName1() throws Exception {
        String[] data = new String[] {};
        String topologyName = "topoName";
        Topology t = newTopology(topologyName);
        t.strings(data);
        Job job = awaitCompleteExecution(t, new Configuration().addProperty(DirectProvider.CONFIGURATION_JOB_NAME, (String)null).json());
        assertTrue(job.getName().startsWith(topologyName));
    }

    @Test
    public void jobName2() throws Exception {
        String[] data = new String[] {};
        String jobName = "testJob";
        Topology t = newTopology();
        t.strings(data);
        Job job = awaitCompleteExecution(t, new Configuration().addProperty(DirectProvider.CONFIGURATION_JOB_NAME, jobName).json());
        assertEquals(jobName, job.getName());
    }

    @Test
    public void jobDone0() throws Exception {
        String[] data = new String[] {};
        Topology t = newTopology();
        @SuppressWarnings("unused")
        TStream<String> s = t.strings(data);

        Job job = awaitCompleteExecution(t);
        assertEquals("job.getCurrentState() must be RUNNING", Job.State.RUNNING, job.getCurrentState());
        job.stateChange(Job.Action.CLOSE);
        assertEquals("job.getCurrentState() must be CLOSED", Job.State.CLOSED, job.getCurrentState());
    }

    @Test
    public void jobDone1() throws Exception {
        String[] data = new String[] {"a", "b", "c"};
        Topology t = newTopology();
        @SuppressWarnings("unused")
        TStream<String> s = t.strings(data);

        Job job = awaitCompleteExecution(t);
        assertEquals("job.getCurrentState() must be RUNNING", Job.State.RUNNING, job.getCurrentState());
        job.stateChange(Job.Action.CLOSE);
        assertEquals("job.getCurrentState() must be CLOSED", Job.State.CLOSED, job.getCurrentState());
    }

    @Test
    public void jobDone2() throws Exception {
        final int NUM_TUPLES = 1000000;
        Integer[] data = new Integer[NUM_TUPLES];
        AtomicInteger numTuples = new AtomicInteger();

        for (int i = 0; i < data.length; i++) {
            data[i] = new Integer(i);
        }
        Topology t = newTopology();
        TStream<Integer> ints = t.collection(Arrays.asList(data));
        ints.sink(tuple -> numTuples.incrementAndGet());

        Job job = awaitCompleteExecution(t);
        Thread.sleep(1500); // wait for numTuples visibility 
        assertEquals(NUM_TUPLES, numTuples.get());
        assertEquals("job.getCurrentState() must be RUNNING", Job.State.RUNNING, job.getCurrentState());
        job.stateChange(Job.Action.CLOSE);
        assertEquals("job.getCurrentState() must be CLOSED", Job.State.CLOSED, job.getCurrentState());
    }

    @Test
    public void jobPeriodicSource() throws Exception {
        Topology t = newTopology();
        AtomicInteger n = new AtomicInteger(0);
        @SuppressWarnings("unused")
        TStream<Integer> ints = t.poll(() -> n.incrementAndGet(), 100, TimeUnit.MILLISECONDS);

        Future<Job> fj = ((DirectProvider)getTopologyProvider()).submit(t);
        Job job = fj.get();
        assertEquals(Job.State.RUNNING, job.getCurrentState());
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        assertTrue(n.get() > 0); // At least one tuple was processed
        job.stateChange(Job.Action.CLOSE);
        assertEquals(Job.State.CLOSED, job.getCurrentState());
    }

    @Test
    public void jobPeriodicSourceCancellation() throws Exception {
        Topology t = newTopology();
        AtomicInteger n = new AtomicInteger(0);
        @SuppressWarnings("unused")
        TStream<Integer> ints = t.poll(() -> n.incrementAndGet(), 500, TimeUnit.MILLISECONDS);

        // Get the source oplet
        Collection<Vertex<? extends Oplet<?, ?>, ?, ?>> vertices = t.graph().getVertices();
        PeriodicSource<?> src = null;
        for (Vertex<? extends Oplet<?, ?>, ?, ?> v : vertices) {
            Oplet<?,?> op = v.getInstance();
            assertTrue(op instanceof PeriodicSource);
            src = (PeriodicSource<?>) op;
            assertEquals(500, src.getPeriod());
            assertSame(TimeUnit.MILLISECONDS, src.getUnit());
        }
        
        // Submit job
        Future<Job> fj = ((DirectProvider)getTopologyProvider()).submit(t);
        Job job = fj.get();
        assertEquals(Job.State.RUNNING, job.getCurrentState());
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        int tupleCount = n.get(); 
        assertTrue("Expected more tuples than "+ tupleCount, tupleCount > 0); // At least one tuple was processed
        
        // Changing the period cancels the source's task and schedules new one
        src.setPeriod(100); 

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        
        // More tuples processed after resetting the period
        assertTrue("Expected more tuples than "+ n.get(), n.get() > 3*tupleCount);

        job.stateChange(Job.Action.CLOSE);
        assertEquals(Job.State.CLOSED, job.getCurrentState());
    }

    @Test
    public void jobProcessSource() throws Exception {
        Topology t = newTopology();
        AtomicInteger n = new AtomicInteger(0);
        @SuppressWarnings("unused")
        TStream<Integer> ints = t.generate(() -> n.incrementAndGet());

        Future<Job> fj = ((DirectProvider)getTopologyProvider()).submit(t);
        Job job = fj.get();
        assertEquals(Job.State.RUNNING, job.getCurrentState());
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        assertTrue(n.get() > 0); // At least one tuple was processed
        job.stateChange(Job.Action.CLOSE);
        assertEquals(Job.State.CLOSED, job.getCurrentState());
    }

    @Test(expected = TimeoutException.class)
    public void jobTimesOut() throws Exception {
        Topology t = newTopology();
        AtomicInteger n = new AtomicInteger(0);
        @SuppressWarnings("unused")
        TStream<Integer> ints = t.poll(() -> n.incrementAndGet(), 100, TimeUnit.MILLISECONDS);

        Future<Job> fj = ((DirectProvider)getTopologyProvider()).submit(t);
        Job job = fj.get();
        assertEquals(Job.State.RUNNING, job.getCurrentState());
        try {
            job.complete(700, TimeUnit.MILLISECONDS);
        } finally {
            assertTrue(n.get() > 0); // At least one tuple was processed
            assertEquals(Job.State.RUNNING, job.getCurrentState());
        }
    }

    @Test(expected = ExecutionException.class)
    public void jobPeriodicSourceError() throws Exception {
        Topology t = newTopology();
        AtomicInteger n = new AtomicInteger(0);
        TStream<Integer> ints = t.poll(() -> n.incrementAndGet(), 100, TimeUnit.MILLISECONDS);
        ints.pipe(new FailedOplet<Integer>(5, 0));
        
        Future<Job> fj = ((DirectProvider)getTopologyProvider()).submit(t);
        Job job = fj.get();
        assertEquals(Job.State.RUNNING, job.getCurrentState());
        try {
            job.complete(10, TimeUnit.SECONDS); 
        } finally {
            // RUNNING even though execution error 
            assertEquals(Job.State.RUNNING, job.getCurrentState());
        }
    }

    @Test(expected = ExecutionException.class)
    public void jobProcessSourceError() throws Exception {
        Topology t = newTopology();
        AtomicInteger n = new AtomicInteger(0);
        TStream<Integer> ints = t.generate(() -> n.incrementAndGet());
        ints.pipe(new FailedOplet<Integer>(12, 100));

        Future<Job> fj = ((DirectProvider)getTopologyProvider()).submit(t);
        Job job = fj.get();
        assertEquals(Job.State.RUNNING, job.getCurrentState());
        try {
            job.complete(10, TimeUnit.SECONDS); 
        } finally {
            // RUNNING even though execution error 
            assertEquals(Job.State.RUNNING, job.getCurrentState());
        }
    }

    private Job awaitCompleteExecution(Topology t) throws InterruptedException, ExecutionException {
        return awaitCompleteExecution(t, null);
    }

    private Job awaitCompleteExecution(Topology t, JsonObject config) throws InterruptedException, ExecutionException {
        Future<Job> fj = ((DirectProvider)getTopologyProvider()).submit(t, config);
        Job job = fj.get();
        job.complete();
        return job;
    }

    /**
     * Test oplet which fails after receiving a given number of tuples.
     * @param <T>
     */
    @SuppressWarnings("serial")
    private static class FailedOplet<T> extends Pipe<T,T> {
        private final int threshold;
        private final int sleepMillis;
        
        /**
         * Create test oplet.
         * 
         * @param afterTuples number of tuples to receive before failing 
         * @param sleepMillis milliseconds of sleep before processing each tuple
         */
        public FailedOplet(int afterTuples, int sleepMillis) {
            if (afterTuples < 0)
                throw new IllegalArgumentException("afterTuples="+afterTuples);
            if (sleepMillis < 0)
                throw new IllegalArgumentException("sleepMillis="+sleepMillis);
            this.threshold = afterTuples;
            this.sleepMillis = sleepMillis;
        }

        @Override
        public void close() throws Exception {
        }
        @Override
        public void accept(T tuple) {
            if (sleepMillis > 0) {
                sleep(sleepMillis);
            }
            injectError(threshold); 
            submit(tuple);
        }

        private AtomicInteger count = new AtomicInteger(0);
        protected void injectError(int errorAt) {
            if (count.getAndIncrement() == errorAt)
                throw new RuntimeException("Expected Test Exception");
        }
        
        protected static void sleep(long millis) {
            try {
                Thread.sleep(millis);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private static class Configuration {
        private final JsonObject values = new JsonObject();

        Configuration addProperty(String name, String value) {
            values.addProperty(name, value);
            return this;
        }
        
        JsonObject json() {
            return values;
        }
    }
}
