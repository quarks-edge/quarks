#!/bin/bash

quarks=../../..

# Runs the Sample MQTT Publisher or Subscriber
#
# ./runmqttsample.sh pub
# ./runmqttsample.sh sub

export CLASSPATH=${quarks}/samples/lib/quarks.samples.connectors.jar

java quarks.samples.connectors.iotf.IoTFSensors $1
