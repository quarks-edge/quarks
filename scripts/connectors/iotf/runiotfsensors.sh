#!/bin/bash

quarks=../../..

# Runs IBM Watson IoT Plaform sample.
#
# runiotfsensors.sh path/device.cfg
#
# e.g. runiotfsensors.sh $HOME/device.cfg
#
# This connectors to your IBM Watson IoT Platform service
# as the device defined in the device.cfg.
# The format of device.cfg is the standard one for
# IBM Watson IoT Platform and a sample is in this directory
# (omitting values for the authorization tokens).

export CLASSPATH=${quarks}/samples/lib/quarks.samples.connectors.jar

java quarks.samples.connectors.iotf.IotfSensors $1
