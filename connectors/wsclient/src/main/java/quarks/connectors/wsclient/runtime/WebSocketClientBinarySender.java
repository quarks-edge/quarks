/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.wsclient.runtime;

import quarks.function.Function;

public class WebSocketClientBinarySender<T> extends WebSocketClientSender<T> {
    private static final long serialVersionUID = 1L;
    private final Function<T,byte[]> toPayload;
    
    public WebSocketClientBinarySender(WebSocketClientConnector connector, Function<T,byte[]> toPayload) {
        super(connector, null);
        this.toPayload = toPayload;
    }

    @Override
    public void accept(T value) {
        connector.sendBinary(toPayload.apply(value));
    }

}
