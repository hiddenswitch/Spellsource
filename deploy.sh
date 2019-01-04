#!/usr/bin/env bash
set -e
OPTIND=1

usage="$(basename "$0") [-hed] -- build and deploy the Spellsource Server

where:
    -h  show this help text
    -e  deploy for Elastic Beanstalk
    -d  deploy for Docker (requires logged-in docker hub account, PORTAINER_URL,
        PORTAINER_USERNAME, optionally PORTAINER_PASSWORD)
    -w  deploy playspellsource.com (requires spellsource on the command line)

Currently, the docker deployment only updates the spellsource_game image.

Notes for successful deployment:
 - Requires jq, curl and docker on the PATH for Docker deployment
 - Requires eb on the path for Elastic Beanstalk deployment
 - Make sure to bump a version using ./versionbump.sh CURRENT.VERSION
"
deploy_elastic_beanstalk=false
deploy_docker=false
deploy_www=false
while getopts "hedw" opt; do
  case "$opt" in
  h) echo "$usage"
     exit
     ;;
  e) deploy_elastic_beanstalk=true
     echo "Deploying for Elastic Beanstalk"
     ;;
  d) deploy_docker=true
     echo "Deploying for Docker"
     ;;
  w) deploy_www=true
     echo "Deploying playspellsource.com"
     ;;
  esac
done
shift $((OPTIND-1))
[ "${1:-}" = "--" ] && shift

# Configure the gradle command
if test "$CI" = "true" || ! command -v gradle > /dev/null ; then
  export GRADLE_CMD="./gradlew"
else
  export GRADLE_CMD=gradle
fi

# Before building, retrieve the portainer password if it's not specified immediately
if [[ "$deploy_docker" = true && -z ${PORTAINER_PASSWORD+x} ]] ; then
  echo "docker deployment: Requesting PORTAINER_PASSWORD"
  stty -echo
  printf "Password:"
  read PORTAINER_PASSWORD
  stty echo
  printf "\n"
fi

if [[ "$deploy_elastic_beanstalk" = true || "$deploy_docker" = true ]] ; then
  echo "Building Spellsource JAR file"
  { # try
    # Builds the swagger-api specified files
    ./client.sh 2>&1 > /dev/null && \
    # Build the server
    ${GRADLE_CMD} clean 2>&1 > /dev/null && \
    ${GRADLE_CMD} net:shadowJar 2>&1 > /dev/null
  } || { # catch
    echo "Failed to build. Try running ${GRADLE_CMD} net:shadowJar and check for errors."
    exit 1
  }
fi

if [[ "$deploy_www" = true ]] ; then
  if ! command -v spellsource && test -f .venv/bin/activate ; then
    echo "Using virtualenv for spellsource package located at .venv/"
    source .venv/bin/activate
  fi

  if ! command -v spellsource ; then
    echo "Failed to deploy playspellsource.com: Missing spellsource binary. Install with pip3 install -e ."
    exit 1
  fi

  cd www
  ./deploy.sh
  cd ..
  echo "Deployed web"
fi

if [[ "$deploy_docker" = true ]] ; then
  if [[ -z ${PORTAINER_URL+x} ]] ; then
    PORTAINER_URL="http://hs-1.i.hiddenswitch.com:9000/"
  fi

  if [[ -z ${PORTAINER_USERNAME+x} ]] ; then
    PORTAINER_USERNAME="doctorpangloss"
  fi

  if [[ -z ${PORTAINER_IMAGE_NAME+x} ]] ; then
    PORTAINER_IMAGE_NAME="doctorpangloss/spellsource:latest"
  fi

  # Authenticate with portainer
  bearer_token=""
  { # try
    bearer_token=$(curl -s --fail -H "Content-Type: application/json" -X POST \
      -d "{\"Username\":\"${PORTAINER_USERNAME}\", \"Password\": \"${PORTAINER_PASSWORD}\"}" \
      "${PORTAINER_URL}api/auth" | \
    jq --raw-output '.jwt')
  } || { # catch
    echo "Invalid portainer URL, username or password"
    exit 1
  }

  # Build image and upload to docker
  { # try
    echo "Building and uploading Docker image"
    docker build -t spellsource . > /dev/null && \
    docker tag spellsource doctorpangloss/spellsource > /dev/null && \
    docker push doctorpangloss/spellsource:latest > /dev/null
  } || { # catch
    echo "Failed to build or upload Docker image. Make sure you're logged into docker hub"
    exit 1
  }

  # Update specific service for now instead of stack
  { # try
    # Figure out the service ID
    service_name=spellsource_game
    service=$(curl -s -H "Authorization: Bearer ${bearer_token}" "${PORTAINER_URL}api/endpoints/1/docker/services" | jq -c ".[] | select( .Spec.Name==(\"$service_name\"))")
    service_id=$(echo $service | jq  -r .ID)
    service_specification=$(echo $service | jq .Spec)
    service_version=$(echo $service | jq .Version.Index)
    service_update_command=$(echo $service_specification | jq ".TaskTemplate.ContainerSpec.Image |= \"${PORTAINER_IMAGE_NAME}\" " | jq ".TaskTemplate.ForceUpdate |= 1 ")

    # Update the container image
    curl --fail -H "Content-Type: application/json" \
     -H "Authorization: Bearer ${bearer_token}" \
     -X POST \
     -d "${service_update_command}" \
     "${PORTAINER_URL}api/endpoints/1/docker/services/${service_id}/update?version=${service_version}"
  } || { # catch
    echo "Failed to update service"
    exit 1
  }
fi

if [[ "$deploy_elastic_beanstalk" = true ]] ; then
  echo "Checking AWS environment"
  pip install awsebcli >/dev/null

  # Package the two necessary files into the right places into a zip file
  rm artifact.zip || true
  zip artifact.zip \
      ./Dockerfile \
      ./Dockerrun.aws.json \
      ./net/build/libs/net-0.8.8-all.jar \
      ./server.sh >/dev/null

  eb use metastone-dev >/dev/null
  eb deploy --staged
fi
