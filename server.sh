#!/usr/bin/env bash

if [[ -z "${SPELLSOURCE_APPLICATION}" ]]; then
  APPLICATION="Clustered"
else
  APPLICATION="${SPELLSOURCE_APPLICATION}"
fi

# Executes the fat jar of the network server using the Embedded application by default
java -javaagent:/data/quasar-core-0.7.9-jdk8.jar=mb -cp /data/net-1.3.0-all.jar com.hiddenswitch.spellsource.applications.$APPLICATION >>/var/log/java.log 2>&1