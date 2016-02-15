/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.kafka;

import static quarks.samples.connectors.kafka.KafkaClient.OPT_GROUP_ID;
import static quarks.samples.connectors.kafka.KafkaClient.OPT_TOPIC;
import static quarks.samples.connectors.kafka.KafkaClient.OPT_ZOOKEEPER_CONNECT;

import java.util.HashMap;
import java.util.Map;

import quarks.connectors.kafka.KafkaConsumer;
import quarks.samples.connectors.Options;
import quarks.samples.connectors.Util;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.TopologyProvider;

/**
 * A Kafka consumer/subscriber topology application.
 */
public class SubscriberApp {
    private final TopologyProvider tp;
    private final Options options;
    private final String uniq = Util.simpleTS();

    /**
     * @param top the TopologyProvider to use.
     * @param options
     */
    SubscriberApp(TopologyProvider tp, Options options) {
        this.tp = tp;
        this.options = options;
    }
    
    /**
     * Create a topology for the subscriber application.
     */
    public Topology buildAppTopology() {
        Topology t = tp.newTopology("kafkaClientSubscriber");

        // Create the KafkaConsumer broker connector
        Map<String,Object> config = newConfig(t);
        KafkaConsumer kafka = new KafkaConsumer(t, () -> config);
        
        System.out.println("Using Kafka consumer group.id "
                            + config.get(OPT_GROUP_ID));
        
        // Subscribe to the topic and create a stream of messages
        TStream<String> msgs = kafka.subscribe(rec -> rec.value(),
                                                (String)options.get(OPT_TOPIC));
        
        // Process the received msgs - just print them out
        msgs.sink(tuple -> System.out.println(
                String.format("[%s] received: %s", Util.simpleTS(), tuple)));
        
        return t;
    }
    
    private Map<String,Object> newConfig(Topology t) {
        Map<String,Object> config = new HashMap<>();
        // required kafka configuration items
        config.put("zookeeper.connect", options.get(OPT_ZOOKEEPER_CONNECT));
        config.put("group.id", options.get(OPT_GROUP_ID, newGroupId(t.getName())));
        return config;
    }
    
    private String newGroupId(String name) {
        // be insensitive to old consumers for the topic/groupId hanging around
        String groupId = name + "_" + uniq.replaceAll(":", "");
        return groupId;
    }
}
