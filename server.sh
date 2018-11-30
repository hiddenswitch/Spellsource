#!/usr/bin/env bash

if [[ -z "${SPELLSOURCE_APPLICATION}" ]]; then
  APPLICATION="Clustered"
else
  APPLICATION="${SPELLSOURCE_APPLICATION}"
fi

# Executes the fat jar of the network server using the Embedded application by default
java --add-modules java.se \
  --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
  --add-opens java.management/sun.management=ALL-UNNAMED \
  --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
  -javaagent:/data/quasar-core-0.8.0.jar=mb \
  -cp /data/net-1.3.0-all.jar com.hiddenswitch.spellsource.applications.$APPLICATION