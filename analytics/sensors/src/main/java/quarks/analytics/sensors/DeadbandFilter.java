/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.analytics.sensors;

import java.util.concurrent.TimeUnit;

import quarks.function.Function;
import quarks.function.Predicate;

/**
 * Deadband predicate function.
 *
 * @param <T> Tuple type.
 * @param <V> Value type for the deadband function.
 */
class DeadbandFilter<T, V> implements Predicate<T> {

    private static final long serialVersionUID = 1L;

    private final Function<T, V> valueFunction;
    private final Predicate<V> deadbandFunction;
    private final long period;
    private final TimeUnit unit;

    // Always send the first value.
    private transient boolean outOfBand = true;

    private transient long lastSend;
    
    DeadbandFilter(Function<T, V> valueFunction, Predicate<V> deadbandFunction) {
        this(valueFunction , deadbandFunction, 0, null);
    }

    DeadbandFilter(Function<T, V> valueFunction, Predicate<V> deadbandFunction, long period, TimeUnit unit) {
        this.valueFunction = valueFunction;
        this.deadbandFunction = deadbandFunction;
        this.period = period;
        this.unit = unit;
    }

    @Override
    public boolean test(final T t) {
        final V value = valueFunction.apply(t);
        boolean passTuple;
        long now = 0;
        if (!deadbandFunction.test(value)) {
            outOfBand = true;
            passTuple = true;
        } else if (outOfBand) {
            // When the value returns to being in-band
            // send the in-band value.
            outOfBand = false;
            passTuple = true;
        } else {
        	passTuple = false;
        	if (period != 0) {
                now = System.currentTimeMillis();
                long sinceLast = unit.convert(now - lastSend, TimeUnit.MILLISECONDS);
                if (sinceLast > period)
                     passTuple = true;
        	}
        }

        if (passTuple && period != 0)
            lastSend = now == 0 ? System.currentTimeMillis() : now;
            
        return passTuple;
    }

}

