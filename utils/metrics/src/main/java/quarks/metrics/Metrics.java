/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.metrics;

import quarks.graph.Vertex;
import quarks.metrics.oplets.CounterOp;
import quarks.metrics.oplets.RateMeter;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * This interface contains utility methods for manipulating metrics.
 */
public class Metrics {
    /**
     * Increment a counter metric when peeking at each tuple.
     * 
     * @param <T>
     *            TStream tuple type
     * @return a {@link TStream} containing the input tuples
     */
    public static <T> TStream<T> counter(TStream<T> stream) {
        return stream.pipe(new CounterOp<T>());
    }

    /**
     * Measure current tuple throughput and calculate one-, five-, and
     * fifteen-minute exponentially-weighted moving averages.
     * 
     * @param <T>
     *            TStream tuple type
     * @return a {@link TStream} containing the input tuples
     */
    public static <T> TStream<T> rateMeter(TStream<T> stream) {
        return stream.pipe(new RateMeter<T>());
    }

    /**
     * Add counter metrics to all the topology's streams.
     * <p>
     * {@link CounterOp} oplets are inserted between every two graph
     * vertices with the following exceptions:
     * <ul>
     * <li>Oplets are only inserted upstream from a FanOut oplet.</li>
     * <li>If a chain of Peek oplets exists between oplets A and B, a Metric 
     * oplet is inserted after the last Peek, right upstream from oplet B.</li>
     * <li>If a chain a Peek oplets is followed by a FanOut, a metric oplet is 
     * inserted between the last Peek and the FanOut oplet.</li>
     * </ul>
     * The implementation is not idempotent: previously inserted metric oplets
     * are treated as regular graph vertices.  Calling the method twice 
     * will insert a new set of metric oplets into the graph.
     * @param t
     *            The topology
     */
    public static void counter(Topology t) {
        t.graph().peekAll( 
                () -> new CounterOp<>(),
                (Vertex<?, ?, ?> v) -> !(v.getInstance() instanceof quarks.oplet.core.FanOut));
    }
}
