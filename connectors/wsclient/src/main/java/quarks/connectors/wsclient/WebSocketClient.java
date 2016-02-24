/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.wsclient;

import java.util.Objects;
import java.util.Properties;

import com.google.gson.JsonObject;

import quarks.connectors.wsclient.runtime.WebSocketClientBinaryReceiver;
import quarks.connectors.wsclient.runtime.WebSocketClientBinarySender;
import quarks.connectors.wsclient.runtime.WebSocketClientConnector;
import quarks.connectors.wsclient.runtime.WebSocketClientReceiver;
import quarks.connectors.wsclient.runtime.WebSocketClientSender;
import quarks.function.Function;
import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.json.JsonFunctions;

/**
 * A connector for sending and receiving messages to a WebSocket Server.
 * <p>
 * A connector is bound to its configuration specified WebSocket URI.
 * <p> 
 * A single connector instance supports sinking at most one stream
 * and sourcing at most one stream.
 * <p>
 * See {@code javax.websocket} for more information about WebSockets.
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
 * WebSocketClient wsclient = new WebSocketClient(t, properties);
 * 
 * // send a stream's JsonObject tuples as JSON WebSocket text messages
 * TStream<JsonObject> s = ...;
 * wsclient.send(s);
 * 
 * // create a stream of JsonObject tuples from received JSON WebSocket text messages
 * TStream<JsonObject> r = wsclient.receive();
 * r.print();
 * }</pre>
 */
public class WebSocketClient {
    private final Topology t;
    private final WebSocketClientConnector connector;
    private int senderCnt;
    private int receiverCnt;

    /**
     * Create a new Web Socket Client connector.
     * <p>
     * !!! At this moment only "ws" is supported !!!
     * <br>
     * TODO support for "wss" and associated params.
     * <p>
     * Configuration parameters:
     * <ul>
     * <li>ws.uri - "ws://host[:port][/path]", "wss://host[:port][/path]"
     *   the default port is 80 and 443 for "ws" and "wss" respectively.
     *   The optional path must match the server's configuration (including
     *   whether or not it ends with a '/').</li>
     * <li>ws.trustStorePath - required for "wss:"</li>
     * <li>ws.trustStorePassword - required for "wss:"</li>
     * <li>ws.keyStorePath - required for "wss:" if server does client auth</li>
     * <li>ws.keyStorePassword - required for "wss:" if server does client auth</li>
     * <li>ws.keyPassword - defaults to ws.keyStorePassword value</li>
     * <li>ws.keyCertificateAlias - alias for certificate in key store. defaults to "default"</li>
     * </ul>
     * Additional keys in {@code config} are ignored.
     * @param config the connector's configuration
     */
    public WebSocketClient(Topology t, Properties config) {
        Objects.requireNonNull(t, "t");
        Objects.requireNonNull(config, "config");
        this.t = t;
        connector = new WebSocketClientConnector(config);
    }

    /**
     * Send a stream's JsonObject tuples as JSON in a WebSocket text message.
     * @param stream the stream
     * @return sink
     */
    public TSink<JsonObject> send(TStream<JsonObject> stream) {
        Objects.requireNonNull(stream, "stream");
        return sendText(stream, JsonFunctions.asString());
    }

    /**
     * Send a stream's String tuples in a WebSocket text message.
     * @param stream the stream
     * @return sink
     */
    public TSink<String> sendString(TStream<String> stream) {
        Objects.requireNonNull(stream, "stream");
        return sendText(stream, tuple -> tuple);
    }
    
    private <T> TSink<T> sendText(TStream<T> stream, Function<T,String> toPayload) {
        Objects.requireNonNull(stream, "stream");
        Objects.requireNonNull(toPayload, "toPayload");
        checkAddSender();
        return stream.sink(new WebSocketClientSender<T>(connector, toPayload));
    }
    
    /**
     * Send a stream's byte[] tuples in a WebSocket binary message.
     * @param stream the stream
     * @return sink
     */
    public TSink<byte[]> sendBytes(TStream<byte[]> stream) {
        Objects.requireNonNull(stream, "stream");
        return sendBinary(stream, tuple -> tuple);
    }
    
    private <T> TSink<T> sendBinary(TStream<T> stream, Function<T,byte[]> toPayload) {
        Objects.requireNonNull(stream, "stream");
        Objects.requireNonNull(toPayload, "toPayload");
        checkAddSender();
        return stream.sink(new WebSocketClientBinarySender<T>(connector, toPayload));
    }
    
    private void checkAddSender() throws IllegalStateException {
        // enforce a sender restriction to match the receiver restriction.
        if (++senderCnt > 1)
            throw new IllegalStateException("More than one sender specified");
    }

    /**
     * Create a stream of JsonObject tuples from received JSON WebSocket text messages.
     * @return the stream
     */
    public TStream<JsonObject> receive() {
        return receiveText(JsonFunctions.fromString());
    }
    
    /**
     * Create a stream of String tuples from received WebSocket text messages.
     * @return the stream
     */
    public TStream<String> receiveString() {
        return receiveText(tuple -> tuple);
    }
    
    private <T> TStream<T> receiveText(Function<String,T> toTuple) {
        checkAddReceiver();
        return t.events(new WebSocketClientReceiver<T>(connector, toTuple));
    }

    /**
     * Create a stream of byte[] tuples from received WebSocket binary messages.
     * @return the stream
     */
    public TStream<byte[]> receiveBytes() {
        return receiveBinary(payload -> payload);
    }
    
    private <T> TStream<T> receiveBinary(Function<byte[],T> toTuple) {
        checkAddReceiver();
        return t.events(new WebSocketClientBinaryReceiver<T>(connector, toTuple));
    }
    
    private void checkAddReceiver() throws IllegalStateException {
        // there's no good reason for the base functionality to support
        // multiple receivers so don't.  The underlying implementation
        // is simplified with this restriction.
        if (++receiverCnt > 1)
            throw new IllegalStateException("More than one receiver specified");
    }
    
}
