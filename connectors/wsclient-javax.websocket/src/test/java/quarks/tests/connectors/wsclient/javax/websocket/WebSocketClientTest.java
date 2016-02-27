package quarks.tests.connectors.wsclient.javax.websocket;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;

import com.google.gson.JsonObject;

import quarks.connectors.wsclient.WebSocketClient;
import quarks.connectors.wsclient.javax.websocket.Jsr356WebSocketClient;
import quarks.test.connectors.common.ConnectorTestBase;
import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.json.JsonFunctions;
import quarks.topology.plumbing.PlumbingStreams;

public class WebSocketClientTest extends ConnectorTestBase {
    private final static int SEC_TMO = 5;
    WebSocketServerEcho wsServer;
    boolean isExternalServer;// = true;
    int wsServerPort = !isExternalServer ? 0 : 52224;
    String wsUriPath = "/echo";  // match what WsServerEcho is using
    
    @After
    public void cleanup() {
        if (wsServer != null)
            wsServer.stop();
        wsServer = null;
    }
    
    private enum ServerMode { WS, SSL, SSL_CLIENT_AUTH }
    private void startEchoer() {
        startEchoer(ServerMode.WS);
    }
    private void startEchoer(ServerMode mode) {
        try {
            if (!isExternalServer) {
                URI uri;
                if (mode==ServerMode.WS) {
                    uri = new URI("ws://localhost:0");
                    wsServer = new WebSocketServerEcho();
                }
                else {
                    uri = new URI("wss://localhost:0");
                    wsServer = new WebSocketServerEcho();
                }
                wsServer.start(uri);
                wsServerPort = wsServer.getPort();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("startEchoer",e );
        }
    }
    
    Properties getConfig() {
        return getWsConfig();
    }

    Properties getWsConfig() {
        Properties config = new Properties();
        config.setProperty("ws.uri", getWsUri());
        return config;
    }

    Properties getWssConfig() {
        Properties config = new Properties();
        config.setProperty("ws.uri", getWssUri());
        // TODO key/trust store stuff
        return config;
    }
    
    String getWsUri() {
        int port = wsServerPort==0 ? 8080 : wsServerPort;
        return "ws://localhost:"+port+wsUriPath;
    }
    
    String getWssUri() {
        int port = wsServerPort==0 ? 443 : wsServerPort;
        return "wss://localhost:"+port+wsUriPath;
    }
    
    @Test
    public void testBasicStaticStuff() {
        Topology t = newTopology("testBasicStaticStuff");

        Properties config = getConfig();
        WebSocketClient wsClient1 = new Jsr356WebSocketClient(t, config);
        
        TStream<String> s1 = wsClient1.receiveString();
        assertNotNull("s1", s1);
        
        TSink<String> sink1 = wsClient1.sendString(t.strings("one", "two"));
        assertNotNull("sink1", sink1);
        
        WebSocketClient wsClient2 = new Jsr356WebSocketClient(t, config);
        TStream<String> s2 = wsClient2.receiveString();
        assertNotSame("s1 s2", s1, s2);
        
        TSink<String> sink2 = wsClient2.sendString(t.strings("one", "two"));
        assertNotSame("sink1 sink2", sink1, sink2);        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMissingWsUri() {
        Topology t = newTopology("testMissingWsUri");
        new Jsr356WebSocketClient(t, new Properties());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMalformedWsUri() {
        Topology t = newTopology("testMalformedWsUri");
        Properties config = new Properties();
        config.setProperty("ws.uri", "localhost"); // missing scheme
        new Jsr356WebSocketClient(t, config);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNotWsUri() {
        Topology t = newTopology("testNotWsUri");
        Properties config = new Properties();
        config.setProperty("ws.uri", "tcp://localhost");
        new Jsr356WebSocketClient(t, config);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWssTrustStorePathNeg() {
        Topology t = newTopology("testWssTrustStorePathNeg");
        Properties config = new Properties();
        config.setProperty("ws.uri", getWssUri());
        // missing trustStorePath
        new Jsr356WebSocketClient(t, config);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWssTrustStorePasswordNeg() {
        Topology t = newTopology("testWssTrustStorePasswordNeg");
        Properties config = new Properties();
        config.setProperty("ws.uri", getWssUri());
        config.setProperty("ws.trustStorePath", "xyzzy"); // not checked till runtime
        // missing truststorePassword
        new Jsr356WebSocketClient(t, config);
    }
    
    @Test
    public void testWssConfig() {
        Topology t = newTopology("testWssConfig");
        Properties config = new Properties();
        config.setProperty("ws.uri", getWssUri());
        config.setProperty("ws.trustStorePath", "xyzzy"); // not checked till runtime
        config.setProperty("ws.trustStorePassword", "xyzzy"); // not checked till runtime
        new Jsr356WebSocketClient(t, config);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testTooManySendersNeg() {
        Topology t = newTopology("testTooManySendersNeg");
        TStream<String> s1 = t.strings("one", "two");
        TStream<String> s2 = t.strings("one", "two");

        Properties config = getConfig();
        WebSocketClient wsClient = new Jsr356WebSocketClient(t, config);
        wsClient.sendString(s1);
        wsClient.sendString(s2); // should throw
    }
    
    @Test(expected = IllegalStateException.class)
    public void testTooManyReceiversNeg() {
        Topology t = newTopology("testTooManyReceiversNeg");

        Properties config = getConfig();
        WebSocketClient wsClient = new Jsr356WebSocketClient(t, config);
        @SuppressWarnings("unused")
        TStream<String> s1 = wsClient.receiveString();
        @SuppressWarnings("unused")
        TStream<String> s2 = wsClient.receiveString(); // should throw
    }
    
    @Test
    public void testJson() throws Exception {
        Topology t = newTopology("testJson");
        System.out.println("===== "+t.getName());
        
        startEchoer();  // before getConfig() so it gets the port
        
        Properties config = getConfig();
        WebSocketClient wsClient = new Jsr356WebSocketClient(t, config);
        
        String[] expected = new String[] {
                "{\"id\":\"id1\",\"value\":27}",
                "{\"id\":\"id2\",\"value\":13}"
        };
        
        TStream<JsonObject> s = t.strings(expected)
                                .map(JsonFunctions.fromString());
        s = PlumbingStreams.blockingOneShotDelay(s, 2, TimeUnit.SECONDS);
        wsClient.send(s);
        
        TStream<String> rcvd = wsClient.receive()
                                .map(JsonFunctions.asString());
        
        completeAndValidate("", t, rcvd, SEC_TMO, expected);
    }
    
    @Test
    public void testString() throws Exception {
        Topology t = newTopology("testString");
        System.out.println("===== "+t.getName());
        
        startEchoer();  // before getConfig() so it gets the port
        
        Properties config = getConfig();
        WebSocketClient wsClient = new Jsr356WebSocketClient(t, config);
        
        String[] expected = new String[] { "one", "two" };
        
        TStream<String> s = t.strings(expected);
        s = PlumbingStreams.blockingOneShotDelay(s, 2, TimeUnit.SECONDS);
        wsClient.sendString(s);
        
        TStream<String> rcvd = wsClient.receiveString();
        
        completeAndValidate("", t, rcvd, SEC_TMO, expected);
    }
    
    @Test
    public void testBytes() throws Exception {
        Topology t = newTopology("testBytes");
        System.out.println("===== "+t.getName());

        startEchoer();  // before getConfig() so it gets the port
        
        Properties config = getConfig();
        WebSocketClient wsClient = new Jsr356WebSocketClient(t, config);
        
        String[] expected = new String[] { "one", "two" };
        
        TStream<byte[]> s = t.strings(expected)
                                .map(tup -> tup.getBytes());
        s = PlumbingStreams.blockingOneShotDelay(s, 2, TimeUnit.SECONDS);
        wsClient.sendBytes(s);
        
        TStream<String> rcvd = wsClient.receiveBytes()
                                .map(tup -> new String(tup));
        
        completeAndValidate("", t, rcvd, SEC_TMO, expected);
    }
    
    @Test
    public void testReconnect() throws Exception {
        assumeTrue(false);
        
        Topology t = newTopology("testReconnect");
        System.out.println("===== "+t.getName());

        startEchoer();  // before getConfig() so it gets the port
        
        // TODO
    }
    
    @Test
    public void testSsl() throws Exception {

        assumeTrue(false); // TODO
        
        Topology t = newTopology("testSsl");
        System.out.println("===== "+t.getName());

        startEchoer(ServerMode.SSL);  // before getConfig() so it gets the port
        
        Properties config = getWssConfig();
        assertTrue(config.getProperty("ws.uri").startsWith("wss:"));
        
        WebSocketClient wsClient = new Jsr356WebSocketClient(t, config);
        
        String[] expected = new String[] { "one", "two" };
        
        TStream<String> s = t.strings(expected);
        s = PlumbingStreams.blockingOneShotDelay(s, 2, TimeUnit.SECONDS);
        wsClient.sendString(s);
        
        TStream<String> rcvd = wsClient.receiveString();
        
        completeAndValidate("", t, rcvd, SEC_TMO, expected);
    }
    
    @Test
    public void testSslNeg() throws Exception {

        assumeTrue(false); // TODO
        
        Topology t = newTopology("testSslNeg");
        System.out.println("===== "+t.getName());

        startEchoer();  // before getConfig() so it gets the port
        
        Properties config = getWssConfig();
        assertTrue(config.getProperty("ws.uri").startsWith("wss:"));
        String emptyTrustStorePath = "path-to-empty-trust-store"; // TODO
        config.setProperty("ws.trustStorePath", emptyTrustStorePath); // so we don't recognize server cert
        
        WebSocketClient wsClient = new Jsr356WebSocketClient(t, config);
        
        String[] expected = new String[] { "one", "two" };
        
        TStream<String> s = t.strings(expected);
        s = PlumbingStreams.blockingOneShotDelay(s, 2, TimeUnit.SECONDS);
        wsClient.sendString(s);
        
        TStream<String> rcvd = wsClient.receiveString();
        
        completeAndValidate("", t, rcvd, SEC_TMO, expected);
    }
    
    @Test
    public void testSslClientAuth() throws Exception {

        assumeTrue(false); // TODO
        
        Topology t = newTopology("testSslClientAuth");
        System.out.println("===== "+t.getName());

        startEchoer(ServerMode.SSL_CLIENT_AUTH);  // before getConfig() so it gets the port
        
        Properties config = getWssConfig();
        assertTrue(config.getProperty("ws.uri").startsWith("wss:"));
        
        WebSocketClient wsClient = new Jsr356WebSocketClient(t, config);
        
        String[] expected = new String[] { "one", "two" };
        
        TStream<String> s = t.strings(expected);
        s = PlumbingStreams.blockingOneShotDelay(s, 2, TimeUnit.SECONDS);
        wsClient.sendString(s);
        
        TStream<String> rcvd = wsClient.receiveString();
        
        completeAndValidate("", t, rcvd, SEC_TMO, expected);
    }
    
    @Test
    public void testSslClientAuthNeg() throws Exception {

        assumeTrue(false); // TODO
        
        Topology t = newTopology("testSslClientAuthNeg");
        System.out.println("===== "+t.getName());

        startEchoer(ServerMode.SSL_CLIENT_AUTH);  // before getConfig() so it gets the port
        
        Properties config = getWssConfig();
        assertTrue(config.getProperty("ws.uri").startsWith("wss:"));
        config.remove("ws.keyStorePath");  // so we can't supply client cert
        
        WebSocketClient wsClient = new Jsr356WebSocketClient(t, config);
        
        String[] expected = new String[] { "one", "two" };
        
        TStream<String> s = t.strings(expected);
        s = PlumbingStreams.blockingOneShotDelay(s, 2, TimeUnit.SECONDS);
        wsClient.sendString(s);
        
        TStream<String> rcvd = wsClient.receiveString();
        
        completeAndValidate("", t, rcvd, SEC_TMO, expected);
    }
}
