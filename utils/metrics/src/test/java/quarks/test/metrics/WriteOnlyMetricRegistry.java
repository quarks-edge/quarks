/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.metrics;

import com.codahale.metrics.MetricRegistry;

public class WriteOnlyMetricRegistry extends MetricRegistry {
    public boolean remove(String name) {
        return getMetrics().containsKey(name);
    }
}
