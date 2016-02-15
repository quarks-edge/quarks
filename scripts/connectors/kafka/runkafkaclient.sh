#!/bin/bash

quarks=../../..

# Runs the Kafka Publisher or Subscriber Client
#
# ./runkafkaclient.sh pub
# ./runkafkaclient.sh sub
# ./runkafkaclient.sh -h

export CLASSPATH=${quarks}/lib/quarks.samples.connectors.jar

java quarks.samples.connectors.kafka.KafkaClient $@
