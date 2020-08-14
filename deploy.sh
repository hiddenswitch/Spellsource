#!/usr/bin/env bash
set -e
OPTIND=1
SPELLSOURCE_VERSION=0.8.82

usage="$(basename "$0") [-hvCsS] -- bash source for Spellsource

where:
    -h  show this help text
    -D  installs or updates a virtualenv at VIRTUALENV_PATH=./.venv and other
        binaries for your platform necessary for deployment
    -v  bump the version (requires SPELLSOURCE_VERSION indicating
        the current version)
    -C  builds the stack
    -s  deploys the stack to the specified SSH_HOST, a docker swarm manager
    -S  deploys build macOS and Windows binaries to Steam
    -m  clones the mongo database locally to ./.mongo
"
deploy_steam=false
deploy_stack=false
build_stack=false
bump_version=false
dump_mongo=false
install_dependencies=false
while getopts "hvCsSDm" opt; do
  case "$opt" in
  h*)
    echo "$usage"
    exit
    ;;
  v)
    bump_version=true
    echo "Bumping version"
    ;;
  D)
    install_dependencies=true
    echo "Installing dependencies"
    ;;
  s)
    deploy_stack=true
    echo "Deploying stack"
    ;;
  S)
    deploy_steam=true
    echo "Deploying to Steam"
    ;;
  m)
    dump_mongo=true
    echo "Cloning mongo"
    ;;
  C)
    build_stack=true
    echo "Building and pushing stack"
    ;;
  esac
done
shift $((OPTIND - 1))
[ "${1:-}" = "--" ] && shift

# Configure virtualenv path
if [[ -z ${VIRTUALENV_PATH+x} ]]; then
  VIRTUALENV_PATH="./.venv"
fi

if [[ "$install_dependencies" == true ]]; then
  if test "Darwin" = $(uname); then
    # Install brew, python3, docker, python packages
    if ! command -v brew >/dev/null; then
      echo "Installing brew..."
      /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)" >/dev/null
    fi

    if ! command -v python3 --version >/dev/null; then
      echo "Installing python3..."
      brew install python >/dev/null
    fi

    if ! command -v docker --version >/dev/null; then
      echo "Installing docker..."
      brew cask install docker >/dev/null
    fi

    if ! command -v node --version >/dev/null; then
      echo "Installing node..."
      brew install node >/dev/null
    fi

    if ! command -v mongod >/dev/null; then
      echo "Installing mongod version 3.6"
      brew tap mongodb/brew
      brew install mongodb-community@3.6
      brew link --force mongodb-community@3.6
    fi

    if [[ ! -f ${VIRTUALENV_PATH}/bin/activate ]]; then
      echo "Installing virtualenv at ${VIRTUALENV_PATH}"
      pip3 install -U virtualenv >/dev/null
      virtualenv -p python3 ${VIRTUALENV_PATH}
    fi

    source ${VIRTUALENV_PATH}/bin/activate

    echo "Installing python dependencies"

    if ! command -v spellsource >/dev/null; then
      echo "Installing spellsource from pypy."
      echo "If you'd like to develop this package instead, uninstall from your virtualenv with"
      echo "  source ${VIRTUALENV_PATH}/bin/activate"
      echo "  pip3 uninstall spellsource"
      echo "and install the package locally in editable mode with a valid jar using:"
      echo "  ./gradlew net:shadowJar && pip3 install -e ."
      pip3 install spellsource >/dev/null
    fi

    pip3 install awscli awsebcli bump2version >/dev/null
  else
    echo "Cannot install dependencies on this platform yet"
    exit 1
  fi
fi

if [[ "$bump_version" == true ]]; then
  if [[ -z ${SPELLSOURCE_VERSION+x} ]]; then
    echo "Requires SPELLSOURCE_VERSION to be specified as the current version."
    exit 1
  fi

  if ! command -v bump2version >/dev/null && test -f ${VIRTUALENV_PATH}/bin/activate; then
    echo "Using virtualenv for bump2version package located at ${VIRTUALENV_PATH}"
    source ${VIRTUALENV_PATH}/bin/activate
  fi

  if ! command -v bump2version >/dev/null; then
    echo "Failed to bump version: Missing bump2version binary. Install with pip3 install bump2version"
    exit 1
  fi

  new_version=$(bump2version --allow-dirty --current-version ${SPELLSOURCE_VERSION} --dry-run --list patch | grep new_version | sed s,"^.*=",,)
  bump2version --allow-dirty --current-version "${SPELLSOURCE_VERSION}" patch \
    build.gradle \
    python/setup.py \
    deploy.sh \
    python/spellsource/context.py \
    core/src/main/java/com/hiddenswitch/spellsource/core/Version.java \
    gradle.properties
  SPELLSOURCE_VERSION=new_version
fi

# Configure the gradle command
if [[ -z ${GRADLE_CMD+x} ]]; then
  if test "$CI" = "true" || ! command -v gradle >/dev/null; then
    GRADLE_CMD="./gradlew"
  else
    GRADLE_CMD=gradle
  fi
fi

if [[ "$build_stack" == true ]]; then
  # Explicitly delete secrets here
  # shellcheck disable=SC2046
  unset $(grep -v '^#' "secrets/spellsource/stack-application-production.env" | sed -E 's/(.*)=.*/\1/' | xargs)
  export COMPOSE_DOCKER_CLI_BUILD=1 DOCKER_BUILDKIT=1
  docker context use default
  docker-compose build
  docker-compose push
fi

if [[ "$deploy_stack" == true ]]; then
  # Only used in this script
  source "secrets/common-deployment.env"
  docker context import hiddenswitch secrets/hiddenswitch.dockercontext >/dev/null || true
  docker context use hiddenswitch >/dev/null
  # shellcheck disable=SC2046
  env $(cat "secrets/spellsource/stack-application-production.env") docker-compose config |
    ssh -i "secrets/deployment.rsa" doctorpangloss@"${SSH_HOST}" docker stack deploy --prune --resolve-image always --compose-file - spellsource
fi

if [[ "$deploy_steam" == true ]]; then
  # should be executed with local docker
  docker context use default
  docker build -t steamguardcli -f steamguardcli/Dockerfile ./steamguardcli
  source "secrets/spellsource/unityclient-build.env"
  STEAMCMD_GUARD_CODE="$(docker run --rm -v="$(pwd)"/secrets/spellsource/maFiles:/data steamguardcli mono /build/steamguard --mafiles-path /data generate-code)"
  docker run -v="$(pwd):/home/steam/workdir" -w="/home/steam/workdir" cm2network/steamcmd /home/steam/steamcmd/steamcmd.sh \
    +login "${STEAMCMD_ACCOUNT}" "${STEAMCMD_PASSWORD}" "${STEAMCMD_GUARD_CODE}" \
    +run_app_build_http "/home/steam/workdir/secrets/spellsource/steam-app-build.vdf" \
    +quit
fi

if [[ ${dump_mongo} == true ]]; then
  dump_dir=$(mktemp -d)
  archive_path="${dump_dir}"/dump.tar.gz
  echo "Archive Path: ${archive_path}"

  docker context import hiddenswitch secrets/hiddenswitch.dockercontext >/dev/null || true
  docker context use hiddenswitch >/dev/null
  docker run --network=backend -i --rm mongo:3.6 bash -c "mkdir -pv ./out >/dev/null; mongodump  --excludeCollection=games --uri=mongodb://spellsource_mongo:27017/metastone >/dev/null; tar -czvf ./archive.tar.gz ./dump >/dev/null; cat archive.tar.gz" >"${archive_path}"
  mkdir -pv .mongo
  docker context use default >/dev/null
  mongo_pid=$(docker run --rm -v="$(pwd)"/.mongo:/data/db -p="27017:27017" -d mongo:3.6)
  echo "mongod pid: ${mongo_pid}"
  tar -C "${dump_dir}" -xzvf "${archive_path}"
  mongorestore --drop --uri=mongodb://localhost:27017/metastone "${dump_dir}"/dump
  docker stop ${mongo_pid}
fi
