#!/usr/bin/env bash

# This file executes the shadow / fat jar of the server on its docker image, using PORT=80 by default.

if [[ -z "${SPELLSOURCE_VERSION+x}" ]]; then
  SPELLSOURCE_VERSION=0.8.59
fi

# Executes the fat jar of the network server using the Embedded application by default
java --add-modules java.se \
  --add-opens java.base/java.util.concurrent.atomic=kryo \
  --add-opens java.base/java.lang=co.paralleluniverse.quasar.core \
  --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
  --add-opens java.management/sun.management=ALL-UNNAMED \
  --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
  -cp /data/net-${SPELLSOURCE_VERSION}-all.jar com.hiddenswitch.spellsource.applications.net.Clustered