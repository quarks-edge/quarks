/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology;

/**
 * Termination point (sink) for a stream.
 *
 */
public interface TSink<T> extends TopologyElement {
    /**
     * Get the stream feeding this sink.
     * The returned reference may be used for
     * further processing of the feeder stream.
     * <BR>
     * For example, {@code s.print().filter(...)}
     * <BR>
     * Here the filter is applied
     * to {@code s} so that {@code s} feeds
     * the {@code print()} and {@code filter()}.
     * 
     * @return stream feeding this sink.
     */
    public TStream<T> getFeed();
}
