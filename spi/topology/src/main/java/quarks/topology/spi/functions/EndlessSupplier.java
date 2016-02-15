/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology.spi.functions;

import java.util.Iterator;

import quarks.function.Functions;
import quarks.function.Supplier;
import quarks.function.WrappedFunction;

public class EndlessSupplier<T> extends WrappedFunction<Supplier<T>> implements Supplier<Iterable<T>> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public EndlessSupplier(Supplier<T> data) {
        super(Functions.synchronizedSupplier(data));
    }

    @Override
    public final Iterable<T> get() {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {

                return new Iterator<T>() {
                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public T next() {
                        return f().get();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };

            }

        };
    }
}
