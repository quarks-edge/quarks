#!/bin/bash

quarks=../../..

# Runs IBM Watson IoT Plaform Quickstart sample.
#
# runiotfqucikstart.sh
#
# This connectors to the Qucikstart IBM Watson IoT Platform service
# which requires no registration at all.
#
# The application prints out a URL which allows a browser
# to see the data being sent from this sample to
# IBM Watson IoT Plaform Quickstart sample.

export CLASSPATH=${quarks}/samples/lib/quarks.samples.connectors.jar

java quarks.samples.connectors.iotf.IotfQuickstart
