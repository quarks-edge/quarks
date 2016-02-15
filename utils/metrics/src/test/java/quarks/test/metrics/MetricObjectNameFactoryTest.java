/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.test.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.SortedMap;

import javax.management.ObjectName;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.codahale.metrics.Counter;

import quarks.metrics.MetricObjectNameFactory;
import quarks.metrics.Metrics;
import quarks.topology.TStream;
import quarks.topology.Topology;

/*
 * Failures in these tests may indicate that job/oplet id prefixes defined
 * by the embedded runtime and those used by MetricObjectNameFactory got out 
 * of sync.
 */
@Ignore("abstract, provides common tests for concrete implementations")
public abstract class MetricObjectNameFactoryTest extends MetricsOnTest {
    private MetricObjectNameFactory factory;
    
    @Before
    public void createMetricObjectNameFactory() throws Exception {
        this.factory = new MetricObjectNameFactory();
    }
    

    @Test
    public void testCreateName() throws Exception {
        String name = counterName(new String[] {"a", "b", "c"});
        
        ObjectName on = factory.createName("counters", "domain", name);
        assertEquals("domain", on.getDomain());
        assertEquals(MetricObjectNameFactory.TYPE_PREFIX + ".counters", 
                on.getKeyProperty(MetricObjectNameFactory.KEY_TYPE));
        assertEquals(job.getId(), 
                on.getKeyProperty(MetricObjectNameFactory.KEY_JOBID));
        assertEquals(MetricObjectNameFactory.PREFIX_OPID + "1", 
                on.getKeyProperty(MetricObjectNameFactory.KEY_OPID));
    }

    @Test
    public void testCreateNameWithNoJobId() throws Exception {
        String name = counterName(new String[] {"a", "b", "c"});

        name = name.replace(MetricObjectNameFactory.PREFIX_JOBID, "XXX");
        ObjectName on = factory.createName("counters", "domain", name);
        assertEquals("domain", on.getDomain());
        assertEquals(MetricObjectNameFactory.TYPE_PREFIX + ".counters", 
                on.getKeyProperty(MetricObjectNameFactory.KEY_TYPE));
        assertTrue(on.getKeyProperty(MetricObjectNameFactory.KEY_JOBID) == null);
        assertEquals(MetricObjectNameFactory.PREFIX_OPID + "1", 
                on.getKeyProperty(MetricObjectNameFactory.KEY_OPID));
    }

    @Test
    public void testCreateNameWithNoOpId() throws Exception {
        String name = counterName(new String[] {"a", "b", "c"});
        name = name.replace(MetricObjectNameFactory.PREFIX_OPID, "XXX");
        ObjectName on = factory.createName("counters", "domain", name);
        assertEquals("domain", on.getDomain());
        assertEquals(MetricObjectNameFactory.TYPE_PREFIX + ".counters", 
                on.getKeyProperty(MetricObjectNameFactory.KEY_TYPE));
        assertTrue(on.getKeyProperty(MetricObjectNameFactory.KEY_OPID) == null);
        assertEquals(job.getId(), 
                on.getKeyProperty(MetricObjectNameFactory.KEY_JOBID));
    }

    private String counterName(String[] data) throws Exception {
        Topology t = newTopology();
        TStream<String> s = t.strings(data);
        s = Metrics.counter(s);

        waitUntilComplete(t, s, data);

        if (metricRegistry != null) {
            SortedMap<String, Counter> counters = metricRegistry.getCounters();
            assertEquals(1, counters.size());
            Set<String> names = metricRegistry.getNames();
            assertEquals(1, names.size());
            for (String name : names) {
                return name;
            }
        }
        return null;
    }
}
