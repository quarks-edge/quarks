/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.functional;

import static quarks.function.Functions.closeFunction;

import java.util.concurrent.TimeUnit;

import quarks.function.Supplier;
import quarks.oplet.OpletContext;
import quarks.oplet.core.PeriodicSource;

public class SupplierPeriodicSource<T> extends PeriodicSource<T> {

    private Supplier<T> data;

    public SupplierPeriodicSource(long period, TimeUnit unit, Supplier<T> data) {
        super(period, unit);
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
    public void fetchTuples() {
        T tuple = data.get();
        if (tuple != null)
            submit(tuple);
    }
}
