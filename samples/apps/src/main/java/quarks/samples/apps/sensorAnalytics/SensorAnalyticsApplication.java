/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
package quarks.samples.apps.sensorAnalytics;

import quarks.samples.apps.mqtt.AbstractMqttApplication;
import quarks.topology.Topology;

/**
 * A sample application demonstrating some common sensor analytic processing
 * themes.
 */
public class SensorAnalyticsApplication extends AbstractMqttApplication {
    
    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to application properties file");
        
        SensorAnalyticsApplication application = new SensorAnalyticsApplication(args[0]);
        
        application.run();
    }
    
    /**
     * Create an application instance.
     * @param propsPath pathname to an application configuration file
     * @throws Exception
     */
    SensorAnalyticsApplication(String propsPath) throws Exception {
        super(propsPath);
    }
    
    @Override
    protected void buildTopology(Topology t) {
        
        // Add the "sensor1" analytics to the topology
        new Sensor1(t, this).addAnalytics();
        
        // TODO Add the "sensor2" analytics to the topology
        // TODO Add the "sensor3" analytics to the topology
    }
}
