/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.metrics.oplets;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;

/**
 * A metrics oplet which measures current tuple throughput and one-, five-, 
 * and fifteen-minute exponentially-weighted moving averages.
 */
public final class RateMeter<T> extends SingleMetricAbstractOplet<T> {

    public static final String METRIC_NAME = "TupleRateMeter";
    private static final long serialVersionUID = 3328912985808062552L;
    private final Meter meter;

    public RateMeter() {
        super(METRIC_NAME);
        this.meter = new Meter();
    }

    @Override
    protected void peek(T tuple) {
        meter.mark();
    }

    @Override
    protected Metric getMetric() {
        return meter;
    }
}
