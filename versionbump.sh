#!/usr/bin/env bash
source .venv/bin/activate
versionbump -c "$1" patch \
  build.gradle \
  setup.py \
  deploy.sh \
  server.sh \
  Dockerfile \
  spellsource/context.py \
  cluster/runsims.sh \
  net/src/main/java/com/hiddenswitch/spellsource/Version.java
