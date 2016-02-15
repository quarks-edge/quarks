/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.function;

import java.io.Serializable;

/**
 * Consumer function that takes two arguments.
 */
public interface BiConsumer<T,U> extends Serializable {
    
    /**
     * Consume the two arguments.
     * @param t First function argument
     * @param u Second function argument
     */
    void accept(T t, U u);
}
