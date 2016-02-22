/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.analytics.sensors;

import java.util.concurrent.TimeUnit;

import quarks.function.Function;
import quarks.function.Predicate;
import quarks.topology.TStream;

/**
 * Filters aimed at sensors.
 */
public class Filters {
	
	private Filters() {}

    /**
     * Deadband filter with maximum suppression time.
     * 
     * A filter that discards any tuples that are in the deadband, uninteresting to downstream consumers.
     * <P>
     * A tuple {@code t} is passed through the deadband filter if:
     * <UL>
     * <LI>
     * {@code inBand.test(value.apply(t))} is false, that is the tuple's value is outside of the deadband
     * </LI>
     * <LI>
     * OR {@code inBand.test(value.apply(t))} is true AND the last tuple's value was outside of the deadband.
     * This corresponds to the first tuple's value inside the deadband after a period being outside it.
     * </LI>
     * <LI>
     * OR it has been more than {@code maximumSuppression} seconds (in unit {@code unit}) 
     * </LI>
     * <LI>
     * OR it is the first tuple (effectively the state of the filter starts as outside of the deadband).
     * </LI>
     * </UL>
     * 
     * @param <T> Tuple type.
     * @param <V> Value type for the deadband function.
     * 
     * @param stream Stream containing readings.
     * @param value Function to obtain the tuple's value passed to the deadband function.
     * @param inBand Function that defines the deadband.
     * @param maximumSuppression Maximum amount of time to suppress values that are in the deadband.
     * @param unit Unit for {@code maximumSuppression}.
     * @return Filtered stream.
     */
    public static <T, V> TStream<T> deadband(TStream<T> stream, Function<T, V> value, Predicate<V> inBand,
            long maximumSuppression, TimeUnit unit) {

        return stream.filter(new Deadband<>(value, inBand, maximumSuppression, unit));
    }
    
    /**
     * Deadband filter.
     * 
     * A filter that discards any tuples that are in the deadband, uninteresting to downstream consumers.
     * <P>
     * A tuple {@code t} is passed through the deadband filter if:
     * <UL>
     * <LI>
     * {@code inBand.test(value.apply(t))} is false, that is the value is outside of the deadband
     * </LI>
     * <LI>
     * OR {@code inBand.test(value.apply(t))} is true and the last value was outside of the deadband.
     * This corresponds to the first value inside the deadband after a period being outside it.
     * </LI>
     * <LI>
     * OR it is the first tuple (effectively the state of the filter starts as outside of the deadband).
     * </LI>
     * </UL>
     * <P>
     * Here's an example of how {@code deadband()} would pass through tuples for a sequence of
     * values against the shaded dead band area. Circled values are ones that are passed through
     * the filter to the returned stream.
     * <BR>
     * <UL>
     * <LI>All tuples with a value outside the dead band.</LI>
     * <LI>Two tuples with values within the dead band that are the first time values return to being in band
     * after being outside of the dead band.</LI>
     * <LI>The first tuple.</LI>
     * </UL>
     * <BR>
     * <img src="doc-files/deadband.png" alt="Deadband example"/>
     * </P>
     * 
     * @param <T> Tuple type.
     * @param <V> Value type for the deadband function.
     * 
     * @param stream Stream containing readings.
     * @param value Function to obtain the value passed to the deadband function.
     * @param inBand Function that defines the deadband.
     * @return Filtered stream.
     */
    public static <T, V> TStream<T> deadband(TStream<T> stream, Function<T, V> value, Predicate<V> inBand) {

        return stream.filter(new Deadband<>(value, inBand));
    }
}
