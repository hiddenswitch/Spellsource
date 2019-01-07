#!/usr/bin/env bash

if [[ -z "${METEOR_SETTINGS}" ]]; then
  METEOR_SETTINGS="$(cat /data/settings.json)"
fi

METEOR_SETTINGS="${METEOR_SETTINGS}" PORT=3000 node /data/main.js