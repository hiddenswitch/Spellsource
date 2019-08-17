#!/usr/bin/env bash
set -e
OPTIND=1
SPELLSOURCE_VERSION=0.8.48

usage="$(basename "$0") [-hcedwpvlWDA] -- build and deploy the Spellsource Server

where:
    -h  show this help text
    -c  build the client libraries in swagger. If a Spellsource-Client folder
        is a sibling of the current working directory, the client there is
        updated too
    -e  deploy for Elastic Beanstalk
    -d  deploy for Docker (requires logged-in docker hub account, optionally
        PORTAINER_URL, PORTAINER_USERNAME, and PORTAINER_PASSWORD)
    -l  builds and deploys the launcher
    -p  deploy for Python (optionally TWINE_USERNAME, TWINE_PASSWORD)
    -w  deploy playspellsource.com (requires spellsource on the command line)
    -W  deploy wiki.hiddenswitch.com
    -j  deploy JARs to Maven Central (requires signing assets)
    -v  bump the version (requires SPELLSOURCE_VERSION indicating
        the current version)
    -D  installs or updates a virtualenv at VIRTUALENV_PATH=./.venv and other
        binaries for your platform necessary for deployment
    -A  visits the AWS console for Hidden Switch

Invoking this script always rebuilds Spellsource-Server.

Notes for successful deployment:
 - Requires jq, curl and docker on the PATH for Docker deployment
 - Requires eb on the path for Elastic Beanstalk deployment

For example, to build the client library, bump the version and deploy to docker,
python and playspellsource.com:

  ./deploy.sh -cpdwv
"
deploy_elastic_beanstalk=false
deploy_docker=false
deploy_www=false
deploy_python=false
deploy_launcher=false
deploy_wiki=false
bump_version=false
install_dependencies=false
deploy_java=false
build_client=false
while getopts "hcedwpjvlWDA" opt; do
  case "$opt" in
  h) echo "$usage"
     exit
     ;;
  e) deploy_elastic_beanstalk=true
     echo "Deploying for Elastic Beanstalk"
     ;;
  c) build_client=true
     echo "Building swagger client libraries"
     ;;
  d) deploy_docker=true
     echo "Deploying for Docker"
     ;;
  W) deploy_wiki=true
     echo "Deploying mediawiki"
     ;;
  j) deploy_java=true
     echo "Deploying to Maven"
     ;;
  l) deploy_launcher=true
     echo "Deploying launcher"
     ;;
  p) deploy_python=true
     echo "Deploying for Python"
     ;;
  w) deploy_www=true
     echo "Deploying playspellsource.com"
     ;;
  v) bump_version=true
     echo "Bumping version"
     ;;
  D) install_dependencies=true
     echo "Installing dependencies"
     ;;
  A) open https://786922801148.signin.aws.amazon.com/console
     exit
     ;;
  esac
done
shift $((OPTIND-1))
[ "${1:-}" = "--" ] && shift

function update_portainer() {
  service_name=$1
  portainer_image_name=$2
  # It takes a while for docker hub to process all the metadata for an image, unfortunately.
  sleep 20
  service=$(curl -s -H "Authorization: Bearer ${PORTAINER_BEARER_TOKEN}" "${PORTAINER_URL}api/endpoints/1/docker/services" | jq -c ".[] | select( .Spec.Name==(\"$service_name\"))")
  service_id=$(echo $service | jq  -r .ID)
  service_specification=$(echo $service | jq .Spec)
  service_version=$(echo $service | jq .Version.Index)
  service_update_command=$(echo $service_specification | jq ".TaskTemplate.ContainerSpec.Image |= \"${portainer_image_name}\" " | jq ".TaskTemplate.ForceUpdate |= 1 ")

  # Update the container image
  curl --fail -H "Content-Type: application/json" \
   -H "Authorization: Bearer ${PORTAINER_BEARER_TOKEN}" \
   -X POST \
   -d "${service_update_command}" \
   "${PORTAINER_URL}api/endpoints/1/docker/services/${service_id}/update?version=${service_version}"

   echo "Deployed image"
}

# Configure virtualenv path
if [[ -z ${VIRTUALENV_PATH+x} ]] ; then
  VIRTUALENV_PATH="./.venv"
fi

if [[ "$install_dependencies" = true ]] ; then
  if test "Darwin" = $(uname) ; then
    # Install brew, java, jq, python3, docker, python packages
    if ! command -v brew > /dev/null ; then
      echo "Installing brew..."
      /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)" > /dev/null
    fi

    if ! command -v java -version > /dev/null ; then
      echo "Installing java..."
      brew cask install java
    fi

    if ! command -v python3 --version > /dev/null ; then
      echo "Installing python3..."
      brew install python3 > /dev/null
    fi

    if ! command -v jq --version > /dev/null ; then
      echo "Installing jq..."
      brew install jq > /dev/null
    fi

    if ! command -v docker --version > /dev/null ; then
      echo "Installing docker..."
      brew cask install docker
    fi

    if ! command -v meteor --version > /dev/null ; then
      echo "Installing meteor..."
      curl https://install.meteor.com/ | sh
    fi

    if ! command -v mongod > /dev/null ; then
      echo "Installing mongod version 3.6"
      brew install mongodb@3.6
      brew link --force mongodb@3.6
    fi

    if [[ ! -f ${VIRTUALENV_PATH}/bin/activate ]] ; then
      echo "Installing virtualenv at ${VIRTUALENV_PATH}"
      pip3 install -U virtualenv > /dev/null
      virtualenv -p python3 ${VIRTUALENV_PATH}
    fi

    source ${VIRTUALENV_PATH}/bin/activate

    echo "Installing python dependencies"

    if ! command -v spellsource > /dev/null ; then
      echo "Installing spellsource from pypy."
      echo "If you'd like to develop this package instead, uninstall from your virtualenv with"
      echo "  source ${VIRTUALENV_PATH}/bin/activate"
      echo "  pip3 uninstall spellsource"
      echo "and install the package locally in editable mode with a valid jar using:"
      echo "  ./gradlew net:shadowJar && pip3 install -e ."
      pip3 install spellsource > /dev/null
    fi

    pip3 install awscli awsebcli bump2version twine > /dev/null
  else
    echo "Cannot install dependencies on this platform yet"
    exit 1
  fi
fi

if [[ "$bump_version" = true ]] ; then
  if [[ -z ${SPELLSOURCE_VERSION+x} ]] ; then
    echo "Requires SPELLSOURCE_VERSION to be specified as the current version."
    exit 1
  fi

  if ! command -v bump2version > /dev/null && test -f ${VIRTUALENV_PATH}/bin/activate ; then
    echo "Using virtualenv for bump2version package located at ${VIRTUALENV_PATH}"
    source ${VIRTUALENV_PATH}/bin/activate
  fi

  if ! command -v bump2version > /dev/null ; then
    echo "Failed to bump version: Missing bump2version binary. Install with pip3 install bump2version"
    exit 1
  fi

  new_version=$(bump2version --allow-dirty --current-version ${SPELLSOURCE_VERSION} --dry-run --list patch | grep new_version  | sed s,"^.*=",,)
  bump2version --allow-dirty --current-version "${SPELLSOURCE_VERSION}" patch \
    build.gradle \
    setup.py \
    deploy.sh \
    server.sh \
    Dockerfile \
    spellsource/context.py \
    net/src/main/java/com/hiddenswitch/spellsource/Version.java \
    gradle.properties
  SPELLSOURCE_VERSION=new_version
fi

# Configure the gradle command
if test "$CI" = "true" || ! command -v gradle > /dev/null ; then
  export GRADLE_CMD="./gradlew"
else
  export GRADLE_CMD=gradle
fi

if [[ "$build_client" = true ]] ; then
  rm -rf "./client/"
  ${GRADLE_CMD} swagger

  if [[ -d "../Spellsource-Client" ]] ; then
    mkdir -pv "clientcsharp"
    INPUT_DIR="clientcsharp"
    OUTPUT_DIR="../Spellsource-Client/Assets/Plugins/Client"
    ${GRADLE_CMD} swaggerClient
    # Remove a lot of unnecessary files from the Unity project
    rm -rf ${OUTPUT_DIR}
    mv ${INPUT_DIR} ${OUTPUT_DIR}
    rm -rf ${INPUT_DIR}
    rm -rf ${OUTPUT_DIR}/src/
    rm -rf ${OUTPUT_DIR}/docs/
    rm -f ${OUTPUT_DIR}/build.bat
    rm -f ${OUTPUT_DIR}/build.sh
    rm -f ${OUTPUT_DIR}/git_push.sh
    rm -f ${OUTPUT_DIR}/Spellsource.Client.sln
    rm -f ${OUTPUT_DIR}/mono_nunit_test.sh
    rm -f ${OUTPUT_DIR}/README.md
    rm -rf ${OUTPUT_DIR}/Scripts/Spellsource.Client/Properties
    rm -f ${OUTPUT_DIR}/Scripts/Spellsource.Client/packages.config
    rm -rf ${OUTPUT_DIR}/Scripts/Spellsource.Client/Api
    rm -f ${OUTPUT_DIR}/Scripts/Spellsource.Client/Client/ApiClient.cs
    rm -f ${OUTPUT_DIR}/Scripts/Spellsource.Client/Client/ApiException.cs
    rm -f ${OUTPUT_DIR}/Scripts/Spellsource.Client/Client/ApiResponse.cs
    rm -f ${OUTPUT_DIR}/Scripts/Spellsource.Client/Client/Configuration.cs
    rm -f ${OUTPUT_DIR}/Scripts/Spellsource.Client/Client/ExceptionFactory.cs
    rm -f ${OUTPUT_DIR}/Scripts/Spellsource.Client/Client/GlobalConfiguration.cs
    rm -f ${OUTPUT_DIR}/Scripts/Spellsource.Client/Client/IApiAccessor.cs
    rm -f ${OUTPUT_DIR}/Scripts/Spellsource.Client/Client/IReadableConfiguration.cs
  fi
fi

if [[ "$deploy_java" = true ]] ; then
  ${GRADLE_CMD} uploadArchives  --no-daemon --no-parallel >/dev/null \
    && echo "Successfully uploaded to maven. Navigating you to Sonatype if your platform supports it..."
  if [[ -x "$(command -v open)" ]] ; then
    open "https://oss.sonatype.org/#stagingRepositories"
  fi
fi

# Before building, retrieve the portainer password if it's not specified immediately

if [[ "$deploy_docker" = true || "$deploy_launcher" = true || "$deploy_wiki" = true ]] ; then
  if [[ -z ${PORTAINER_PASSWORD+x} ]] ; then
    echo "docker deployment: Requesting PORTAINER_PASSWORD"
    stty -echo
    printf "Password: "
    read PORTAINER_PASSWORD
    stty echo
    printf "\n"
  fi

  if [[ -z ${PORTAINER_URL+x} ]] ; then
    PORTAINER_URL="http://hs-1.i.hiddenswitch.com:9000/"
  fi

  if [[ -z ${PORTAINER_USERNAME+x} ]] ; then
    PORTAINER_USERNAME="doctorpangloss"
  fi

  # Authenticate with portainer
  if [[ -z ${PORTAINER_BEARER_TOKEN+x} ]] ; then
    { # try
      PORTAINER_BEARER_TOKEN=$(curl -s --fail -H "Content-Type: application/json" -X POST \
        -d "{\"Username\":\"${PORTAINER_USERNAME}\", \"Password\": \"${PORTAINER_PASSWORD}\"}" \
        "${PORTAINER_URL}api/auth" | \
      jq --raw-output '.jwt')
    } || { # catch
      echo "Invalid portainer URL, username or password"
      exit 1
    }
  fi
fi

# Before building for python, check that we have the twine username and password
if [[ "$deploy_python" = true && -z ${TWINE_USERNAME+x} ]] ; then
  echo "docker deployment: Requesting TWINE_USERNAME"
  printf "Twine Username: "
  read TWINE_USERNAME
  printf "\n"
fi

if [[ "$deploy_python" = true && -z ${TWINE_PASSWORD+x} ]] ; then
  echo "docker deployment: Requesting TWINE_PASSWORD"
  stty -echo
  printf "Password: "
  read TWINE_PASSWORD
  stty echo
  printf "\n"
fi

if [[ "$deploy_launcher" = true ]] ; then
  cd launcher

  meteor npm install --save localforage
  meteor build --server-only --architecture os.linux.x86_64 --directory ./

  # Build image and upload to docker
  { # try
    echo "Building and uploading launcher Docker image"
    docker build -t launcher . > /dev/null && \
    rm -rf bundle && \
    docker tag launcher doctorpangloss/launcher > /dev/null && \
    docker push doctorpangloss/launcher:latest > /dev/null
  } || { # catch
    echo "Failed to build or upload Docker image. Make sure you're logged into docker hub"
    exit 1
  }

  cd ..

  { # try
    # Figure out the service ID
    service_name=spellsource_launcher
    portainer_image_name="doctorpangloss/launcher:latest"
    update_portainer ${service_name} ${portainer_image_name}
  } || { # catch
    echo "Failed to update launcher service"
    exit 1
  }
fi

if [[ "$deploy_wiki" = true ]] ; then
  cd mediawiki

  # Build image and upload to docker
  { # try
    echo "Building and uploading wiki Docker image"
    docker build -t  doctorpangloss/wiki . >/dev/null && \
    docker push doctorpangloss/wiki:latest >/dev/null
  } || { # catch
    echo "Failed to build or upload Docker image. Make sure you're logged into docker hub"
    exit 1
  }

  cd ..

  { # try
    # Figure out the service ID
    service_name=spellsource_mediawiki
    portainer_image_name="doctorpangloss/wiki:latest"
    update_portainer ${service_name} ${portainer_image_name}
  } || { # catch
    echo "Failed to update wiki service"
    exit 1
  }
fi

if [[ "$deploy_elastic_beanstalk" = true || "$deploy_docker" = true || "$deploy_python" = true ]] ; then
  echo "Building Spellsource JAR file"
  { # try
    # Build the server
    ${GRADLE_CMD} net:shadowJar 2>&1 > /dev/null
  } || { # catch
    echo "Failed to build. Try running ${GRADLE_CMD} net:shadowJar and check for errors."
    exit 1
  }

  if [[ ! -e "net/build/libs/net-${SPELLSOURCE_VERSION}.jar" ]] ; then
    echo "Failed to build. jar not found!"
    exit 1
  fi
fi

if [[ "$deploy_www" = true ]] ; then
  if ! command -v spellsource && test -f ${VIRTUALENV_PATH}/bin/activate ; then
    echo "Using virtualenv for spellsource package located at ${VIRTUALENV_PATH}"
    source ${VIRTUALENV_PATH}/bin/activate
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

  # Build image and upload to docker
  echo "Building and uploading Docker image"
  docker build -t doctorpangloss/spellsource . && \
  docker tag doctorpangloss/spellsource doctorpangloss/spellsource:${SPELLSOURCE_VERSION} && \
  docker tag doctorpangloss/spellsource doctorpangloss/spellsource:latest && \
  docker push doctorpangloss/spellsource:latest


  # Update specific service for now instead of stack
  { # try
    # Figure out the service ID
    service_name=spellsource_game
    portainer_image_name="doctorpangloss/spellsource:latest"
    update_portainer ${service_name} ${portainer_image_name}
  } || { # catch
    echo "Failed to update service"
    exit 1
  }
fi

if [[ "$deploy_python" = true ]] ; then
  if ! command -v twine > /dev/null && test -f ${VIRTUALENV_PATH}/bin/activate ; then
    echo "Using virtualenv for twine package located at ${VIRTUALENV_PATH}"
    source ${VIRTUALENV_PATH}/bin/activate
  fi

  if ! command -v twine > /dev/null ; then
    echo "Failed to deploy python: Missing twine binary. Install with pip3 install twine"
    exit 1
  fi
  rm -rf dist/
  mkdir -pv dist
  pip3 install wheel twine >/dev/null
  python3 setup.py sdist bdist_wheel >/dev/null
  echo Deploying
  TWINE_USERNAME=${TWINE_USERNAME} TWINE_PASSWORD=${TWINE_PASSWORD} twine upload dist/*
  rm -rf dist/
fi

if [[ "$deploy_elastic_beanstalk" = true ]] ; then
    if ! command -v eb > /dev/null && test -f ${VIRTUALENV_PATH}/bin/activate ; then
    echo "Using virtualenv for twine package located at ${VIRTUALENV_PATH}"
    source ${VIRTUALENV_PATH}/bin/activate
  fi

  if ! command -v eb > /dev/null ; then
    echo "Failed to deploy ELB python: Missing eb binary. Install with pip3 install awsebcli"
    exit 1
  fi

  # Package the two necessary files into the right places into a zip file
  rm artifact.zip || true
  zip artifact.zip \
      ./Dockerfile \
      ./Dockerrun.aws.json \
      ./net/build/libs/net-0.8.48.jar \
      ./server.sh >/dev/null

  eb use metastone-dev >/dev/null
  eb deploy --staged
fi
