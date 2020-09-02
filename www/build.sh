#!/usr/bin/env bash
set -e
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
source "${DIR}"/../.venv/bin/activate
pip3 install -e "${DIR}"/../python
echo "Getting the latest versions of dependencies"
CXX="clang++ -I${JAVA_HOME:-`/usr/libexec/java_home`}/include/darwin/" npm install
echo "Building and deploying..."
mkdir -pv public/whats-new
npx gatsby build
spellsource markdown-to-textmesh "src/pages-markdown/whatsnew.md" >public/whats-new/index.txt