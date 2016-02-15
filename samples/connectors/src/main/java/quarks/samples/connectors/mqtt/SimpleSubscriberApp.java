/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.mqtt;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import quarks.connectors.mqtt.MqttConfig;
import quarks.connectors.mqtt.MqttStreams;
import quarks.console.server.HttpServer;
import quarks.providers.development.DevelopmentProvider;
import quarks.samples.connectors.Util;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * A simple MQTT subscriber topology application.
 */
public class SimpleSubscriberApp {
    private final Properties props;
    private final String topic;

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to mqtt.properties file");
        SimpleSubscriberApp subscriber = new SimpleSubscriberApp(args[0]);
        subscriber.run();
    }

    /**
     * @param mqttPropsPath pathname to properties file
     */
    SimpleSubscriberApp(String mqttPropsPath) throws Exception {
        props = new Properties();
        props.load(Files.newBufferedReader(new File(mqttPropsPath).toPath()));
        topic = props.getProperty("topic");
    }
    
    private MqttConfig createMqttConfig() {
        MqttConfig mqttConfig = new MqttConfig(props.getProperty("serverURI"),
                                                null);
        // used if broker requires username/pw authentication
        mqttConfig.setUserName(props.getProperty("userID",
                                System.getProperty("user.name")));
        mqttConfig.setPassword(props.getProperty("password",
                                "myMosquittoPw").toCharArray());
        return mqttConfig;
    }
    
    /**
     * Create a topology for the subscriber application and run it.
     */
    private void run() throws Exception {
        DevelopmentProvider tp = new DevelopmentProvider();
        
        // build the application/topology
        
        Topology t = tp.newTopology("mqttSampleSubscriber");
        
        // Create the MQTT broker connector
        MqttConfig mqttConfig = createMqttConfig();
        MqttStreams mqtt = new MqttStreams(t, () -> mqttConfig);
        
        // Subscribe to the topic and create a stream of messages
        TStream<String> msgs = mqtt.subscribe(topic, 0/*qos*/);
        
        // Process the received msgs - just print them out
        msgs.sink(tuple -> System.out.println(
                String.format("[%s] received: %s", Util.simpleTS(), tuple)));
        
        // run the application / topology
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(t);
    }

}
