#!/usr/bin/env bash
set -e
echo "Getting the latest versions of dependencies"
if ! type jekyll >/dev/null; then
  gem install jekyll >/dev/null
fi
if ! type s3_website >/dev/null; then
  gem install s3_website >/dev/null
fi
echo "Building and deploying..."
jekyll build
spellsource markdown-to-textmesh whatsnew.md > _site/whats-new/index.txt
s3_website push
