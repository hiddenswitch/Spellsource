#!/usr/bin/env bash
set -e
echo "Getting the latest versions of dependencies"
if ! type bundle >/dev/null; then
  gem install bundle >/dev/null
fi
if ! type s3_website >/dev/null; then
  gem install s3_website >/dev/null
fi
echo "Building and deploying..."
bundle
bundle exec jekyll build
spellsource markdown-to-textmesh whatsnew.md > _site/whats-new/index.txt
bundle exec s3_website push
