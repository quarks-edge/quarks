#!/bin/bash

quarks=../../..

# Runs the DeviceComms client
#
# ./runDeviceComms.sh watch | send <commandLabel> <commandArg>
#
# no checking is done for the validity of commandLabel or commandArg

export CLASSPATH=${quarks}/samples/lib/quarks.samples.apps.jar

java quarks.samples.apps.mqtt.DeviceCommsApp sensorAnalytics.properties $*
