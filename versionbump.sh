#!/usr/bin/env bash
source .venv/bin/activate
versionbump -c 0.7.6 patch build.gradle setup.py deploy.sh server.sh
