#!/bin/bash

quarks=../../..

# Runs the Sample Kafka Publisher or Subscriber
#
# ./runkafkasample.sh pub
# ./runkafkasample.sh sub

export CLASSPATH=${quarks}/samples/lib/quarks.samples.connectors.jar

app=$1; shift
if [ "$app" == "pub" ]; then
    java quarks.samples.connectors.kafka.SimplePublisherApp kafka.properties
elif [ "$app" == "sub" ]; then
    java quarks.samples.connectors.kafka.SimpleSubscriberApp kafka.properties
else
    echo "unrecognized mode '$app'"
    echo "usage: $0 pub|sub"
    exit 1
fi
