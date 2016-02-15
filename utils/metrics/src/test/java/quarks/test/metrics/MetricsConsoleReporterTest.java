/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.metrics;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import quarks.execution.DirectSubmitter;
import quarks.metrics.Metrics;
import quarks.metrics.MetricsSetup;
import quarks.test.topology.TopologyAbstractTest;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.tester.Condition;

@Ignore("Test disabled until we figure out Metrics naming and default reporters")
public abstract class MetricsConsoleReporterTest extends TopologyAbstractTest {
    
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream output = new PrintStream(bytes);
    private final ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry)
            .outputTo(output)
            .formattedFor(Locale.US)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .build();

    // Register Metrics service before each test.
    @Before
    public void createMetricRegistry() {
        MetricsSetup.withRegistry(((DirectSubmitter<?,?>)getSubmitter()).getServices(), metricRegistry);
        // Don't start reporter thread as report is generated inside the test method
        // reporter.start(1, TimeUnit.SECONDS);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void reportsCounterValues() throws Exception {
        Topology t = newTopology();
        String[] data = new String[] {"a"};
        TStream<String> s = t.strings(data);
        s = Metrics.counter(s);

        waitUntilComplete(t, s, data);

        reporter.report(this.<Gauge>map(),
                        this.metricRegistry.getCounters(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        assertEquals(
                linesToString(  
                        "-- Counters --------------------------------------------------------------------",
                        "XXX.CounterOp",
                        "             count = 1",
                        "",
                        ""
                ),
                lines(2, consoleOutput()));
    }
    
    private <T> SortedMap<String, T> map() {
        return new TreeMap<String, T>();
    }

    private String consoleOutput() throws UnsupportedEncodingException {
        return bytes.toString("UTF-8");
    }

    /**
     * Return substring starting with the line specified by startLine 
     * (0-based).
     */
    private String lines(int startLine, String s) {
        String lines[] = s.split("\\r?\\n");
        return linesToString(startLine, lines);
    }

    /**
     * Coalesce the given lines into a single string starting with the 
     * line specified by startLine (0-based).
     */
    private String linesToString(int startLine, String... lines) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i >= startLine) {
                String line = lines[i];
                builder.append(line).append(String.format("%n"));
            }
        }
        return builder.toString();
    }

    private String linesToString(String... lines) {
        return linesToString(0, lines);
    }

    private void waitUntilComplete(Topology t, TStream<String> s, String[] data) throws Exception {
        Condition<Long> tc = t.getTester().tupleCount(s, data.length);
        complete(t, tc);
    }
}
