/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.function;

import java.io.Serializable;

/**
 * Predicate function.
 *
 * @param <T> Type of value to be tested.
 */
public interface Predicate<T> extends Serializable {
    
    /**
     * Test a value against a predicate.
     * @param value Value to be tested.
     * @return True if this predicate is true for {@code value} otherwise false.
     */
    boolean test(T value);
}
