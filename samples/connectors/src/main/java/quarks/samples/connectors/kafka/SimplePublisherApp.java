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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import quarks.connectors.kafka.KafkaProducer;
import quarks.console.server.HttpServer;
import quarks.providers.development.DevelopmentProvider;
import quarks.samples.connectors.Util;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * A simple Kafka publisher topology application.
 */
public class SimplePublisherApp {
    private final Properties props;
    private final String topic;

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to kafka.properties file");
        SimplePublisherApp publisher = new SimplePublisherApp(args[0]);
        publisher.run();
    }

    /**
     * @param kafkaPropsPath pathname to properties file
     */
    SimplePublisherApp(String kafkaPropsPath) throws Exception {
        props = new Properties();
        props.load(Files.newBufferedReader(new File(kafkaPropsPath).toPath()));
        topic = props.getProperty("topic");
    }
    
    private Map<String,Object> createKafkaConfig() {
        Map<String,Object> kafkaConfig = new HashMap<>();
        kafkaConfig.put("bootstrap.servers", props.get("bootstrap.servers"));
        return kafkaConfig;
    }
    
    /**
     * Create a topology for the publisher application and run it.
     */
    private void run() throws Exception {
        DevelopmentProvider tp = new DevelopmentProvider();
        
        // build the application/topology
        
        Topology t = tp.newTopology("kafkaSamplePublisher");

        // Create the Kafka Producer broker connector
        Map<String,Object> kafkaConfig = createKafkaConfig();
        KafkaProducer kafka = new KafkaProducer(t, () -> kafkaConfig);
        
        // Create a sample stream of tuples to publish
        AtomicInteger cnt = new AtomicInteger();
        TStream<String> msgs = t.poll(
                () -> {
                    String msg = String.format("Message-%d from %s",
                            cnt.incrementAndGet(), Util.simpleTS());
                    System.out.println("poll generated msg to publish: " + msg);
                    return msg;
                }, 1L, TimeUnit.SECONDS);
        
        // Publish the stream to the topic.  The String tuple is the message value.
        kafka.publish(msgs, topic);
        
        // run the application / topology
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(t);
    }

}
