package quarks.connectors.serial;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serial port runtime access.
 * 
 * A serial port has an {@link #getOutput()} and
 * an {@link #getOutput() output} I/O stream.
 *
 */
public interface SerialPort {
    
    /**
     * Get the input stream for this serial port.
     * @return input stream for this serial port.
     */
    InputStream getInput();
    
    /**
     * Get the output stream for this serial port.
     * @return output stream for this serial port.
     */
    OutputStream getOutput();
}
