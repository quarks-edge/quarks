/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.functional;


import static quarks.function.Functions.closeFunction;

import quarks.function.Consumer;

/**
 * Functional peek oplet.
 * 
 * Each peek calls {@code peeker.accept(tuple)}.
 *
 * @param <T> Tuple type.
 */
public class Peek<T> extends quarks.oplet.core.Peek<T> {
    private static final long serialVersionUID = 1L;
    private final Consumer<T> peeker;

    /**
     * Peek oplet using a function to peek.
     * @param peeker Function that peeks at the tuple.
     */
    public Peek(Consumer<T> peeker) {
        this.peeker = peeker;
    }

    @Override
    protected void peek(T tuple) {
        peeker.accept(tuple);
    }

    @Override
    public void close() throws Exception {
        closeFunction(peeker);
    }
}
