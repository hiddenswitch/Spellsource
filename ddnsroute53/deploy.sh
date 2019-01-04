#!/usr/bin/env bash
set -e
OPTIND=1

usage="$(basename "$0") [-hwmuiadt] -- deploy a Route53 DynDNS path

where:
    -h  show this help text

Requires the HOSTED_ZONE_ID and SHARED_SECRET environment variables set. See README.md for more.
"
while getopts "hwmuiatd" opt; do
  case "$opt" in
  h) echo "$usage"
     exit
     ;;
  esac
done
shift $((OPTIND-1))
[ "${1:-}" = "--" ] && shift

serverless deploy -v --hosted_zone_id "${HOSTED_ZONE_ID}" --shared_secret "${SHARED_SECRET}"