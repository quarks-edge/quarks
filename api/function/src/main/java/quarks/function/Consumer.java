/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.function;

import java.io.Serializable;

/**
 * Function that consumes a value.
 *
 * @param <T> Type of function argument.
 */
public interface Consumer<T> extends Serializable {
    
    /**
     * Apply the function to {@code value}.
     * @param value Value function is applied to.
     */
    void accept(T value);
}
