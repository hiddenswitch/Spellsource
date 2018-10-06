#!/usr/bin/env bash
set -e
echo Checking AWS environment
pip install awsebcli >/dev/null
echo Building
./build.sh >/dev/null
eb use metastone-dev
eb deploy --staged
