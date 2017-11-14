#!/usr/bin/env bash

# Executes the fat jar of the network server using the Embedded application by default
java -javaagent:/data/quasar-core-0.7.5-jdk8.jar -cp /data/net-1.3.0-all.jar com.hiddenswitch.spellsource.applications.Embedded >>/var/log/java.log 2>&1