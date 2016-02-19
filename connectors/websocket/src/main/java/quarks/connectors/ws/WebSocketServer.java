/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.ws;

import java.net.InetSocketAddress;
import java.util.Properties;

import quarks.function.BiFunction;
import quarks.function.Function;
import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * A connector for receiving and sending messages to a Web Socket Client.
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
 * WebSocketServer wsserver = new WebSocketServer(t, properties);
 * 
 * // create a stream of tuples from JSON received from Web Socket Clients
 * TStream<JsonObject> r = wsserver.receive().map(JsonFunctions.fromString());
 * r.print();
 * 
 * // send a stream's of tuples as JSON to all connected Web Socket Clients 
 * TStream<JsonObject> s = ...;
 * wsserver.send(s, JsonFunctions.asString(), null);
 * }</pre>
 */
public class WebSocketServer {

	/**
	 * Create a new Web Socket Server connector.
     * <p>
     * Configuration parameters:
     * <ul>
     * <li>ws.url - "ws://host[:port]", "wss://host[:port]"
     *   the default port is 80 and 443 for "ws" and "wss" respectively</li>
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
     * Create a stream of String tuples from received String messages.
     * <p>
     * Same as {@link #receive(BiFunction)} that ignores the client address info.
     * <p>
     * @return the stream
     */
    public TStream<String> receive() {
        return receive((payload,clientAddr) -> payload);
    }
    
    /**
     * Create a stream of tuples from received String messages.
     * 
     * @param toTuple function that creates a tuple from a payload and client address.
     * @return the stream
     */
    public <T> TStream<T> receive(BiFunction<String,InetSocketAddress,T> toTuple) {
        return null; // TODO
    }

    /**
     * Create a stream of tuples from received byte[] messages.
     * 
     * @param toTuple function that creates a tuple from a payload and client address.
     * @return the stream
     */
    public <T> TStream<T> receiveBytes(BiFunction<byte[],InetSocketAddress,T> toTuple) {
        return null; // TODO
    }
    
    /**
     * Send the stream's String tuples to all connected clients.
     * <p>
     * Same as {@code send(stream, tuple -> tuple, null)}
     * @param stream the stream
     * @return a TSink
     */
    public TSink<String> send(TStream<String> stream) {
    	return send(stream, tuple -> tuple, null);
    }
    
    /**
     * Send the stream's tuples as String payloads to the tuple specified
     * connected client.
     * 
     * @param stream the stream
     * @param toPayload function to create a payload from a tuple
     * @param toClientAddr function to create a client address from a tuple. 
     *        null to send to all connected clients.
     * @return a TSink
     */
    public <T> TSink<T> send(TStream<T> stream, Function<T,String> toPayload, Function<T,InetSocketAddress> toClientAddr) {
    	return null; // TODO
    }
    
    /**
     * Send the stream's tuples as byte[] payloads to the tuple specified 
     * connected client.
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
