#!/usr/bin/env bash
set -e
DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
VARS=$(cat "${DIR}"/../../spellsource-private/src/secrets/common-deployment.env | xargs)
export $VARS

if [[ $(command -v aws) ]] ; then
  AWS_CMD=aws
elif [[ -f "C:\Program Files\Amazon\AWSCLIV2\aws.exe" ]] ; then
  AWS_CMD="C:\Program Files\Amazon\AWSCLIV2\aws.exe"
fi

env $VARS "${AWS_CMD}" s3 cp --recursive "${DIR}"/../public s3://www.playspellsource.com
env $VARS "${AWS_CMD}" cloudfront create-invalidation --distribution-id="${PLAYSPELLSOURCE_CLOUDFRONT_DISTRIBUTION_ID}" --paths='/*'
