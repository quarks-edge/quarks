#!/bin/bash

quarks=../../..

# Runs the Sample JDBC Writer or Reader
#
# ./runjdbcsample.sh writer
# ./runjdbcsample.sh reader

if [ -z "$DERBY_HOME" ]; then
    echo "\$DERBY_HOME not defined."
    exit 1;
fi
if [ ! -f $DERBY_HOME/lib/derby.jar ]; then
    echo "\$DERBY_HOME/lib/derby.jar: file not found"
    exit 1;
fi

export CLASSPATH=${quarks}/samples/lib/quarks.samples.connectors.jar:$DERBY_HOME/lib/derby.jar

app=$1; shift
if [ "$app" == "writer" ]; then
    java quarks.samples.connectors.jdbc.SimpleWriterApp jdbc.properties
elif [ "$app" == "reader" ]; then
    java quarks.samples.connectors.jdbc.SimpleReaderApp jdbc.properties
else
    echo "unrecognized mode '$app'"
    echo "usage: $0 writer|reader"
    exit 1
fi
