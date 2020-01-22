#!/usr/bin/env bash
set -e
echo "Getting the latest versions of dependencies"
if ! type bundle >/dev/null; then
  gem install bundle >/dev/null
fi
echo "Building and deploying..."
sudo xcode-select --switch /Library/Developer/CommandLineTools
ruby -rrbconfig -e 'puts RbConfig::CONFIG["rubyhdrdir"]'
bundle update --quiet
bundle install --quiet --path vendor/bundle
sudo xcode-select --switch /Applications/Xcode.app
bundle exec jekyll build
spellsource markdown-to-textmesh whatsnew.md > _site/whats-new/index.txt
bundle exec s3_website push
