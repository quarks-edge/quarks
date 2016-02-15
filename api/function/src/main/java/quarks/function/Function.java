/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.function;

import java.io.Serializable;

/**
 * Single argument function.
 * For example:
 * <UL>
 * <LI>
 * A function that doubles a value {@code v -> v * 2}
 * </LI>
 * <LI>
 * A function that trims a {@code String} {@code v -> v.trim()} or {@code v -> String::trim}
 * </LI>
 * </UL>
 *
 * @param <T> Type of function argument.
 * @param <R> Type of function return.
 */
public interface Function<T, R> extends Serializable {
    
    /**
     * Apply a function to {@code value}.
     * @param value Value the function is applied to
     * @return Result of the function against {@code value}.
     */
    R apply(T value);
}
