/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.kafka;

import quarks.samples.connectors.Options;

/**
 * Demonstrate integrating with the Apache Kafka messaging system
 * <a href="http://kafka.apache.org">http://kafka.apache.org</a>.
 * <p>
 * {@link quarks.connectors.kafka.KafkaProducer KafkaProducer} is
 * a connector used to create a bridge between topology streams
 * and publishing to Kafka topics.
 * <p>
 * {@link quarks.connectors.kafka.KafkaConsumer KafkaConsumer} is
 * a connector used to create a bridge between topology streams
 * and subscribing to Kafka topics.
 * <p>
 * The client either publishes messages to a topic or   
 * subscribes to the topic and reports the messages received.
 * <p>
 * By default, a running Kafka cluster with the following
 * characteristics is assumed:
 * <ul>
 * <li>{@code bootstrap.servers="localhost:9092"}</li>
 * <li>{@code zookeeper.connect="localhost:2181"}</li>
 * <li>kafka topic {@code "kafkaSampleTopic"} exists</li>
 * </ul>
 * <p>
 * See the Apache Kafka link above for information about setting up a Kafka
 * cluster as well as creating a topic.
 * <p>
 * This may be executed from as:
 * <UL>
 * <LI>
 * {@code java -cp samples/lib/quarks.samples.connectors.kafka.jar
 *  quarks.samples.connectors.kafka.KafkaClient -h
 * } - Run directly from the command line.
 * </LI>
 * </UL>
 * <LI>
 * An application execution within your IDE once you set the class path to include the correct jars.</LI>
 * </UL>
 */
public class KafkaClient {
    private static final String usage = "usage: "
            + "\n" + "[-v] [-h]"
            + "\n" + "pub | sub"
            + "\n" + "[bootstrap.servers=<value>]"
            + "\n" + "[zookeeper.connect=<value>]"
            + "\n" + "[group.id=<value>]"
            + "\n" + "[pubcnt=<value>]"
            ;

    public static void main(String[] args) throws Exception {
        Options options = processArgs(args);
        if (options == null)
            return;

        Runner.run(options);
    }
     
    private static Options processArgs(String[] args) {
        Options options = new Options();
        initHandlers(options);
        try {
            options.processArgs(args);
        }
        catch (Exception e) {
            System.err.println(e);
            System.out.println(usage);
            return null;
        }
        
        if ((Boolean)options.get(OPT_HELP)) {
            System.out.println(usage);
            return null;
        }
        
        if (!(Boolean)options.get(OPT_PUB) && !(Boolean)options.get(OPT_SUB)) {
            System.err.println(String.format("Missing argument '%s' or '%s'.", OPT_PUB, OPT_SUB));
            System.out.println(usage);
            return null;
        }
        
        String[] announceOpts = new String[] {
        };
        if ((Boolean)options.get(OPT_VERBOSE))
            announceOpts = options.getAll().stream().map(e -> e.getKey()).toArray(String[]::new);
        for (String opt : announceOpts) {
            Object value = options.get(opt);
            if (value != null) {
                if (opt.toLowerCase().contains("password"))
                    value = "*****";
                System.out.println("Using "+opt+"="+value);
            }
        }
        
        return options;
    }
    
    static final String OPT_VERBOSE = "-v";
    static final String OPT_HELP = "-h";
    static final String OPT_PUB = "pub";
    static final String OPT_SUB = "sub";
    static final String OPT_BOOTSTRAP_SERVERS = "bootstrap.servers";
    static final String OPT_ZOOKEEPER_CONNECT = "zookeeper.connect";
    static final String OPT_GROUP_ID = "group.id";
    static final String OPT_TOPIC = "topic";
    static final String OPT_PUB_CNT = "pubcnt";
    
    private static void initHandlers(Options opts) {
        // options for which we have a default
        opts.addHandler(OPT_HELP, null, false);
        opts.addHandler(OPT_VERBOSE, null, false);
        opts.addHandler(OPT_PUB, null, false);
        opts.addHandler(OPT_SUB, null, false);
        opts.addHandler(OPT_BOOTSTRAP_SERVERS, v -> v, "localhost:9092");
        opts.addHandler(OPT_ZOOKEEPER_CONNECT, v -> v, "localhost:2181");
        opts.addHandler(OPT_TOPIC, v -> v, "kafkaSampleTopic");
        opts.addHandler(OPT_PUB_CNT, v -> Integer.valueOf(v), -1);

        // optional options (no default value)
        opts.addHandler(OPT_GROUP_ID, v -> v);
    }
    
}
