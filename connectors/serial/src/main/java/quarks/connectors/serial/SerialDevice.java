/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.serial;


import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import quarks.function.Function;
import quarks.function.Supplier;
import quarks.topology.TopologyElement;

/**
 * Access to a device (or devices) connected by a serial port.
 * A serial port at runtime is represented by
 * a {@link SerialPort}.
 * <P>
 * {@code SerialDevice} is typically used through
 * a protocol module that sends the appropriate bytes
 * to the port and decodes the bytes output by the port.
 * </P>
 * <P>
 * It is guaranteed that during any call to function returned by
 * this interface has exclusive access to {@link SerialPort}.
 * </P>
 */
public interface SerialDevice extends TopologyElement {
	
	/**
	 * Set the initialization function for this port.
	 * Can be used to send setup instructions to the
	 * device connected to this serial port.
	 * <BR>
	 * {@code initializer.accept(port)} is called once, passing a runtime
	 * {@link SerialPort} for this serial device.
	 * 
	 * @param initializer Function to be called when the application runs.
	 */
	void setInitializer(Consumer<SerialPort> initializer);
		
	/**
	 * Create a function that can be used to source a
	 * stream from a serial port device.
	 * <BR>
	 * Calling {@code get()} on the returned function will result in a call
	 * to {@code driver.apply(serialPort)}
	 * passing a runtime {@link SerialPort} for this serial device.
	 * The value returned by {@code driver.apply(serialPort)} is
	 * returned by this returned function.
	 * <BR>
	 * The function {@code driver} typically sends instructions to the
	 * serial port using {@link SerialPort#getOutput()} and then
	 * reads the result using {@link SerialPort#getInput()}.
	 * <P>
	 * Multiple instances of a supplier function can be created,
	 * for example to read different parameters from the
	 * device connected to the serial port. While each function
	 * is being called it has exclusive use of the serial port.
	 * </P>
	 * @param driver Function that interacts with the serial port to produce a value.
	 * @return Function that for each call will interact with the serial port to produce a value.
	 * 
	 * @see quarks.topology.Topology#poll(Supplier, long, TimeUnit)
	 */	
	public <T> Supplier<T> getSource(Function<SerialPort,T> driver);
}
