#!/usr/bin/env bash

# This file executes the shadow / fat jar of the server on its docker image, using PORT=80 by default.

if [[ -z "${SPELLSOURCE_VERSION+x}" ]]; then
  SPELLSOURCE_VERSION=0.8.59
fi

# Executes the fat jar of the network server using the Embedded application by default
java -cp /data/net-${SPELLSOURCE_VERSION}-all.jar com.hiddenswitch.spellsource.applications.net.Clustered