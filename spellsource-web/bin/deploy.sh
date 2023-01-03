#!/usr/bin/env bash
set -e
DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
DEPLOYMENT_ENV_PATH="${DIR}"/../../spellsource-private/src/secrets/common-deployment.env
if [[ -e $DEPLOYMENT_ENV_PATH ]] ; then
  VARS=$(<"$DEPLOYMENT_ENV_PATH" xargs)
  export "${VARS?}"
fi

if [[ $(command -v aws) ]] ; then
  AWS_CMD=aws
elif [[ -f "C:\Program Files\Amazon\AWSCLIV2\aws.exe" ]] ; then
  AWS_CMD="C:\Program Files\Amazon\AWSCLIV2\aws.exe"
fi

env $VARS "${AWS_CMD}" s3 sync --acl public-read "${DIR}"/../public s3://www.playspellsource.com
env $VARS "${AWS_CMD}" cloudfront create-invalidation --distribution-id="${PLAYSPELLSOURCE_CLOUDFRONT_DISTRIBUTION_ID}" --paths='/*'
