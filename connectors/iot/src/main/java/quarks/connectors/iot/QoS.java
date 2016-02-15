/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.iot;

/**
 * Device event quality of service levels.
 * The QoS levels match the MQTT specification.
 * <BR>
 * An implementation of {@link IotDevice} may not
 * support all QoS levels.
 * 
 * @see <a href="http://mqtt.org/">mqtt.org</a>
 * @see IotDevice#events(quarks.topology.TStream, String, int)
 * @see IotDevice#events(quarks.topology.TStream, quarks.function.Function, quarks.function.UnaryOperator, quarks.function.Function)

 */
public interface QoS {
    
    /**
     * The message containing the event arrives at the message hub either once or not at all.
     * <BR>
     * Value is {@code 0}.
     */
    Integer AT_MOST_ONCE = 0;
    
    /**
     * Fire and forget the event. Synonym for {@link #AT_MOST_ONCE}.
     * <BR>
     * Value is {@code 0}.
     */
    Integer FIRE_AND_FORGET = 0;
    
    /**
     * The message containing the event arrives at the message hub at least once.
     * The message may be seen at the hub multiple times.
     * <BR>
     * Value is {@code 1}.
     */
    Integer AT_LEAST_ONCE = 1;
    
    /**
     * The message containing the event arrives at the message hub exactly once.
     * <BR>
     * Value is {@code 2}.
     */ 
    Integer EXACTLY_ONCE = 2;
}
