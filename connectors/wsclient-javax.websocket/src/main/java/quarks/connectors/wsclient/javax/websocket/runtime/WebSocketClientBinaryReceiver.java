/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.wsclient.javax.websocket.runtime;

import quarks.function.Function;

public class WebSocketClientBinaryReceiver<T> extends WebSocketClientReceiver<T> {
    private static final long serialVersionUID = 1L;
    private final Function<byte[],T> toTuple;
    
    public WebSocketClientBinaryReceiver(WebSocketClientConnector connector, Function<byte[],T> toTuple) {
        super(connector, null);
        this.toTuple = toTuple;
    }
    
    void onBinaryMessage(byte[] message) {
        eventHandler.accept(toTuple.apply(message));
    }
    
    void onTextMessage(String message) {
        connector.getLogger().debug("{} ignoring received text message (expecting binary)", connector.id());
    }

}
