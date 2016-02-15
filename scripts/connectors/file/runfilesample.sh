#!/bin/bash

quarks=../../..

# Runs the File connector sample
#
# ./runfilesample.sh writer
# ./runfilesample.sh reader

sampledir=/tmp/fileConnectorSample
if [ ! -e $sampledir ]; then
    mkdir $sampledir
fi 

export CLASSPATH=${quarks}/samples/lib/quarks.samples.connectors.jar

app=$1; shift
if [ "$app" == "writer" ]; then
    java quarks.samples.connectors.file.FileWriterApp $sampledir
elif [ "$app" == "reader" ]; then
    java quarks.samples.connectors.file.FileReaderApp $sampledir
else
    echo "unrecognized mode '$app'"
    echo "usage: $0 'writer|reader'"
    exit 1
fi
