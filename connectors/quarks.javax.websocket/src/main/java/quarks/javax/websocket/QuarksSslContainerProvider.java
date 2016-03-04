/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.javax.websocket;

import java.util.Properties;
import java.util.ServiceLoader;

import javax.websocket.WebSocketContainer;

/**
 * A {@link WebSocketContainer} provider for dealing with javax.websocket
 * SSL issues.
 * <p>
 * <ul>
 * <li>JSR356 lacks API for Client-side SSL configuration</li>
 * <li>Jetty's {@code javax.websocket.ContainerProvider} ignores the
 *     {@code javax.net.ssl.keyStore} system property.
 *     It correctly handles {@code javax.net.ssl.trustStore}</li>
 * </ul>
 * see https://github.com/eclipse/jetty.project/issues/155
 * <p>
 * The net is that Jetty's {@code javax.websocket.ContainerProvider}
 * works fine for the "ws" protocol and for "wss" as long as
 * one doesn't need to <b>programatically</b> specify a trustStore path
 * and one doesn't to specify a keyStore for SSL client authentication support.
 * <p>
 * A {@code QuarksSslContainerProvider} implementation is responsible for
 * working around those limitations.
 */
public abstract class QuarksSslContainerProvider {
    
    /**
     * Create a WebSocketContainer setup for SSL.
     * <p>
     * The Java {@link ServiceLoader} is used to locate an implementation
     * of {@code quarks.javax.websocket.Quarks.SslContainerProvider}.
     * @param config  SSL configuration info as described by
     * {@code quarks.containers.wsclient.javax.websocket.Jsr356WebSocketClient}.
     * @return the WebSocketContainer
     * @throws RuntimeException upon failure
     */
    public static WebSocketContainer getSslWebSocketContainer(Properties config) throws RuntimeException {
        for (QuarksSslContainerProvider provider : ServiceLoader.load(QuarksSslContainerProvider.class)) {
            WebSocketContainer container = provider.getSslContainer(config);
            if (container != null)
                return container;
        }

        throw new RuntimeException("Unable to find an implementation of QuarksSslContainerProvider");
    }

    /**
     * Create a WebSocketContainer setup for SSL.
     * To be implemented by a javax.websocket client provider.
     * @param config  SSL configuration info. 
     * @return the WebSocketContainer
     * @throws RuntimeException if it fails
     */
    protected abstract WebSocketContainer getSslContainer(Properties config);
}
