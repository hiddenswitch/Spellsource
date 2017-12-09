#!/usr/bin/env bash
set -e
./build.sh
eb deploy --staged