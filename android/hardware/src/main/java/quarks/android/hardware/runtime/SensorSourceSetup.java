/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.android.hardware.runtime;

import quarks.function.Consumer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

/**
 * 
 */
public class SensorSourceSetup implements Consumer<Consumer<SensorEvent>> {
    private static final long serialVersionUID = 1L;
    
    private final SensorManager mSensorManager;
    private final Sensor[] sensors;
    private final int samplingPeriodUs;
    private SensorChangeEvents events;
    

    public SensorSourceSetup(SensorManager mSensorManager, int samplingPeriodUs,
            Sensor ... sensors) {
        this.mSensorManager = mSensorManager;
        this.sensors = sensors;
        this.samplingPeriodUs = samplingPeriodUs;     
    }
    public SensorSourceSetup(SensorManager mSensorManager, Sensor ... sensors) {
        this(mSensorManager , SensorManager.SENSOR_DELAY_NORMAL, sensors);  
    }

    public void accept(Consumer<SensorEvent> submitter) {
        events = new SensorChangeEvents(submitter);
        for (Sensor sensor : sensors)
            mSensorManager.registerListener(events, sensor, samplingPeriodUs);
    }
}
