/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.wsclient.runtime;

import quarks.function.Consumer;
import quarks.function.Function;

public class WebSocketClientReceiver<T> implements Consumer<Consumer<T>>, AutoCloseable {
    private static final long serialVersionUID = 1L;
    protected final WebSocketClientConnector connector;
    private final Function<String,T> toTuple;
    protected Consumer<T> eventHandler;
    
    public WebSocketClientReceiver(WebSocketClientConnector connector, Function<String,T> toTuple) {
        this.connector = connector;
        this.toTuple = toTuple;
    }

    @Override
    public void accept(Consumer<T> eventHandler) {
        this.eventHandler = eventHandler;
        connector.setReceiver(this);
        try {
            connector.client();  // induce connecting.
        } catch (Exception e) {
            connector.getLogger().error("{} receiver setup failed", connector.id(), e);
        }
    }
    
    void onBinaryMessage(byte[] message) {
        connector.getLogger().debug("{} ignoring received binary message (expecting text)", connector.id());
    }
    
    void onTextMessage(String message) {
        eventHandler.accept(toTuple.apply(message));
    }

    @Override
    public void close() throws Exception {
        connector.close();
    }

}
