/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.android.topology;

import android.app.Activity;
import quarks.android.oplet.RunOnUIThread;
import quarks.function.Consumer;
import quarks.function.Function;
import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.plumbing.PlumbingStreams;

/**
 * Stream utilities for an Android {@code Activity}.
 */
public class ActivityStreams {
    
    /**
    * Sink a stream executing the sinker function on the
    * activity's UI thread.
    * <BR>
    * For each tuple {@code t} on {@code stream}
    * the method {@code sinker.accept(t)} will be
    * called on the UI thread.
    *
    * @param activity Activity
    * @param stream Stream to be sinked.
    * @param sinker Function that will be executed on the UI thread.
    *
    * @see quarks.topology.TStream#sink(quarks.function.Consumer)
    */
    public static <T> TSink sinkOnUIThread(Activity activity, TStream<T> stream, Consumer<T> sinker) { 
        return stream.pipe(new RunOnUIThread<>(activity)).sink(sinker);
    }
    
    /**
    * Map tuples on a stream executing the mapper function on the
    * activity's UI thread.
    * <BR>
    * For each tuple {@code t} on {@code stream}
    * the method {@code mapper.apply(t)} will be
    * called on the UI thread. The return from the
    * method will be present on the returned stream
    * if it is not null. Any processing downstream
    * executed against the returned stream is executed
    * on a different thread to the UI thread.
    *
    * @param activity Activity
    * @param stream Stream to be sinked.
    * @param mapper Function that will be executed on the UI thread.
    * @param ordered True if tuple ordering must be maintained after the
    * execution on the UI thread. False if ordering is not required.
    *
    * @see quarks.topology.TStream#map(quarks.function.Function)
    */
    public static <T,U> TStream<U> mapOnUIThread(Activity activity, TStream<T> stream, Function<T,U> mapper, boolean ordered) {  
        
        // Switch to the UI thread
        stream = stream.pipe(new RunOnUIThread<>(activity));
        
        // execute the map on the UI thread
        TStream<U> resultStream = stream.map(mapper);
        
        // Switch back to a non-ui thread
        return PlumbingStreams.isolate(resultStream, ordered);
    }
}
