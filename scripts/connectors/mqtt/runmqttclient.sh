#!/bin/bash

quarks=../../..

# Runs the MQTT Publisher or Subscriber client
#
# ./runmqttclient.sh pub
# ./runmqttclient.sh sub
# ./runmqttclient.sh -h

export CLASSPATH=${quarks}/samples/lib/quarks.samples.connectors.jar

java quarks.samples.connectors.mqtt.MqttClient $@
