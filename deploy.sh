#!/usr/bin/env bash
pip install awsebcli
set -e
./build.sh
eb use metastone-dev
eb deploy --staged
