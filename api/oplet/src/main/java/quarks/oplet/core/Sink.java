/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.core;

import static quarks.function.Functions.closeFunction;

import java.util.Collections;
import java.util.List;

import quarks.function.Consumer;

/**
 * Sink a stream by processing each tuple through
 * a {@link Consumer}.
 * If the {@code sinker} function implements {@code AutoCloseable}
 * then when this oplet is closed {@code sinker.close()} is called.
 *
 * @param <T> Tuple type.
 */
public class Sink<T> extends AbstractOplet<T, Void> {
    
    private Consumer<T> sinker;

    /**
     * Create a {@code Sink} oplet.
     * @param sinker Processing to be performed on each tuple.
     */
    public Sink(Consumer<T> sinker) {
        this.sinker = sinker;
    }

    @Override
    public List<Consumer<T>> getInputs() {
        return Collections.singletonList(sinker);
    }

    @Override
    public void start() {
    }
    
    @Override
    public void close() throws Exception {
        closeFunction(sinker);
    }
}
