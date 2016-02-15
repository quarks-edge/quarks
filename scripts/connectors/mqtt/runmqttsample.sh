#!/bin/bash

quarks=../../..

# Runs the Sample MQTT Publisher or Subscriber
#
# ./runmqttsample.sh pub
# ./runmqttsample.sh sub

export CLASSPATH=${quarks}/samples/lib/quarks.samples.connectors.jar

app=$1; shift
if [ "$app" == "pub" ]; then
    java quarks.samples.connectors.mqtt.SimplePublisherApp mqtt.properties
elif [ "$app" == "sub" ]; then
    java quarks.samples.connectors.mqtt.SimpleSubscriberApp mqtt.properties
else
    echo "unrecognized mode '$app'"
    echo "usage: $0 pub|sub"
    exit 1
fi
