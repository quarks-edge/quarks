/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.ws;

import java.util.Properties;

import quarks.function.Function;
import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.json.JsonFunctions;

/**
 * A connector for sending and receiving messages to a Web Socket Server.
 * <p>
 * Sample use:
 * <pre>{@code
 * // assuming a properties file containing at least:
 * // ws.url=ws://myWsServerHost
 *  
 * String propsPath = <path to properties file>; 
 * Properties properties = new Properties();
 * properties.load(Files.newBufferedReader(new File(propsPath).toPath()));
 *
 * Topology t = new DirectProvider();
 * WebSocketClient wsclient = new WebSocketClient(t, properties);
 * 
 * // send a stream's tuples as JSON to a Web Socket Server
 * TStream<JsonObject> s = ...;
 * wsclient.send(s, JsonFunctions.asString());
 * 
 * // create a stream of tuples from JSON received from a Web Socket Server
 * TStream<JsonObject> r = wsclient.receive(JsonFunctions.fromString());
 * r.print();
 * }</pre>
 */
public class WebSocketClient {

    /**
     * Create a new Web Socket Client connector.
     * <p>
     * Configuration parameters:
     * <ul>
     * <li>ws.url - "ws://host[:port]", "wss://host[:port]"
     *   the default port is 80 and 443 for "ws" and "wss" respectively</li>
     * <li>ws.trustStorePath - required for "wss:"</li>
     * <li>ws.trustStorePassword - required for "wss:"</li>
     * <li>ws.keyStorePath - required for "wss:" if server does client auth</li>
     * <li>ws.keyStorePassword - required for "wss:" if server does client auth</li>
     * <li>ws.keyPassword - defaults to ws.keyStorePassword value</li>
     * <li>ws.keyCertificateAlias - alias for certificate in key store. defaults to "default"</li>
     * </ul>
     * @param config the connector's configuration
     */
    public WebSocketClient(Topology t, Properties config) {
    }

    /**
     * Send a stream's String tuples as a String payload.
     * @param stream the stream
     * @return sink
     */
    public TSink<String> send(TStream<String> stream) {
        return send(stream, tuple -> tuple);
    }

    /**
     * Send a stream's tuples as a String payload.
     * @param stream the stream
     * @param toPayload function to create the payload from a tuple.
     * @return sink
     * @see JsonFunctions#asString()
     */
    public <T> TSink<T> send(TStream<T> stream, Function<T,String> toPayload) {
        return null; // TODO
    }

    /**
     * Send a stream's tuples as a byte[] payload.
     * @param stream the stream
     * @param toPayload function to create the payload from a tuple.
     * @return sink
     * @see JsonFunctions#asBytes()
     */
    public <T> TSink<T> sendBytes(TStream<T> stream, Function<T,byte[]> toPayload) {
        return null; // TODO
    }
    
    /**
     * Create a stream of String tuples from received String payload messages.
     * @return the stream
     */
    public TStream<String> receive() {
    	return receive(payload -> payload);
    }
    
    /**
     * Create a stream of tuples from received String payload messages.
     * 
     * @param toTuple function to create a tuple from the payload
     * @return the stream
     * @see JsonFunctions#fromString()
     */
    public <T> TStream<T> receive(Function<String,T> toTuple) {
        return null; // TODO
    }
    
    /**
     * Create a stream of tuples from received byte[] payload messages.
     * 
     * @param toTuple function to create a tuple from the payload
     * @return the stream
     * @see JsonFunctions#fromBytes()
     */
    public <T> TStream<T> receiveBytes(Function<byte[],T> toTuple) {
        return null; // TODO
    }

}
