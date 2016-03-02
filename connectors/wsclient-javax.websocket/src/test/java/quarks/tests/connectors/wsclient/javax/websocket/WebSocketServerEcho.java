package quarks.tests.connectors.wsclient.javax.websocket;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

/**
 * Simple WebSocket server program to echo received messages.
 * Following info from https://github.com/jetty-project/embedded-jetty-websocket-examples
 */
@ServerEndpoint(value="/echo")
public class WebSocketServerEcho {
    Server server;
    ServerConnector connector;
    
    public static void main(String[] args) throws Exception {
        URI uri = new URI("ws://localhost:0");
        WebSocketServerEcho srvr = new WebSocketServerEcho();
        srvr.start(uri);
    }
    
    public void start(URI endpointURI) {
        server = new Server(endpointURI.getPort());
        connector = (ServerConnector)server.getConnectors()[0];

        // Setup the basic application "context" for this application at "/"
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        
        try {
            // Initialize javax.websocket layer
            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

            // Add WebSocket endpoint to javax.websocket layer
            wscontainer.addEndpoint(this.getClass());

            server.start();
            System.out.println("WsServerEcho started "+connector);
            // server.dump(System.err);            
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            throw new RuntimeException("start", e);
        }
    }
    
    public int getPort() {
        // returns -1 if called before started
        return connector.getLocalPort();
    }
    
    public void stop() {
        if (connector != null) {
            try {
                System.out.println("WsServerEcho stop "+connector);
                connector.stop();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally {
                connector = null;
            }
        }
    }
    
    @OnOpen
    public void opOpen(Session session) {
        System.out.println("WsServerEcho onOpen ");
    }
    
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WsServerEcho onClose reason="+reason);
    }
    
    @OnMessage
    public void onStringMessage(Session session, String message) {
        System.out.println("WsServerEcho onStringMessage msg="+message);
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  // "echo" response
    }
    
    @OnMessage
    public void onByteMessage(Session session, ByteBuffer message) {
        System.out.println("WsServerEcho onByteMessage "+message.array().length+" bytes");
        try {
            session.getBasicRemote().sendBinary(message);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  // "echo" response
    }
    
    @OnError
    public void onError(Throwable cause) {
        System.err.println("WsServerEcho onError " + cause);
        cause.printStackTrace(System.err);
    }

}