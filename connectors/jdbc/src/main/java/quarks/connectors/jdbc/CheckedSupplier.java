/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.connectors.jdbc;

/**
 * Function that supplies a result and may throw an Exception.
 *
 * @param <T> stream tuple type
 */
@FunctionalInterface
public interface CheckedSupplier<T> {
    /**
     * Get a result.
     * @return the result
     * @throws Exception if there are errors
     */
    T get() throws Exception;
}
