/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology.plumbing;

import java.util.concurrent.TimeUnit;

import quarks.function.Function;
import quarks.oplet.plumbing.Isolate;
import quarks.oplet.plumbing.PressureReliever;
import quarks.oplet.plumbing.UnorderedIsolate;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * Plumbing utilities for {@link TStream}.
 * Methods that manipulate the flow of tuples in a streaming topology,
 * but are not part of the logic of the application.
 */
public class PlumbingStreams {
    
    /**
     * Insert a blocking delay between tuples.
     * Returned stream is the input stream delayed by {@code delay}.
     * <p>
     * Delays less than 1msec are translated to a 0 delay.
     * <p>
     * This function always adds the {@code delay} amount after receiving
     * a tuple before forwarding it.  
     * <p>
     * Downstream tuple processing delays will affect
     * the overall delay of a subsequent tuple.
     * <p>
     * e.g., the input stream contains two tuples t1 and t2 and
     * the delay is 100ms.  The forwarding of t1 is delayed by 100ms.
     * Then if a downstream processing delay of 80ms occurs, this function
     * receives t2 80ms after it forwarded t1 and it will delay another
     * 100ms before forwarding t2.  Hence the overall delay between forwarding
     * t1 and t2 is 180ms.
     * See {@link #blockingThrottle(long, TimeUnit) blockingThrottle}.
     * 
     * @param stream Stream t
     * @param delay Amount of time to delay a tuple.
     * @param unit Time unit for {@code delay}.
     * 
     * @return Stream that will be delayed.
     */
    public static <T> TStream<T> blockingDelay(TStream<T> stream, long delay, TimeUnit unit) {
        return stream.map(t -> {try {
            Thread.sleep(unit.toMillis(delay));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } return t;}) ;
    }
    
    /**
     * Maintain a constant blocking delay between tuples.
     * The returned stream is the input stream throttled by {@code delay}.
     * <p>
     * Delays less than 1msec are translated to a 0 delay.
     * <p>
     * Sample use:
     * <pre>{@code
     * TStream<String> stream = topology.strings("a", "b, "c");
     * // Create a stream with tuples throttled to 1 second intervals.
     * TStream<String> throttledStream = blockingThrottle(stream, 1, TimeUnit.SECOND);
     * // print out the throttled tuples as they arrive
     * throttledStream.peek(t -> System.out.println(new Date() + " - " + t));
     * }</pre>
     * <p>
     * The function adjusts for downstream processing delays.
     * The first tuple is not delayed.  If {@code delay} has already
     * elapsed since the prior tuple was forwarded, the tuple 
     * is forwarded immediately.
     * Otherwise, forwarding the tuple is delayed to achieve
     * a {@code delay} amount since forwarding the prior tuple.
     * <p>
     * e.g., the input stream contains two tuples t1 and t2 and
     * the delay is 100ms.  The forwarding of t1 is delayed by 100ms.
     * Then if a downstream processing delay of 80ms occurs, this function
     * receives t2 80ms after it forwarded t1 and it will only delay another
     * 20ms (100ms - 80ms) before forwarding t2.  
     * Hence the overall delay between forwarding t1 and t2 remains 100ms.
     * 
     * @param <T> tuple type
     * @param stream the stream to throttle
     * @param delay Amount of time to delay a tuple.
     * @param unit Time unit for {@code delay}.
     * @return the throttled stream
     */
    public static <T> TStream<T> blockingThrottle(TStream<T> stream, long delay, TimeUnit unit) {
        return stream.map( blockingThrottle(delay, unit) );
    }

    private static <T> Function<T,T> blockingThrottle(long delay, TimeUnit unit) {
        long[] nextTupleTime = { 0 };
        return t -> {
            long now = System.currentTimeMillis();
            if (nextTupleTime[0] != 0) {
                if (now < nextTupleTime[0]) {
                    try {
                        Thread.sleep(nextTupleTime[0] - now);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    now = System.currentTimeMillis();
                }
            }
            nextTupleTime[0] = now + unit.toMillis(delay);
            return t;
        };
    }
    
    /**
     * Insert a blocking delay before forwarding the first tuple and
     * no delay for subsequent tuples.
     * <p>
     * Delays less than 1msec are translated to a 0 delay.
     * <p>
     * Sample use:
     * <pre>{@code
     * TStream<String> stream = topology.strings("a", "b, "c");
     * // create a stream where the first tuple is delayed by 5 seconds. 
     * TStream<String> oneShotDelayedStream =
     *      stream.map( blockingOneShotDelay(5, TimeUnit.SECONDS) );
     * }</pre>
     * 
     * @param <T> tuple type
     * @param stream input stream
     * @param delay Amount of time to delay a tuple.
     * @param unit Time unit for {@code delay}.
     * @return the delayed stream
     */
    public static <T> TStream<T> blockingOneShotDelay(TStream<T> stream, long delay, TimeUnit unit) {
        return stream.map( blockingOneShotDelay(delay, unit) );
    }

    private static <T> Function<T,T> blockingOneShotDelay(long delay, TimeUnit unit) {
        long[] initialDelay = { unit.toMillis(delay) };
        return t -> {
            if (initialDelay[0] != -1) {
                try {
                    Thread.sleep(initialDelay[0]);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                initialDelay[0] = -1;
            }
            return t;
            };
    }
    
    /**
     * Relieve pressure on upstream processing by discarding tuples.
     * This method ensures that upstream processing is not
     * constrained by any delay in downstream processing,
     * for example by a connector not being able to connect
     * to its external system.
     * <P>
     * Any downstream processing of the returned stream is isolated
     * from {@code stream} so that any slow down does not affect {@code stream}.
     * When the downstream processing cannot keep up with rate of
     * {@code stream} tuples will be dropped from returned stream.
     * <BR>
     * Up to {@code count} of the most recent tuples per key from {@code stream}
     * are maintained when downstream processing is slow, any older tuples
     * that have not been submitted to the returned stream will be discarded.
     * <BR>
     * Tuple order is maintained within a partition but is not guaranteed to
     * be maintained across partitions.
     * </P>
     * 
     * @param stream Stream to be isolated from downstream processing.
     * @param keyFunction Function defining the key of each tuple.
     * @param count Maximum number of tuples to maintain when downstream processing is backing up.
     * @return Stream that is isolated from and thus relieves pressure on {@code stream}.
     * 
     * @param <T> Tuple type.
     * @param <K> Key type.
     */
    public static <T,K> TStream<T> pressureReliever(TStream<T> stream, Function<T,K> keyFunction, int count) {
        return stream.pipe(new PressureReliever<>(count, keyFunction));
    }
    
    /**
     * Isolate upstream processing from downstream processing.
     * <BR>
     * Implementations may throw {@code OutOfMemoryExceptions} 
     * if the processing against returned stream cannot keep up
     * with the arrival rate of tuples on {@code stream}.
     *  
     * @param stream Stream to be isolated from downstream processing.
     * @param ordered {@code true} to maintain arrival order on the returned stream,
     * {@code false} to not guaranteed arrival order.
     * @return Stream that is isolated from {@code stream}.
     */
    public static <T> TStream<T> isolate(TStream<T> stream, boolean ordered) {
        return stream.pipe(
                ordered ? new Isolate<T>() : new UnorderedIsolate<T>());
    }

}
