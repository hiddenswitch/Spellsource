#!/usr/bin/env bash
set -e
source .venv/bin/activate
rm -rf dist/
mkdir -pv dist
pip3 install wheel twine >/dev/null
echo Building
gradle net:shadowJar
python3 setup.py sdist bdist_wheel >/dev/null
echo Deploying
twine upload dist/*
