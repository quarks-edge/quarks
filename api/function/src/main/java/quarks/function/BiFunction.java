/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.function;

import java.io.Serializable;

/**
 * Function that takes two arguments and returns a value.
 *
 * @param <T> Type of function's first argument
 * @param <U> Type of function's second argument
 * @param <R> Type of function return.
 */
public interface BiFunction<T, U, R> extends Serializable {
    
    /**
     * Apply a function to {@code t} and {@code u}.
     * @param t First argument the function is applied to.
     * @param u Second argument the function is applied to.
     * @return Result of the function against {@code t} and {@code u}.
     */
    R apply(T t, U u);
}
