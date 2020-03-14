#!/usr/bin/env bash
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

set -e
echo "Getting the latest versions of dependencies"
npm install
echo "Building and deploying..."
mkdir -pv public/whats-new
npx gatsby build
spellsource markdown-to-textmesh "src/pages-markdown/whatsnew.md" >public/whats-new/index.txt
aws s3 cp --recursive public s3://www.playspellsource.com
# shellcheck source=../secrets/common-deployment.env
. "${DIR}"/../secrets/common-deployment.env
aws cloudfront create-invalidation --distribution-id="${PLAYSPELLSOURCE_CLOUDFRONT_DISTRIBUTION_ID}" --paths='/*'
