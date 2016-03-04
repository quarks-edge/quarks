/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.wsclient;

import com.google.gson.JsonObject;

import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.TopologyElement;

/**
 * A generic connector for sending and receiving messages to a WebSocket Server.
 * <p>
 * A connector is bound to its configuration specified 
 * {@code javax.websockets} WebSocket URI.
 * <p> 
 * A single connector instance supports sinking at most one stream
 * and sourcing at most one stream.
 * <p>
 * Sample use:
 * <pre>{@code
 * // assuming a properties file containing at least:
 * // ws.uri=ws://myWsServerHost/myService
 *  
 * String propsPath = <path to properties file>; 
 * Properties properties = new Properties();
 * properties.load(Files.newBufferedReader(new File(propsPath).toPath()));
 *
 * Topology t = ...;
 * Jsr356WebSocketClient wsclient = new SomeWebSocketClient(t, properties);
 * 
 * // send a stream's JsonObject tuples as JSON WebSocket text messages
 * TStream<JsonObject> s = ...;
 * wsclient.send(s);
 * 
 * // create a stream of JsonObject tuples from received JSON WebSocket text messages
 * TStream<JsonObject> r = wsclient.receive();
 * r.print();
 * }</pre>
 * 
 * Implementations are strongly encouraged to support construction from
 * Properties with the following configuration parameters:
 * <ul>
 * <li>ws.uri - "ws://host[:port][/path]", "wss://host[:port][/path]"
 *   the default port is 80 and 443 for "ws" and "wss" respectively.
 *   The optional path must match the server's configuration.</li>
 * <li>ws.trustStore - optional. Only used with "wss:".
 *     Path to trust store file in JKS format.
 *     If not set, the standard JRE and javax.net.ssl system properties
 *     control the SSL behavior.
 *     Generally not required if server has a CA-signed certificate.</li>
 * <li>ws.trustStorePassword - required if ws.trustStore is set</li>
 * <li>ws.keyStore - optional. Only used with "wss:" when the
 *     server is configured for client auth.
 *     Path to key store file in JKS format.
 *     If not set, the standard JRE and javax.net.ssl system properties
 *     control the SSL behavior.</li>
 * <li>ws.keyStorePassword - required if ws.keyStore is set.</li>
 * <li>ws.keyPassword - defaults to ws.keyStorePassword value</li>
 * <li>ws.keyCertificateAlias - alias for certificate in key store. defaults to "default"</li>
 * </ul>
 */
public interface WebSocketClient extends TopologyElement {

    /**
     * Send a stream's JsonObject tuples as JSON in a WebSocket text message.
     * @param stream the stream
     * @return sink
     */
    TSink<JsonObject> send(TStream<JsonObject> stream);

    /**
     * Send a stream's String tuples in a WebSocket text message.
     * @param stream the stream
     * @return sink
     */
    TSink<String> sendString(TStream<String> stream);
    
    /**
     * Send a stream's byte[] tuples in a WebSocket binary message.
     * @param stream the stream
     * @return sink
     */
    TSink<byte[]> sendBytes(TStream<byte[]> stream);

    /**
     * Create a stream of JsonObject tuples from received JSON WebSocket text messages.
     * @return the stream
     */
    TStream<JsonObject> receive();
    
    /**
     * Create a stream of String tuples from received WebSocket text messages.
     * @return the stream
     */
    TStream<String> receiveString();

    /**
     * Create a stream of byte[] tuples from received WebSocket binary messages.
     * @return the stream
     */
    TStream<byte[]> receiveBytes();
    
}
