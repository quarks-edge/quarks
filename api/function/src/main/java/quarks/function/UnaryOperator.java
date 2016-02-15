/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.function;

/**
 * Function that returns the same type as its argument.
 *
 * @param <T> Type of function argument and return.
 */
public interface UnaryOperator<T> extends Function<T, T> {
}
