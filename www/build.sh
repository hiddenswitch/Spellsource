#!/usr/bin/env bash
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
source ../.venv/bin/activate
set -e
echo "Getting the latest versions of dependencies"
npm install
echo "Building and deploying..."
mkdir -pv public/whats-new
npx gatsby build
spellsource markdown-to-textmesh "src/pages-markdown/whatsnew.md" >public/whats-new/index.txt