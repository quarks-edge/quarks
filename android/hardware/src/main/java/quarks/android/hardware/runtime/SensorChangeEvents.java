/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.android.hardware.runtime;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import quarks.function.Consumer;

/**
 * Sensor event listener that submits sensor
 * change events as tuples using a Consumer.
 * 
 */
public class SensorChangeEvents implements SensorEventListener {
    private final Consumer<SensorEvent> eventSubmitter;
    
    /**
     * @param eventSubmitter How events are submitted to a stream.
     */
    public SensorChangeEvents(Consumer<SensorEvent> eventSubmitter) {
        this.eventSubmitter = eventSubmitter;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        eventSubmitter.accept(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
