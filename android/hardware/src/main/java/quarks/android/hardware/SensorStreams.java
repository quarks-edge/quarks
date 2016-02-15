/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.android.hardware;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import quarks.android.hardware.runtime.SensorSourceSetup;
import quarks.topology.TStream;
import quarks.topology.TopologyElement;

/**
 * Create streams from sensors.
 *
 */
public class SensorStreams {
    
    /**
     * Declare a stream of sensor events.
     * A listener is registered with {@code sensorManager}
     * using {@code SensorManager.SENSOR_DELAY_NORMAL}.
     * Each sensor event will result in a tuple on
     * the returned stream.
     *
     * @param te Topology element for stream's topology
     * @param sensorManager Sensor manager
     * @param sensorTypes Which sensors to listen for. 
     * @return Stream that will contain events from the sensors.
     */
    public static TStream<SensorEvent> sensors(TopologyElement te, SensorManager sensorManager, int ... sensorTypes) {
        Sensor[] sensors = new Sensor[sensorTypes.length];
        
        for (int i = 0; i < sensorTypes.length; i++)
            sensors[i] = sensorManager.getDefaultSensor(sensorTypes[i]);
        
        return sensors(te, sensorManager, sensors);
    }
    
    /**
     * Declare a stream of sensor events.
     * A listener is registered with {@code sensorManager}
     * using {@code SensorManager.SENSOR_DELAY_NORMAL}.
     * Each sensor event will result in a tuple on
     * the returned stream.
     *
     * @param te Topology element for stream's topology
     * @param sensorManager Sensor manager
     * @param sensors Which sensors to listen for. 
     * @return Stream that will contain events from the sensors.
     */
    public static TStream<SensorEvent> sensors(TopologyElement te, SensorManager sensorManager, Sensor ... sensors) {        
        return te.topology().events(
                new SensorSourceSetup(sensorManager, sensors));
    } 
}
