/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.metrics.oplets;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;

/**
 * A metrics oplet which counts the number of tuples peeked at.
 */
public final class CounterOp<T> extends SingleMetricAbstractOplet<T> {

    public static final String METRIC_NAME = "TupleCounter";
    private final Counter counter;
    private static final long serialVersionUID = -6679532037136159885L;

    public CounterOp() {
        super(METRIC_NAME);
        this.counter = new Counter();
    }

    @Override
    protected void peek(T tuple) {
        counter.inc();
    }

    @Override
    protected Metric getMetric() {
        return counter;
    }
}
