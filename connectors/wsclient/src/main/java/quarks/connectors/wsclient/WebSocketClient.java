/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.wsclient;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import com.google.gson.JsonObject;

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
 * Topology t = ...;
 * WebSocketClient wsclient = new WebSocketClient(t, properties);
 * 
 * // send a stream's JsonObject tuples as JSON to a Web Socket Server
 * TStream<JsonObject> s = ...;
 * wsclient.send(s);
 * 
 * // create a stream of JsonObject tuples from JSON received from a Web Socket Server
 * TStream<JsonObject> r = wsclient.receive();
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
     * Send a stream's JsonObject tuples as JSON.
     * @param stream the stream
     * @return sink
     */
    public TSink<JsonObject> send(TStream<JsonObject> stream) {
        return sendBytes(stream, JsonFunctions.asBytes());
    }

    /**
     * Send a stream's String tuples as UTF8.
     * @param stream the stream
     * @return sink
     */
    public TSink<String> sendString(TStream<String> stream) {
        return sendBytes(stream, tuple -> tuple.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Send a stream's byte[] tuples.
     * @param stream the stream
     * @return sink
     */
    public TSink<byte[]> sendBytes(TStream<byte[]> stream) {
        return sendBytes(stream, tuple -> tuple);
    }
    
    private <T> TSink<T> sendBytes(TStream<T> stream, Function<T,byte[]> toPayload) {
    	return null; // TODO
    }

    /**
     * Create a stream of JsonObject tuples from received JSON messages.
     * @return the stream
     */
    public TStream<JsonObject> receive() {
    	return receiveBytes(JsonFunctions.fromBytes());
    }
    
    /**
     * Create a stream of String tuples from received UTF8 messages.
     * @return the stream
     */
    public TStream<String> receiveString() {
    	return receiveBytes(payload -> new String(payload, StandardCharsets.UTF_8));
    }

    /**
     * Create a stream of byte[] tuples from received messages.
     * @return the stream
     */
    public TStream<byte[]> receiveBytes() {
    	return receiveBytes(payload -> payload);
    }
    
    private <T> TStream<T> receiveBytes(Function<byte[],T> toTuple) {
    	return null; // TODO
    }
    
}
