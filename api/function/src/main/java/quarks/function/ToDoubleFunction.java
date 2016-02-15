/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.function;

/**
 * Function that returns a double primitive.
 *
 * @param <T> Type of function argument.
 */
public interface ToDoubleFunction<T> {
    /**
     * Apply a function to {@code value}.
     * @param value Value the function is applied to
     * @return Result of the function against {@code value}.
     */
    double applyAsDouble(T value);
}
