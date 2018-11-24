#!/usr/bin/env bash
echo "Getting the latest versions of dependencies"
gem install jekyll >/dev/null
gem install s3_website >/dev/null
echo "Building and deploying..."
jekyll build
s3_website push
