# Docker command to use. Use of podman is highly adviced.
DOCKER ?= docker
DOCKER-COMPOSE ?= docker-compose

# maven
MAVEN ?= mvn

# set quiet mode by default
Q=@

ORG=quay.io/parados/
WORKFLOW_SERVICE_IMAGE=workflow-service
NOTIFICATION_SERVICE_IMAGE=notification-service

# get version from pom
VERSION = $(shell sed -n "s/<revision>\(.*\)<\/revision>/\1/p" $(PWD)/pom.xml | sed 's/\ *//g')

GIT_BRANCH := $(shell git rev-parse --abbrev-ref HEAD | sed s,^main$$,latest,g)
GIT_HASH := $(shell git rev-parse HEAD)

JAVA_VERSION_MAX_SUPPORTED=11
JAVA_VERSION_MIN_SUPPORTED=11
# Setting SHELL to bash allows bash commands to be executed by recipes.
# This is a requirement for 'setup-envtest.sh' in the test target.
# Options are set to exit when a recipe line exits non-zero or a piped command fails.
SHELL = /usr/bin/env bash -o pipefail
.SHELLFLAGS = -ec

# check if maven is installed
ifeq (,$(shell which $(MAVEN)))
  $(error "No maven found in $(PATH). Please install maven")
else
  MVN_JAVA_VERSION=$(shell mvn --version | egrep -o "Java version: [[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+"| head -n 1 | cut -c15-16)
  MVN_VERSION_IS_SUPPORTED=$(shell [ $(MVN_JAVA_VERSION) -lt $(JAVA_VERSION_MAX_SUPPORTED)  ] && [ $(MVN_JAVA_VERSION) -gt $(JAVA_VERSION_MIN_SUPPORTED)  ] && echo true)
  ifeq ($(MVN_VERSION_IS_SUPPORTED), )
    $(error "Maven Java $(MVN_JAVA_VERSION) version should be [$(JAVA_VERSION_MIN_SUPPORTED) ; $(JAVA_VERSION_MAX_SUPPORTED)]. Please use Java within these bounds with mvn")
  endif
endif
# check java version
ifeq (,$(shell java --version))
  $(error "No java found in $(PATH). Please install java >= 11")
else
  JAVA_VERSION=$(shell java --version | egrep -o "[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+" | head -n 1 | cut -c1-2)
  $(info $$JAVA_VERSION is [${JAVA_VERSION}])
  JAVA_VERSION_IS_SUPPORTED=$(shell [ $(JAVA_VERSION) -lt $(JAVA_VERSION_MAX_SUPPORTED)  ] && [ $(JAVA_VERSION) -gt $(JAVA_VERSION_MIN_SUPPORTED)  ]&& echo true)
	ifeq ($(JAVA_VERSION_IS_SUPPORTED), )
		$(error "Java version $(JAVA_VERSION) should be [$(JAVA_VERSION_MIN_SUPPORTED) ; $(JAVA_VERSION_MAX_SUPPORTED)]. Please install Java within those bounds")
	endif
endif

##@ General

# The help target prints out all targets with their descriptions organized
# beneath their categories. The categories are represented by '##@' and the
# target descriptions by '##'. The awk commands is responsible for reading the
# entire set of makefiles included in this invocation, looking for lines of the
# file as xyz: ## something, and then pretty-format the target and help. Then,
# if there's a line with ##@ something, that gets pretty-printed as a category.
# More info on the usage of ANSI control characters for terminal formatting:
# https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_parameters
# More info on the awk command:
# http://linuxcommand.org/lc3_adv_awk.php
help: ## Display this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Build
.PHONY: all docker-build workflow-service notification-service fast-build-notification-service 
.PHONY: model-api fast-build-model-api fast-build 

# maven arguments for fast build
FAST_BUILD_ARGS = -Dmaven.test.skip=true -Dmaven.javadoc.skip=true

clean: ## Clean all modules
	$(MAVEN) clean

all: clean ## Build all modules
	$(MAVEN) $(ARGS) install

fast-build: ARGS = $(FAST_BUILD_ARGS) ## Build all modules without running the tests and generate javadoc
fast-build: all

workflow-service: ## Build workload service
	$(MAVEN) $(ARGS) install -pl workflow-service

fast-build-workflow-service: ARGS = $(FAST_BUILD_ARGS) ## Fast build workflow service
fast-build-workflow-service: workflow-service

notification-service: ## Build notification-service
	$(MAVEN) $(ARGS) install -pl notification-service

fast-build-notification-service: ARGS = $(FAST_BUILD_ARGS)
fast-build-notification-service: notification-service

model-api:
	$(MAVEN) $(ARGS) install -pl parodos-model-api

fast-build-model-api: ARGS = $(FAST_BUILD_ARGS)
fast-build-model-api: model-api

workflow-engine: ## Build workload engine
	$(MAVEN) $(ARGS) install -pl workflow-engine

fast-build-workflow-engine: ARGS = $(FAST_BUILD_ARGS) ## Fast build workflow engine
fast-build-workflow-engine: workflow-engine

pattern-detection-library: ## Build pattern detection library
	$(MAVEN) $(ARGS) install -pl pattern-detection-library

fast-build-pattern-detection-library: ARGS = $(FAST_BUILD_ARGS) ## Fast build pattern detection library
fast-build-pattern-detection-library: pattern-detection-library

workflow-examples: ## Build worklow examples
	$(MAVEN) $(ARGS) -pl workflow-examples

fast-build-workflow-examples: ARGS = $(FAST_BUILD_ARGS) ## Fast build workflow examples
fast-build-workflow-examples: workflow-examples

coverage: ## Build coverage
	$(MAVEN) $(ARGS) -pl coverage

fast-build-coverage: ARGS = $(FAST_BUILD_ARGS) ## Fast build coverage
fast-build-coverage: coverage

# Build docker images
.PHONY: build-images tag-images push-images push-images-to-kind install-nginx install-kubernetes-dependencies wait-kubernetes-dependencies
	
build-images: ## Build docker images
	$(DOCKER-COMPOSE) -f ./docker-compose/docker-compose.yml build

tag-images: ## Tag docker images with git hash and branch name
	$(DOCKER) tag docker-compose_workflow-service:latest $(ORG)$(WORKFLOW_SERVICE_IMAGE):$(GIT_HASH)
	$(DOCKER) tag docker-compose_notification-service:latest $(ORG)$(NOTIFICATION_SERVICE_IMAGE):$(GIT_HASH)

	$(DOCKER) tag docker-compose_workflow-service:latest $(ORG)$(WORKFLOW_SERVICE_IMAGE):$(GIT_BRANCH)
	$(DOCKER) tag docker-compose_notification-service:latest $(ORG)$(NOTIFICATION_SERVICE_IMAGE):$(GIT_BRANCH)

push-images: ## Push docker images to quay.io registry
	$(DOCKER) push  $(ORG)/$(WORKFLOW_SERVICE_IMAGE):$(GIT_HASH)
	$(DOCKER) push  $(ORG)/$(NOTIFICATION_SERVICE_IMAGE):$(GIT_HASH)

	$(DOCKER) push  $(ORG)/$(WORKFLOW_SERVICE_IMAGE):$(GIT_BRANCH)
	$(DOCKER) push  $(ORG)/$(NOTIFICATION_SERVICE_IMAGE):$(GIT_BRANCH)

push-images-to-kind: ## Push docker images to kind
	$(DOCKER) tag docker-compose_workflow-service:latest $(ORG)$(WORKFLOW_SERVICE_IMAGE):test
	$(DOCKER) tag docker-compose_notification-service:latest $(ORG)$(NOTIFICATION_SERVICE_IMAGE):test
	kind load docker-image $(ORG)$(WORKFLOW_SERVICE_IMAGE):test
	kind load docker-image $(ORG)$(NOTIFICATION_SERVICE_IMAGE):test

install-nginx: ## Install nginx
	kubectl label node kind-control-plane  "ingress-ready=true" || exit 0
	kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

install-kubernetes-dependencies: install-nginx

wait-kubernetes-dependencies: ## Wait for dependencies to be ready
	kubectl wait --namespace ingress-nginx \
	  --for=condition=ready pod \
	  --selector=app.kubernetes.io/component=controller \
	  --timeout=90s

##@ Run
.PHONY: docker-run docker-stop

docker-run: ## Run notification and workflow services in containers
	$(DOCKER-COMPOSE) -f $(PWD)/docker-compose/docker-compose.yml up 

docker-stop: ## Stop notification and workflow services
	$(DOCKER-COMPOSE) -f $(PWD)/docker-compose/docker-compose.yml down

JAVA_ARGS = -Dspring.profiles.active=local
run-workflow-service: ## Run local workflow service
	java -jar $(JAVA_ARGS) workflow-service/target/workflow-service-$(VERSION).jar

run-notification-service: ## Run local notification service
	java -jar $(JAVA_ARGS) notification-service/target/notification-service-$(VERSION).jar
