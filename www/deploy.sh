#!/usr/bin/env bash
set -e
echo "Getting the latest versions of dependencies"
npm install
echo "Building and deploying..."
mkdir -pv public/whats-new
npx gatsby build
spellsource markdown-to-textmesh "src/pages-markdown/whatsnew.md" > public/whats-new/index.txt
npx deploy