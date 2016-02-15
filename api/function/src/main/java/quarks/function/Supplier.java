/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.function;

import java.io.Serializable;

/**
 * Function that supplies a value.
 * For example, functions that returns the current time:
 * <UL>
 * <LI>
 * As a lambda expression {@code () -> System.currentTimeMillis()}
 * </LI>
 * <LI>
 * As a method reference {@code () -> System::currentTimeMillis}
 * </LI>
 * </UL>
 *
 * @param <T> Type of function return.
 */
public interface Supplier<T> extends Serializable {
    /**
     * Supply a value, each call to this function may return
     * a different value.
     * @return Value supplied by this function.
     */
    T get();
}
