#!/bin/bash

# This script pulls the latest version of Parodos services locally, builds Docker images, runs Docker Compose to start it up.

## folder with script and docker-compose files
export DOCKER_COMPOSE_FILE_PATH="docker-compose"

## function of each step

function header {
   echo "------------------------------------------------------------------------";
   echo " Parodos Running Locally                                                ";
   echo " https://github.com/redhat-developer/parodos/                           ";
   echo "------------------------------------------------------------------------";
}

function check_mvn_install {
	OUTPUT="$(mvn --version)"
	if [[ $OUTPUT == *"Command 'mvn' not found"* ]]; then
  		echo "You need to install mvn to run this script:"
  		echo "Check the link below for the instructions."
  		echo "https://maven.apache.org/install.html"
  		exit 1;
	fi
}

function check_docker_install {
	OUTPUT="$(docker --version)"
	if [[ $OUTPUT == *"Command 'docker' not found"* ]]; then
  		echo "You need Docker to run this script:"
  		echo "Check the link below for the instructions."
  		echo "https://docs.docker.com/get-docker/"
  		exit 1;
	fi
}

function pull_latest_source {
  echo "Getting latest version of Parodos"
  cd .. && git pull
  cd ./$DOCKER_COMPOSE_FILE_PATH || exit 1;
}

function build_source {
  echo "Building latest version of Parodos"
  cd .. && mvn clean install
  cd ./$DOCKER_COMPOSE_FILE_PATH || exit 1;
}

function run {
  echo "Running Parodos locally"
  docker-compose -f docker-compose.yml up
}

## Execution steps

header
check_mvn_install
check_docker_install
#pull_latest_source
build_source
run
