/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.android.oplet;

import android.app.Activity;
import quarks.oplet.core.Pipe;

public class RunOnUIThread<T> extends Pipe<T,T> {

    private static final long serialVersionUID = 1L;
    
    private final Activity activity;
    public RunOnUIThread(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void accept(T value) {       
        activity.runOnUiThread(() -> getDestination().accept(value));
    }

    @Override
    public void close() throws Exception {
    }
}
