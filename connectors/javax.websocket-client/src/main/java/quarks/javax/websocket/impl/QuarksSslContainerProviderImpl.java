/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.javax.websocket.impl;

import java.util.Properties;

import javax.websocket.WebSocketContainer;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.jsr356.ClientContainer;

import quarks.javax.websocket.QuarksSslContainerProvider;

public class QuarksSslContainerProviderImpl extends QuarksSslContainerProvider {
    
    public QuarksSslContainerProviderImpl() { }

    @Override
    public WebSocketContainer getSslContainer(Properties config) {
        
        // With jetty, can't directly use ContainerProvider.getWebSocketContainer()
        // as it's "too late" to inject SslContextFactory into the mix.
        
        String trustStore = config.getProperty("ws.trustStore", 
                                System.getProperty("javax.net.ssl.trustStore"));
        String trustStorePassword = config.getProperty("ws.trustStorePassword",
                                System.getProperty("javax.net.ssl.trustStorePassword"));
        String keyStore = config.getProperty("ws.keyStore", 
                                System.getProperty("javax.net.ssl.keyStore"));
        String keyStorePassword = config.getProperty("ws.keyStorePassword", 
                                System.getProperty("javax.net.ssl.keyStorePassword"));
        String keyPassword = config.getProperty("ws.keyPassword", keyStorePassword);
        String certAlias = config.getProperty("ws.keyCertificateAlias", "default");
        
        // create ClientContainer as usual
        ClientContainer container = new ClientContainer();
        
        //  tweak before starting it
        SslContextFactory scf = container.getClient().getSslContextFactory();
        if (trustStore != null) {
            // System.out.println("setting " + trustStore);
            scf.setTrustStorePath(trustStore);
            scf.setTrustStorePassword(trustStorePassword);
        }
        if (keyStore != null) {
            // System.out.println("setting " + keyStore);
            scf.setKeyStorePath(keyStore);
            scf.setKeyStorePassword(keyStorePassword);
            scf.setKeyManagerPassword(keyPassword);
            scf.setCertAlias(certAlias);
        }
        
        // start as usual
        try {
            container.start();
            return container;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to start Client Container", e);
        }
    }

}
