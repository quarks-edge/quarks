/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.metrics;

import org.junit.Before;
import org.junit.Ignore;

import quarks.execution.DirectSubmitter;
import quarks.metrics.MetricsSetup;

@Ignore("abstract, provides common tests for concrete implementations")
public abstract class MetricsOnTest extends MetricsBaseTest {
    
    // Register Metrics service before each test.
    @Before
    public void createMetricRegistry() {
        metricRegistry = new WriteOnlyMetricRegistry();
        MetricsSetup.withRegistry(((DirectSubmitter<?,?>)getSubmitter()).getServices(), metricRegistry);
        // Don't start reporter thread as report is generated inside the test method
        // reporter.start(1, TimeUnit.SECONDS);
    }
}
