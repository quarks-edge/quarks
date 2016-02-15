/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.functional;

import static quarks.function.Functions.closeFunction;

import quarks.function.Supplier;
import quarks.oplet.OpletContext;
import quarks.oplet.core.ProcessSource;

public class SupplierSource<T> extends ProcessSource<T> {

    private Supplier<Iterable<T>> data;

    public SupplierSource() {
    }

    public SupplierSource(Supplier<Iterable<T>> data) {
        this.data = data;
    }

    @Override
    public void initialize(OpletContext<Void, T> context) {
        super.initialize(context);
    }

    @Override
    public void close() throws Exception {
        closeFunction(data);
    }

    @Override
    public void process() {
        for (T tuple : data.get()) {
            if (tuple != null)
                submit(tuple);

            if (Thread.currentThread().isInterrupted())
                break;
        }
    }
}
