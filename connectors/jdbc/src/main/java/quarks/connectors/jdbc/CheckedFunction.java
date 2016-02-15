/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.connectors.jdbc;

/**
 * Function to apply a funtion to an input value and return a result.
 *
 * @param <T> input stream tuple type
 * @param <R> result stream tuple type
 */
@FunctionalInterface
public interface CheckedFunction<T,R> {
    /**
     * Apply a function to {@code t} and return the result.
     * @param t input value
     * @return the function result.
     * @throws Exception if there are processing errors.
     */
    R apply(T t) throws Exception;
}
