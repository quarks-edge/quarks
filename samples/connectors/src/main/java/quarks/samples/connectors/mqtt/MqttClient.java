/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.mqtt;

import quarks.samples.connectors.Options;

/**
 * Demonstrate integrating with the MQTT messaging system
 * <a href="http://mqtt.org">http://mqtt.org</a>.
 * <p>
 * {@link quarks.connectors.mqtt.MqttStreams MqttStreams} is
 * a connector used to create a bridge between topology streams
 * and an MQTT broker.
 * <p>
 * The client either publishes some messages to a MQTT topic  
 * or subscribes to the topic and reports the messages received.
 * <p>
 * By default, a running MQTT broker with the following
 * characteristics is assumed:
 * <ul>
 * <li>the broker's connection is {@code tcp://localhost:1883}</li>
 * <li>the broker is configured for no authentication</li>
 * </ul>
 * <p>
 * See the MQTT link above for information about setting up a MQTT broker.
 * <p>
 * This may be executed as:
 * <UL>
 * <LI>
 * {@code java -cp samples/lib/quarks.samples.connectors.mqtt.jar
 *  quarks.samples.connectors.mqtt.MqttClient -h
 * } - Run directly from the command line.
 * </LI>
 * Specify absolute pathnames if using the {@code trustStore}
 * or {@code keyStore} arguments.
 * </UL>
 * <LI>
 * An application execution within your IDE once you set the class path to include the correct jars.</LI>
 * </UL>
 */
public class MqttClient {
    private static final String usage = "usage: "
            + "\n" + "[-v] [-h]"
            + "\n" + "pub | sub"
            + "\n" + "[serverURI=<value>]"
            + "\n" + "[clientId=<value>]"
            + "\n" + "[cleanSession=<true|false>]"
            + "\n" + "[topic=<value>] [qos=<value>]"
            + "\n" + "[retain]"
            + "\n" + "[pubcnt=<value>]"
            + "\n" + "[cnTimeout=<value>]"
            + "\n" + "[actionTimeoutMillis=<value>]"
            + "\n" + "[idleTimeout=<value>]"
            + "\n" + "[idleReconnectInterval=<value>]"
            + "\n" + "[userID=<value>] [password=<value>]"
            + "\n" + "[trustStore=<value>] [trustStorePassword=<value>]"
            + "\n" + "[keyStore=<value>] [keyStorePassword=<value>]"
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

        if (options.get(OPT_PASSWORD) != null)
            options.put(OPT_USER_ID, options.get(OPT_USER_ID, System.getProperty("user.name")));
        
        String[] announceOpts = new String[] {
                OPT_USER_ID,
                OPT_PASSWORD,
                OPT_TRUST_STORE,
                OPT_TRUST_STORE_PASSWORD,
                OPT_KEY_STORE,
                OPT_KEY_STORE_PASSWORD
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
    static final String OPT_SERVER_URI = "serverURI";
    static final String OPT_CLIENT_ID = "clientId";
    static final String OPT_CN_TIMEOUT_SEC = "cnTimeout";
    static final String OPT_ACTION_TIMEOUT_MILLIS = "actionTimeoutMillis";
    static final String OPT_QOS = "qos";
    static final String OPT_TOPIC = "topic";
    static final String OPT_CLEAN_SESSION = "cleanSession";
    static final String OPT_RETAIN = "retain";
    static final String OPT_USER_ID = "userID";
    static final String OPT_PASSWORD = "password";
    static final String OPT_TRUST_STORE = "trustStore";
    static final String OPT_TRUST_STORE_PASSWORD = "trustStorePassword";
    static final String OPT_KEY_STORE = "keyStore";
    static final String OPT_KEY_STORE_PASSWORD = "keyStorePassword";
    static final String OPT_PUB_CNT = "pubcnt";
    static final String OPT_IDLE_TIMEOUT_SEC = "idleTimeout";
    static final String OPT_IDLE_RECONNECT_INTERVAL_SEC = "idleReconnectInterval";
    
    private static void initHandlers(Options opts) {
        // options for which we have a default
        opts.addHandler(OPT_HELP, null, false);
        opts.addHandler(OPT_VERBOSE, null, false);
        opts.addHandler(OPT_PUB, null, false);
        opts.addHandler(OPT_SUB, null, false);
        opts.addHandler(OPT_SERVER_URI, v -> v, "tcp://localhost:1883");
        opts.addHandler(OPT_TOPIC, v -> v, "mqttSampleTopic");
        opts.addHandler(OPT_RETAIN, null, false);
        opts.addHandler(OPT_PUB_CNT, v -> Integer.valueOf(v), -1);
        opts.addHandler(OPT_QOS, v -> Integer.valueOf(v), 0);

        // optional options (no default value)
        opts.addHandler(OPT_CLIENT_ID, v -> v);
        opts.addHandler(OPT_CN_TIMEOUT_SEC, v -> Integer.valueOf(v));
        opts.addHandler(OPT_ACTION_TIMEOUT_MILLIS, v -> Long.valueOf(v));
        opts.addHandler(OPT_CLEAN_SESSION, v -> Boolean.valueOf(v));
        opts.addHandler(OPT_USER_ID, v -> v);
        opts.addHandler(OPT_PASSWORD, v -> v);
        opts.addHandler(OPT_TRUST_STORE, v -> v);
        opts.addHandler(OPT_TRUST_STORE_PASSWORD, v -> v);
        opts.addHandler(OPT_KEY_STORE, v -> v);
        opts.addHandler(OPT_KEY_STORE_PASSWORD, v -> v);
        opts.addHandler(OPT_IDLE_TIMEOUT_SEC, v -> Integer.valueOf(v));
        opts.addHandler(OPT_IDLE_RECONNECT_INTERVAL_SEC, v -> Integer.valueOf(v));
    }
    
}
