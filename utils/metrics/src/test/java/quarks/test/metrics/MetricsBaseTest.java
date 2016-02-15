/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.test.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.SortedMap;

import org.junit.Ignore;
import org.junit.Test;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import quarks.execution.Job;
import quarks.metrics.Metrics;
import quarks.test.topology.TopologyAbstractTest;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.tester.Condition;

/**
 * This will only work with an direct setup.
 */
@Ignore
public abstract class MetricsBaseTest extends TopologyAbstractTest {

    protected MetricRegistry metricRegistry;
    
    @Test
    public void counter() throws Exception {
        counter(new String[] {"a", "b", "c"});
    }

    @Test
    public void counterZeroTuples() throws Exception {
        counter(new String[0]);
    }

    @Test
    public void rateMeter() throws Exception {
        rateMeter(new String[] {"a", "b", "c"});
    }

    @Test
    public void rateMeterZeroTuples() throws Exception {
        rateMeter(new String[0]);
    }

    private final void counter(String[] data) throws Exception {
        Topology t = newTopology();
        TStream<String> s = t.strings(data);
        s = Metrics.counter(s);

        waitUntilComplete(t, s, data);

        if (metricRegistry != null) {
            SortedMap<String, Counter> counters = metricRegistry.getCounters();
            assertEquals(1, counters.size());
            Collection<Counter> values = counters.values();
            for (Counter v : values) {
                assertEquals(data.length, v.getCount());
            }
        }
    }

    private final void rateMeter(String[] data) throws Exception {
        Topology t = newTopology();
        TStream<String> s = t.strings(data);
        s = Metrics.rateMeter(s);

        waitUntilComplete(t, s, data);

        if (metricRegistry != null) {
            SortedMap<String, Meter> meters = metricRegistry.getMeters();
            assertEquals(1, meters.size());
            Collection<Meter> values = meters.values();
            for (Meter v : values) {
                assertEquals(data.length, v.getCount());
            }
        }
    }

    protected Job job;
    protected void waitUntilComplete(Topology t, TStream<String> s, String[] data) throws Exception {
        Condition<Long> tc = t.getTester().tupleCount(s, data.length);
        complete(t, tc);
        
        // Save the job.
        job = t.getTester().getJob();
        assertNotNull(job);
        
    }
}
