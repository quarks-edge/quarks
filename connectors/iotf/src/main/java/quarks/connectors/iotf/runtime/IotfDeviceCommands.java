/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.iotf.runtime;

import com.ibm.iotf.client.device.Command;

import quarks.function.Consumer;

/**
 * Consumer that publishes stream tuples as IoTf device events.
 *
 */
public class IotfDeviceCommands implements Consumer<Consumer<Command>> {
    private static final long serialVersionUID = 1L;
    private final IotfConnector connector;

    public IotfDeviceCommands(IotfConnector connector) {
        this.connector = connector;
    }

    @Override
    public void accept(Consumer<Command> commandSubmitter) {
        
        try {
            connector.subscribeCommands(commandSubmitter);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
