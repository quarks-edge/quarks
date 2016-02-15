#!/bin/bash

quarks=../../..

# Runs the SensorAnalytics sample application
#
# ./runSensorAnalytics.sh

export CLASSPATH=${quarks}/samples/lib/quarks.samples.apps.jar

java quarks.samples.apps.sensorAnalytics.SensorAnalyticsApplication sensorAnalytics.properties
