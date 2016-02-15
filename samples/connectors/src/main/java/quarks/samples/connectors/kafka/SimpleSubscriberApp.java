/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.kafka;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import quarks.connectors.kafka.KafkaConsumer;
import quarks.console.server.HttpServer;
import quarks.providers.development.DevelopmentProvider;
import quarks.samples.connectors.Util;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * A simple Kafka subscriber topology application.
 */
public class SimpleSubscriberApp {
    private final Properties props;
    private final String topic;

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to kafka.properties file");
        SimpleSubscriberApp subscriber = new SimpleSubscriberApp(args[0]);
        subscriber.run();
    }

    /**
     * @param kafkaPropsPath pathname to properties file
     */
    SimpleSubscriberApp(String kafkaPropsPath) throws Exception {
        props = new Properties();
        props.load(Files.newBufferedReader(new File(kafkaPropsPath).toPath()));
        topic = props.getProperty("topic");
    }
    
    private Map<String,Object> createKafkaConfig() {
        Map<String,Object> kafkaConfig = new HashMap<>();
        kafkaConfig.put("zookeeper.connect", props.get("zookeeper.connect"));
        // for the sample, be insensitive to old/multiple consumers for
        // the topic/groupId hanging around
        kafkaConfig.put("group.id", 
                "kafkaSampleConsumer_" + Util.simpleTS().replaceAll(":", ""));
        return kafkaConfig;
    }
    
    /**
     * Create a topology for the subscriber application and run it.
     */
    private void run() throws Exception {
        DevelopmentProvider tp = new DevelopmentProvider();
        
        // build the application/topology
        
        Topology t = tp.newTopology("kafkaSampleSubscriber");
        
        // Create the Kafka Consumer broker connector
        Map<String,Object> kafkaConfig = createKafkaConfig();
        KafkaConsumer kafka = new KafkaConsumer(t, () -> kafkaConfig);
        
        // Subscribe to the topic and create a stream of messages
        TStream<String> msgs = kafka.subscribe(rec -> rec.value(), topic);
        
        // Process the received msgs - just print them out
        msgs.sink(tuple -> System.out.println(
                String.format("[%s] received: %s", Util.simpleTS(), tuple)));
        
        // run the application / topology
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(t);
    }

}
