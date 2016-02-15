/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.function;

import java.io.Serializable;

/**
 * Function that returns a int primitive.
 *
 * @param <T> Type of function argument.
 */
public interface ToIntFunction<T> extends Serializable {
    /**
     * Apply a function to {@code value}.
     * @param value Value the function is applied to
     * @return Result of the function against {@code value}.
     */
    int applyAsInt(T value);
}
