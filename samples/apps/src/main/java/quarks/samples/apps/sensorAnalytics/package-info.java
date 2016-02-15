/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
/**
 * The Sensor Analytics sample application demonstrates some common 
 * continuous sensor analytic application themes.
 * See {@link quarks.samples.apps.sensorAnalytics.Sensor1 Sensor1} for the
 * core of the analytics processing and  
 * {@link quarks.samples.apps.sensorAnalytics.SensorAnalyticsApplication
 * SensorAnalyticsApplication}
 * for the main program.
 * <p>
 * The themes include:
 * <ul>
 * <li>Batched Data Reduction - reducing higher frequency sensor reading
 *     samples down to a lower frequency using statistical aggregations
 *     of the raw readings.
 *     </li>
 * <li>Computing continuous historical statistics such as a
 *     30 second trailing average of sensor readings.
 *     </li>
 * <li>Outlier / threshold detection against a configurable range</li>
 * <li>Local logging of stream tuples</li>
 * <li>Publishing analytic results to an MQTT broker</li>
 * <li>Dynamic configuration control - subscribing to a MQTT broker
 *     to receive commands to adjust the threshold detection range value. 
 *     </li>
 * <li>Generally, the configuration of the processing is driven via an
 *     external configuration description.
 *     </li>
 * <li>Conditional stream tracing - configuration controlled inclusion of tracing.
 *     </li>
 * </ul>
 * 
 * <h2>Prerequisites:</h2>
 * <p>
 * The default configuration is for a local MQTT broker.
 * A good resource is <a href="http://mosquitto.org">mosquitto.org</a>
 * if you want to download and setup your own MQTT broker.
 * Or you can use some other broker available in your environment.
 * <p>
 * Alternatively, there are some public MQTT brokers available to experiment with.
 * Their availability status isn't guaranteed.  If you're unable to connect
 * to the broker, it's likely that it isn't up or your firewalls don't
 * allow you to connect.  DO NOT PUBLISH ANYTHING SENSITIVE - anyone
 * can be listing.  A couple of public broker locations are noted
 * in the application's properties file.
 * <p>
 * The default {@code mqttDevice.topic.prefix} value, used by default in 
 * generated MQTT topic values and MQTT clientId, contains the user's
 * local login id.  The SensorAnalytics sample application does not have any
 * other sensitive information.
 * <p>
 * Edit {@code <quarks-release>/java8/scripts/apps/sensorAnalytics/sensoranalytics.properties}
 * to change the broker location or topic prefix.
 * <p>
 * <h2>Application output:</h2>
 * <p>
 * The application periodically (every 30sec), publishes a list of
 * the last 10 outliers to MQTT.  When enabled, it also publishes 
 * full details of individual outliers as they occur.
 * It also subscribes to MQTT topics for commands to dynamically change the
 * threshold range and whether to publish individual outliers.
 * <p>
 * All MQTT configuration information, including topic patterns,
 * are in the application.properties file.
 * <p>
 * The application logs outlier events in local files.  The actual location
 * is specified in the application.properties file.
 * <p>
 * The application generates some output on stdout and stderr.
 * The information includes:
 * <ul>
 * <li>MQTT device info. Lines 1 through 5 below.</li>
 * <li>URL for the Quarks development console.  Line 6.</li>
 * <li>Trace of the outlier event stream. Line 7.
 *     The output is a label, which includes the active threshold range,
 *     followed by the event's JSON.
 *     These are the events that will also be logged and conditionally published
 *     as well as included in the periodic lastN info published every 30sec.
 *     </li>
 * <li>Announcement when a "change threshold" or "enable publish of 1khz outliers"
 *     command is received and processed.
 *     Line 8 and 9. 
 *     </li>
 * <li>At this time some INFO trace output from the MQTT connector</li>
 * <li>At this time some INFO trace output from the File connector</li>
 * </ul>
 * Sample console output:
 * <pre>{@code
 * [1] MqttDevice serverURLs [tcp://localhost:1883]
 * [2] MqttDevice clientId id/012345
 * [3] MqttDevice deviceId 012345
 * [4] MqttDevice event topic pattern id/012345/evt/+/fmt/json
 * [5] MqttDevice command topic pattern id/012345/cmd/+/fmt/json
 * [6] Quarks Console URL for the job: http://localhost:57324/console
 * [7] sensor1.outside1hzMeanRange[124..129]: {"id":"sensor1","reading":{"N":1000,"MIN":0.0,"MAX":254.0,"MEAN":130.23200000000006,"STDDEV":75.5535473324351},"msec":1454623874408,"agg.begin.msec":1454623873410,"agg.count":1000,"AvgTrailingMean":128,"AvgTrailingMeanCnt":4}
 * ...
 * [8] ===== Changing range to [125..127] ======
 * sensor1.outside1hzMeanRange[125..127]: {"id":"sensor1","reading":{"N":1000,"MIN":0.0,"MAX":254.0,"MEAN":129.00099999999978,"STDDEV":74.3076080870567},"msec":1454624142419,"agg.begin.msec":1454624141420,"agg.count":1000,"AvgTrailingMean":127,"AvgTrailingMeanCnt":30}
 * [9] ===== Changing isPublish1hzOutsideRange to true ======
 * ...
 * }</pre>
 * <p>
 * <h2>Running, observing and controlling the application:</h2>
 * <p>
 * <pre>{@code
 * $ ./runSensorAnalytics.sh
 * }</pre>
 * <p>
 * To observe the locally logged outlier events:
 * <pre>{@code
 * $ tail -f /tmp/SensorAnalytics/logs/.outside1hzMeanRange
 * }</pre>
 * <p>
 * To observe the events that are getting published to MQTT:
 * <pre>{@code
 * $ ./runDeviceComms.sh watch
 * }</pre>
 * <p>
 * To change the outlier threshold setting:
 * <br>The command value is the new range string: {@code [<lowerBound>..<upperBound>]}.
 * <pre>{@code
 * $ ./runDeviceComms.sh send sensor1.set1hzMeanRangeThreshold "[125..127]"
 * }</pre>
 * <p>
 * To change the "publish individual 1hz outliers" control:
 * <pre>{@code
 * $ ./runDeviceComms.sh send sensor1.setPublish1hzOutsideRange true
 * }</pre>
 * <p>
 * <h3>Alternative MQTT clients</h3>
 * You can use any MQTT client but you will have to specify the 
 * MQTT server, the event topics to watch / subscribe to, and the command topics
 * and JSON for publish commands.  The MqttDevice output above provides most
 * of the necessary information.
 * <p>
 * For example, the {@code mosquitto_pub} and
 * {@code mosquitto_sub} commands equivalent to the above runDeviceComms.sh
 * commands are:
 * <pre>{@code
 * # Watch the device's event topics
 * $ /usr/local/bin/mosquitto_sub -t id/012345/evt/+/fmt/json
 * # change the outlier threshold setting
 * $ /usr/local/bin/mosquitto_pub -m '{"value":"[125..127]"}' -t id/012345/cmd/sensor1.set1hzMeanRangeThreshold/fmt/json
 * # change the "publish individual 1hz outliers" control
 * $ /usr/local/bin/mosquitto_pub -m '{"value":"true"}' -t id/012345/cmd/sensor1.setPublish1hzOutsideRange/fmt/json
 * }</pre>
 */
package quarks.samples.apps.sensorAnalytics;
