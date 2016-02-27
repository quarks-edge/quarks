/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.wsclient.javax.websocket.runtime;

import quarks.function.Consumer;
import quarks.function.Function;

public class WebSocketClientSender<T> implements Consumer<T>, AutoCloseable {
    private static final long serialVersionUID = 1L;
    protected final WebSocketClientConnector connector;
    protected final Function<T,String> toPayload;
    
    public WebSocketClientSender(WebSocketClientConnector connector, Function<T,String> toPayload) {
        this.connector = connector;
        this.toPayload = toPayload;
    }

    @Override
    public void accept(T value) {
        connector.sendText(toPayload.apply(value));
    }

    @Override
    public void close() throws Exception {
        connector.close();
    }

}
