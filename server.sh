#!/usr/bin/env bash

if [[ -z "${SPELLSOURCE_APPLICATION}" ]]; then
  SPELLSOURCE_APPLICATION="Clustered"
fi

if [[ -z "${SPELLSOURCE_VERSION}" ]]; then
  SPELLSOURCE_VERSION=0.8.5
fi

# Executes the fat jar of the network server using the Embedded application by default
java --add-modules java.se \
  --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
  --add-opens java.management/sun.management=ALL-UNNAMED \
  --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
  -cp /data/net-${SPELLSOURCE_VERSION}-all.jar com.hiddenswitch.spellsource.applications.${SPELLSOURCE_APPLICATION}