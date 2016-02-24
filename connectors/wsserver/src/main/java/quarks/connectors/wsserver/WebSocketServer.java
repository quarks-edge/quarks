/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.wsserver;

import java.net.InetSocketAddress;
import java.util.Properties;

import com.google.gson.JsonObject;

import quarks.function.BiFunction;
import quarks.function.Function;
import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * TBD/PLACEHOLDER A connector for receiving and sending messages to a WebSocket Client.
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
 * WebSocketServer wsserver = new WebSocketServer(t, properties);
 * 
 * // create a stream of JsonObject tuples from JSON received from Web Socket Clients
 * TStream<JsonObject> r = wsserver.receive();
 * r.print();
 * 
 * // send a stream's of JsonObject tuples as JSON to all connected Web Socket Clients 
 * TStream<JsonObject> s = ...;
 * wsserver.send(s);
 * }</pre>
 */
public class WebSocketServer {

    /**
     * Create a new WebSocket Server connector.
     * <p>
     * Configuration parameters:
     * <ul>
     * <li>ws.url - "ws://host[:port]", "wss://host[:port]"
     *   the default port is 80 and 443 for "ws" and "wss" respectively.
     *   Any {@code URI path} component is ignored.</li>
     * <li>ws.keyStorePath - required for "wss:"</li>
     * <li>ws.keyStorePassword - required for "wss:"</li>
     * <li>ws.keyPassword - defaults to ws.keyStorePassword value</li>
     * <li>ws.keyCertificateAlias - alias for certificate in key store. defaults to "default"</li>
     * <li>ws.trustStorePath - required for "wss:" if server does client auth</li>
     * <li>ws.trustStorePassword - required for "wss: if server does client auth"</li>
     * </ul>
     * @param config the connector's configuration
     */
    public WebSocketServer(Topology t, Properties config) {
    }

    /**
     * Create a stream of JsonObject tuples from received JSON WebSocket text messages
     * ignoring the client address information.
     * <p>
     * @return the stream
     */
    public TStream<JsonObject> receive() {
        return null; // TODO
    }

    /**
     * Create a stream of String tuples from received WebSocket text messages
     * ignoring the client address information.
     * <p>
     * @return the stream
     */
    public TStream<String> receiveString() {
        return null; // TODO
    }

    /**
     * Create a stream of byte[] tuples from received WebSocket binary messages
     * ignoring the client address information.
     * <p>
     * @return the stream
     */
    public TStream<byte[]> receiveBytes() {
        return receiveBytes((payload,clientAddr) -> payload);
    }

    /**
     * Create a stream of tuples from received WebSocket binary messages.
     * <p>
     * @param toTuple function to create a tuple from the byte[] payload
     *        and the client's address.
     * @return the stream
     */
    public <T> TStream<T> receiveBytes(BiFunction<byte[],InetSocketAddress,T> toTuple) {
        return null;
    }
    
    /**
     * Send the stream's JsonObject tuples as JSON WebSocket text messages to all connected clients.
     * <p>
     * Same as {@code send(stream, tuple -> tuple, null)}
     * @param stream the stream
     * @return a TSink
     */
    public TSink<JsonObject> send(TStream<JsonObject> stream) {
        return null; // TODO
    }
    
    /**
     * Send the stream's String tuples as WebSocket text messages to all connected clients.
     * <p>
     * Same as {@code send(stream, tuple -> tuple, null)}
     * @param stream the stream
     * @return a TSink
     */
    public TSink<String> sendString(TStream<String> stream) {
        return null; // TODO
    }
    
    /**
     * Send the stream's byte[] tuples as WebSocket binary messages to all connected clients.
     * <p>
     * Same as {@code send(stream, tuple -> tuple, null)}
     * @param stream the stream
     * @return a TSink
     */
    public TSink<byte[]> sendBytes(TStream<byte[]> stream) {
        return sendBytes(stream, tuple -> tuple, null);
    }
    
    /**
     * Send the stream's tuples as byte[] WebSocket binary messages to the 
     * specified connected client.
     * 
     * @param stream the stream
     * @param toPayload function to create a payload from a tuple
     * @param toClientAddr function to create a client address from a tuple. 
     *        null to send to all connected clients.
     * @return a TSink
     */
    public <T> TSink<T> sendBytes(TStream<T> stream, Function<T,byte[]> toPayload, Function<T,InetSocketAddress> toClientAddr) {
        return null; // TODO
    }

}
