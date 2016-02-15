/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
package quarks.samples.apps.mqtt;

import java.util.Arrays;

import com.google.gson.JsonObject;

import quarks.connectors.mqtt.iot.MqttDevice;
import quarks.samples.apps.AbstractApplication;
import quarks.samples.apps.ApplicationUtilities;
import quarks.samples.apps.TopologyProviderFactory;
import quarks.topology.Topology;

/**
 * An MQTT Application base class.
 * <p>
 * Application instances need to:
 * <ul>
 * <li>define an implementation for {@link #buildTopology(Topology)}</li>
 * <li>call {@link #run()} to build and submit the topology for execution.</li>
 * </ul>
 * <p>
 * The class provides some common processing needs:
 * <ul>
 * <li>Support for an external configuration file</li>
 * <li>Provides a {@link TopologyProviderFactory}</li>
 * <li>Provides a {@link ApplicationUtilities}</li>
 * <li>Provides a {@link MqttDevice}</li>
 * </ul>
 */
public abstract class AbstractMqttApplication extends AbstractApplication {
    
    private MqttDevice mqttDevice;
    
    public AbstractMqttApplication(String propsPath) throws Exception {
        super(propsPath);
    }
    
    @Override
    protected void preBuildTopology(Topology t) {
        // Add an MQTT device communication manager to the topology
        updateTopicPrefix();
        mqttDevice = new MqttDevice(t, props);
        System.out.println("MqttDevice serverURLs " + Arrays.toString(mqttDevice.getMqttConfig().getServerURLs()));
        System.out.println("MqttDevice clientId " + mqttDevice.getMqttConfig().getClientId());
        System.out.println("MqttDevice deviceId " + props.getProperty("mqttDevice.id"));
        System.out.println("MqttDevice event topic pattern " + mqttDevice.eventTopic(null));
        System.out.println("MqttDevice command topic pattern " + mqttDevice.commandTopic(null));
    }
    
    /**
     * Get the application's MqttDevice
     * @return the MqttDevice
     */
    public MqttDevice mqttDevice() {
        return mqttDevice;
    }
    
    private void updateTopicPrefix() {
        String val = props.getProperty("mqttDevice.topic.prefix");
        if (val != null) {
            val = val.replace("{user.name}", System.getProperty("user.name"));
            val = val.replace("{application.name}", props.getProperty("application.name"));
            props.setProperty("mqttDevice.topic.prefix", val);
        }
    }
    
    /**
     * Compose a MqttDevice eventId for the sensor.
     * @param sensorId the sensor id
     * @param eventId the sensor's eventId
     * @return the device eventId
     */
    public String sensorEventId(String sensorId, String eventId) {
        return sensorId + "." + eventId;
    }
    
    /**
     * Compose a MqttDevice commandId for the sensor
     * @param sensorId the sensor id
     * @param commandId the sensor's commandId
     * @return the device commandId
     */
    public String commandId(String sensorId, String commandId) {
        return sensorId + "." + commandId;
    }
    
    /**
     * Extract a simple string valued command arg 
     * from a {@link MqttDevice#commands(String...)} returned
     * JsonObject tuple.
     * <p>
     * Interpret the JsonObject's embedded payload as a JsonObject with a single
     * "value" property.
     * @param jo the command tuple.
     * @return the command's argument value 
     */
    public String getCommandValueString(JsonObject jo) {
        return jo.get("payload").getAsJsonObject().get("value").getAsString();
    }

}
