/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
package quarks.samples.apps.mqtt;

import com.google.gson.JsonObject;

import quarks.connectors.iot.QoS;
import quarks.connectors.mqtt.MqttStreams;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.json.JsonFunctions;

/**
 * An MQTT Device Communications client for watching device events
 * and sending commands.
 * <p>
 * This is an "application properties" aware client that gets MQTT configuration
 * from a Quarks sample app application configuration properties file.
 * <p>
 * This client avoids the need for other MQTT clients (e.g., from a mosquitto
 * installation) to observe and control the applications.
 */
public class DeviceCommsApp extends AbstractMqttApplication {
    
    private static final String usage = "Usage: watch | send <cmdLabel> <cmdArg>";

    private String mode;
    private String cmdLabel;
    private String cmdArg;
    
    public static void main(String[] args) throws Exception {
        if (args.length < 1)
            throw new Exception("missing pathname to application properties file");
        
        try {
            int i = 0;
            DeviceCommsApp application = new DeviceCommsApp(args[i++]);
            String mode = args[i++];
            if (!("watch".equals(mode) || "send".equals(mode))) {
                throw new IllegalArgumentException("Unsupport mode: "+application.mode);
            }
            application.mode = mode;
            if (application.mode.equals("send")) {
                application.cmdLabel = args[i++];
                application.cmdArg = args[i++];
            }
        
            application.run();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e.getMessage()
                    +"\n"+usage);
        }
    }
    
    /**
     * Create an application instance.
     * @param propsPath pathname to an application configuration file
     * @throws Exception
     */
    DeviceCommsApp(String propsPath) throws Exception {
        super(propsPath);
    }
    
    @Override
    protected void buildTopology(Topology t) {
        mqttDevice().getMqttConfig().setClientId(null);
        MqttStreams mqtt = new MqttStreams(t, () -> mqttDevice().getMqttConfig());
        if (mode.equals("send")) {
            String topic = mqttDevice().commandTopic(cmdLabel);
            JsonObject jo = new JsonObject();
            jo.addProperty("value", cmdArg);
            System.out.println("Publishing command: topic="+topic+"  value="+jo);
            TStream<String> cmd = t.strings(JsonFunctions.asString().apply(jo));
            mqtt.publish(cmd, topic, QoS.FIRE_AND_FORGET, false/*retain*/);
            // Hmm... the paho MQTT *non-daemon* threads prevent the app
            // from exiting after returning from main() following job submit().
            // Lacking MqttStreams.shutdown() or such...
            // Delay a bit and then explicitly exit().  Ugh.
            cmd.sink(tuple -> { 
                try {
                    Thread.sleep(3*1000);
                } catch (Exception e) { }
                System.exit(0); });
        }
        else if (mode.equals("watch")) {
            String topicFilter = mqttDevice().eventTopic(null);
            System.out.println("Watching topic filter "+topicFilter);
            TStream<String> events = mqtt.subscribe(topicFilter, QoS.FIRE_AND_FORGET,
                    (topic,payload) -> { 
                        String s = "\n# topic "+topic;
                        s += "\n" + new String(payload);
                        return s;
                    });
            events.print();
        }
    }
}
