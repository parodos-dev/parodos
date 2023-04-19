# Docker command to use. Use of podman is highly adviced.
DOCKER ?= docker
DOCKER-COMPOSE ?= docker-compose

TESTDBPASS := parodos
TESTDBNAME := parodos
TESTDBUSER := parodos
TESTDBPORT := 5432

# maven
MAVEN ?= /usr/bin/mvn

# set quiet mode by default

ORG=quay.io/parodos/
WORKFLOW_SERVICE_IMAGE=workflow-service
NOTIFICATION_SERVICE_IMAGE=notification-service

# get version from pom
VERSION = $(shell sed -n "s/<revision>\(.*\)<\/revision>/\1/p" $(PWD)/pom.xml | sed 's/\ *//g')

GIT_BRANCH := $(shell git rev-parse --abbrev-ref HEAD | sed s,^main$$,latest,g)
GIT_HASH := $(shell git rev-parse HEAD)

MVN_JAVA_VERSION := $(shell $(MAVEN) --version | awk 'NR==3 { split($$3, ver, "."); print ver[1] }')
JAVA_VERSION := $(shell java --version | awk 'NR==1 { split($$2, ver, "."); print ver[1] }')
JAVA_VERSION_MAX_SUPPORTED=11
JAVA_VERSION_MIN_SUPPORTED=11

# Setting SHELL to bash allows bash commands to be executed by recipes.
# This is a requirement for 'setup-envtest.sh' in the test target.
# Options are set to exit when a recipe line exits non-zero or a piped command fails.
SHELL = /usr/bin/env bash -o pipefail
.SHELLFLAGS = -ec

mvn-checks:
	# check if maven is installed
	@if [ -z $(MVN_JAVA_VERSION) ]; then echo "No maven found in $(PATH). Please install maven"; exit 1; fi
	@if [ $(MVN_JAVA_VERSION) -lt $(JAVA_VERSION_MIN_SUPPORTED)  ] || [ $(MVN_JAVA_VERSION) -gt $(JAVA_VERSION_MIN_SUPPORTED) ]; then \
		echo "Maven Java $(MVN_JAVA_VERSION) version should be [$(JAVA_VERSION_MIN_SUPPORTED) ; $(JAVA_VERSION_MAX_SUPPORTED)]. Please use Java within these bounds with mvn"; \
		exit 1; \
	fi

java-checks:
	# check if Java is installed
	@if [ -z $(JAVA_VERSION) ]; then echo "No java found in $(PATH). Please install java >= $(JAVA_VERSION_MIN_SUPPORTED)"; exit 1; fi
	@if [ $(JAVA_VERSION) -lt $(JAVA_VERSION_MIN_SUPPORTED) ] || [ $(JAVA_VERSION) -gt $(JAVA_VERSION_MAX_SUPPORTED) ]; then \
		echo "Java version $(JAVA_VERSION) should be [$(JAVA_VERSION_MIN_SUPPORTED) ; $(JAVA_VERSION_MAX_SUPPORTED)]. Please install Java within those bounds"; \
		exit 1; \
	fi

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
.PHONY: all workflow-service notification-service fast-build-notification-service
.PHONY: model-api fast-build-model-api fast-build

# maven arguments for fast build
FAST_BUILD_ARGS = -Dmaven.test.skip=true -Dmaven.javadoc.skip=true

clean: ## Clean all modules
	$(MAVEN) clean

all: mvn-checks java-checks clean ## Build all modules
	$(MAVEN) $(ARGS) install

deploy: clean ## push snapshot modules to maven central
	$(MAVEN) deploy

release: clean ## release and push modules to maven central
	$(MAVEN) deploy -P release

fast-build: ARGS = $(FAST_BUILD_ARGS) ## Build all modules without running the tests and generate javadoc
fast-build: all

workflow-service: mvn-checks ## Build workload service
	$(MAVEN) $(ARGS) install -pl workflow-service

fast-build-workflow-service: ARGS = $(FAST_BUILD_ARGS) ## Fast build workflow service
fast-build-workflow-service: workflow-service

notification-service: mvn-checks ## Build notification-service
	$(MAVEN) $(ARGS) install -pl notification-service

fast-build-notification-service: ARGS = $(FAST_BUILD_ARGS)
fast-build-notification-service: notification-service

model-api: mvn-checks
	$(MAVEN) $(ARGS) install -pl parodos-model-api

fast-build-model-api: ARGS = $(FAST_BUILD_ARGS)
fast-build-model-api: model-api

workflow-engine: mvn-checks ## Build workload engine
	$(MAVEN) $(ARGS) install -pl workflow-engine

fast-build-workflow-engine: ARGS = $(FAST_BUILD_ARGS) ## Fast build workflow engine
fast-build-workflow-engine: workflow-engine

pattern-detection-library: mvn-checks ## Build pattern detection library
	$(MAVEN) $(ARGS) install -pl pattern-detection-library

fast-build-pattern-detection-library: ARGS = $(FAST_BUILD_ARGS) ## Fast build pattern detection library
fast-build-pattern-detection-library: pattern-detection-library

workflow-examples: mvn-checks ## Build worklow examples
	$(MAVEN) $(ARGS) -pl workflow-examples

fast-build-workflow-examples: ARGS = $(FAST_BUILD_ARGS) ## Fast build workflow examples
fast-build-workflow-examples: workflow-examples

coverage: mvn-checks ## Build coverage
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
	$(DOCKER) push  $(ORG)$(WORKFLOW_SERVICE_IMAGE):$(GIT_HASH)
	$(DOCKER) push  $(ORG)$(NOTIFICATION_SERVICE_IMAGE):$(GIT_HASH)

	$(DOCKER) push  $(ORG)$(WORKFLOW_SERVICE_IMAGE):$(GIT_BRANCH)
	$(DOCKER) push  $(ORG)$(NOTIFICATION_SERVICE_IMAGE):$(GIT_BRANCH)

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
run-workflow-service: java-checks ## Run local workflow service
	java -jar $(JAVA_ARGS) \
		-Dloader.path=workflow-examples/target/workflow-examples-$(VERSION)-jar-with-dependencies.jar \
		workflow-service/target/workflow-service-$(VERSION).jar

run-notification-service: java-checks ## Run local notification service
	java -jar $(JAVA_ARGS) notification-service/target/notification-service-$(VERSION).jar

run:
	$(MAVEN) spring-boot:run -pl workflow-service -Dspring-boot.run.profiles=local

run-postgres: # Run a sample postgres instance
	$(DOCKER) run \
		--name parodos-postgres \
		-e POSTGRES_PASSWORD=$(TESTDBPASS) \
		-e POSTGRES_DB=$(TESTDBNAME) \
		-e POSTGRES_USER=$(TESTDBUSER) \
		-p 5432:$(TESTDBPORT) \
		-d postgres:15.2

stop-postgres:
	$(DOCKER) rm -f parodos-postgres

