#!/usr/bin/env bash
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

aws s3 cp --recursive public s3://www.playspellsource.com
# shellcheck source=../secrets/common-deployment.env
. "${DIR}"/../secrets/common-deployment.env
aws cloudfront create-invalidation --distribution-id="${PLAYSPELLSOURCE_CLOUDFRONT_DISTRIBUTION_ID}" --paths='/*'
