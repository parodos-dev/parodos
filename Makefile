# Docker command to use. Use of podman is highly adviced.
DOCKER ?= docker
DOCKER-COMPOSE ?= docker-compose

CLAIR_TMP_DIR := .

TESTDBPASS := parodos
TESTDBNAME := parodos
TESTDBUSER := parodos
TESTDBPORT := 5432

# maven
MAVEN ?= /usr/bin/mvn

# set quiet mode by default

ORG=quay.io/parodos-dev/
WORKFLOW_SERVICE_IMAGE=workflow-service
NOTIFICATION_SERVICE_IMAGE=notification-service
EXAMPLES_SERVICE_IMAGE=examples-service

# get version from pom
VERSION = $(shell sed -n "s/<revision>\(.*\)<\/revision>/\1/p" $(PWD)/pom.xml | sed 's/\ *//g')
RELEASE_VERSION = $(shell echo $(VERSION) | sed -e s/-SNAPSHOT//)
NEXT_VERSION = $(shell echo $(RELEASE_VERSION) | awk -F. -v OFS=. '{$$NF += 1 ; print}')-SNAPSHOT

GIT_BRANCH := $(shell git rev-parse --abbrev-ref HEAD | sed s,^main$$,latest,g)
GIT_HASH := $(shell git rev-parse HEAD)

MVN_JAVA_VERSION := $(shell $(MAVEN) --version | awk 'NR==3 { split($$3, ver, "."); print ver[1] }')
JAVA_VERSION := $(shell java --version | awk 'NR==1 { split($$2, ver, "."); print ver[1] }')
JAVA_VERSION_MAX_SUPPORTED=17
JAVA_VERSION_MIN_SUPPORTED=17

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

all: mvn-checks java-checks install ## Build all modules

install: ARGS = $(FAST_BUILD_ARGS)
install: clean
	$(MAVEN) $(ARGS) install

deploy: clean ## push snapshot modules to maven central
	$(MAVEN) deploy

release: ## release and push modules to maven central
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
	$(MAVEN) $(ARGS) verify -pl coverage

fast-build-coverage: ARGS = $(FAST_BUILD_ARGS) ## Fast build coverage
fast-build-coverage: coverage

# Build docker images
.PHONY: build-images tag-images push-images push-images-to-kind install-nginx install-kubernetes-dependencies wait-kubernetes-dependencies

build-images: ## Build docker images
	$(DOCKER-COMPOSE) -f ./docker-compose/docker-compose.yml build

tag-images: ## Tag docker images with git hash and branch name
	$(eval TAG?=$(GIT_HASH))
	$(DOCKER) tag docker-compose_workflow-service:latest $(ORG)$(WORKFLOW_SERVICE_IMAGE):$(TAG)
	$(DOCKER) tag docker-compose_notification-service:latest $(ORG)$(NOTIFICATION_SERVICE_IMAGE):$(TAG)
	$(DOCKER) tag docker-compose_examples-service:latest $(ORG)$(EXAMPLES_SERVICE_IMAGE):$(TAG)

	$(DOCKER) tag docker-compose_workflow-service:latest $(ORG)$(WORKFLOW_SERVICE_IMAGE):$(GIT_BRANCH)
	$(DOCKER) tag docker-compose_notification-service:latest $(ORG)$(NOTIFICATION_SERVICE_IMAGE):$(GIT_BRANCH)
	$(DOCKER) tag docker-compose_examples-service:latest $(ORG)$(EXAMPLES_SERVICE_IMAGE):$(GIT_BRANCH)

deploy-local-registry:
ifneq ($(shell $(DOCKER) container inspect -f '{{.State.Running}}' registry 2> /dev/null), true)
	$(DOCKER) run -d -p 5000:5000 --restart=always --name registry registry:2
endif
	$(DOCKER) tag docker-compose_workflow-service:latest localhost:5000/docker-compose_workflow-service:latest
	$(DOCKER) tag docker-compose_notification-service:latest localhost:5000/docker-compose_notification-service:latest
	$(DOCKER) push  localhost:5000/docker-compose_workflow-service:latest
	$(DOCKER) push  localhost:5000/docker-compose_notification-service:latest

stop-local-registry:
	$(DOCKER) stop registry
	$(DOCKER) rm registry

deploy-clair: deploy-local-registry
	cd $(CLAIR_TMP_DIR) ;	git clone https://github.com/quay/clair ;	cd clair; $(DOCKER) compose up -d
	sleep 15m
	@echo "Clair is up and running"

stop-clair:
	cd $(CLAIR_TMP_DIR)/clair ; $(DOCKER) compose down

.PHONY: clairctl
CLAIRCTL = $(shell pwd)/bin/clairctl
clairctl: ## Download clairctl locally if necessary.
ifeq (,$(wildcard $(CLAIRCTL)))
ifeq (,$(shell which clairctl 2>/dev/null))
	@{ \
	go install github.com/quay/clair/v4/cmd/clairctl@latest ;\
	}
endif
CLAIRCTL = $(shell which clairctl)
endif

run-images-analysis: clairctl
	$(eval BRIDGE=$(shell $(DOCKER) network inspect -f '{{json .IPAM.Config}}' bridge | jq -r .[].Gateway))
	$(CLAIRCTL) --config $(CLAIR_TMP_DIR)/clair/local-dev/clair/config.yaml report -o json $(BRIDGE):5000/docker-compose_notification-service:latest   | jq .vulnerabilities > $(CLAIR_TMP_DIR)/docker-compose_notification-service_report.json
	$(CLAIRCTL) --config $(CLAIR_TMP_DIR)/clair/local-dev/clair/config.yaml report -o json $(BRIDGE):5000/docker-compose_workflow-service:latest   | jq .vulnerabilities > $(CLAIR_TMP_DIR)/docker-compose_workflow-service_report.json

.SILENT: analyse-images
analyse-images: deploy-clair run-images-analysis analyse-images-fast
	#

.SILENT: analyse-images-fast
analyse-images-fast:
	$(eval FIXABLE_CRITICAL_NOTIFICATION?=$(shell cat $(CLAIR_TMP_DIR)/docker-compose_notification-service_report.json | jq '[.[] | select(.normalized_severity=="Critical") | select(.fixed_in_version!="")] | length'))
	$(eval FIXABLE_HIGH_NOTIFICATION?=$(shell cat $(CLAIR_TMP_DIR)/docker-compose_notification-service_report.json | jq '[.[] | select(.normalized_severity=="High") | select(.fixed_in_version!="")] | length'))
	$(eval FIXABLE_CRITICAL_WORKFLOW?=$(shell cat $(CLAIR_TMP_DIR)/docker-compose_workflow-service_report.json | jq '[.[] | select(.normalized_severity=="Critical") | select(.fixed_in_version!="")] | length'))
	$(eval FIXABLE_HIGH_WORKFLOW?=$(shell cat $(CLAIR_TMP_DIR)/docker-compose_workflow-service_report.json | jq '[.[] | select(.normalized_severity=="High") | select(.fixed_in_version!="")] | length'))
	$(eval CRITICAL_NOTIFICATION?=$(shell cat $(CLAIR_TMP_DIR)/docker-compose_notification-service_report.json |jq '[.[] | select(.normalized_severity=="Critical")] | length'))
	$(eval HIGH_NOTIFICATION?=$(shell cat $(CLAIR_TMP_DIR)/docker-compose_notification-service_report.json | jq '[.[] | select(.normalized_severity=="High")] | length'))
	$(eval CRITICAL_WORKFLOW?=$(shell cat $(CLAIR_TMP_DIR)/docker-compose_workflow-service_report.json | jq '[.[] | select(.normalized_severity=="Critical")] | length'))
	$(eval HIGH_WORKFLOW?=$(shell cat $(CLAIR_TMP_DIR)/docker-compose_workflow-service_report.json | jq '[.[] | select(.normalized_severity=="High")] | length'))

	$(eval FIXABLE_CRITICAL_NOTIFICATION=$(shell [ $(FIXABLE_CRITICAL_NOTIFICATION) > 0 ] && echo "$(FIXABLE_CRITICAL_NOTIFICATION)" || echo "0"))
	$(eval FIXABLE_HIGH_NOTIFICATION=$(shell [ $(FIXABLE_HIGH_NOTIFICATION) > 0 ] && echo "$(FIXABLE_HIGH_NOTIFICATION)" || echo "0"))
	$(eval FIXABLE_CRITICAL_WORKFLOW=$(shell [ $(FIXABLE_CRITICAL_WORKFLOW) > 0 ] && echo "$(FIXABLE_CRITICAL_WORKFLOW)" || echo "0"))
	$(eval FIXABLE_HIGH_WORKFLOW=$(shell [ $(FIXABLE_HIGH_WORKFLOW) > 0 ] && echo "$(FIXABLE_HIGH_WORKFLOW)" || echo "0"))

	echo -e "Notification service: \n\t$(CRITICAL_NOTIFICATION) critical issues found; $(FIXABLE_CRITICAL_NOTIFICATION) fixable\n\t$(HIGH_NOTIFICATION) high issues found; $(FIXABLE_HIGH_NOTIFICATION) fixable\n\
Workflow service:\n\t$(CRITICAL_WORKFLOW) critical issues found; $(FIXABLE_CRITICAL_WORKFLOW) fixable\n\t$(HIGH_WORKFLOW) high issues found; $(FIXABLE_HIGH_WORKFLOW) fixable\n"
	if [ "${FIXABLE_CRITICAL_NOTIFICATION}" -gt 0 ] ; then \
		echo "$(FIXABLE_CRITICAL_NOTIFICATION) fixable critical issues found for notification service" ; \
		exit 1 ; \
	elif [ "${FIXABLE_CRITICAL_WORKFLOW}" -gt 0 ] ; then \
		echo "$(FIXABLE_CRITICAL_WORKFLOW) fixable critical issues found for workflow service" ; \
		exit 1 ; \
	elif [ "${FIXABLE_HIGH_NOTIFICATION}" -gt 0 ] ; then \
		echo "$(FIXABLE_HIGH_NOTIFICATION) fixable high issues found for notification service" ; \
		exit 1 ; \
	elif [ "${FIXABLE_HIGH_WORKFLOW}" -gt 0 ] ; then \
		echo "$(FIXABLE_HIGH_WORKFLOW) fixable high issues found for workflow service" ; \
		exit 1 ; \
	fi
	@echo "Remember to clean $(CLAIR_TMP_DIR)!"

push-images: ## Push docker images to quay.io registry
	$(eval TAG?=$(GIT_HASH))
	$(DOCKER) push  $(ORG)$(WORKFLOW_SERVICE_IMAGE):$(TAG)
	$(DOCKER) push  $(ORG)$(NOTIFICATION_SERVICE_IMAGE):$(TAG)
	$(DOCKER) push  $(ORG)$(EXAMPLES_SERVICE_IMAGE):$(TAG)

	$(DOCKER) push  $(ORG)$(WORKFLOW_SERVICE_IMAGE):$(GIT_BRANCH)
	$(DOCKER) push  $(ORG)$(NOTIFICATION_SERVICE_IMAGE):$(GIT_BRANCH)
	$(DOCKER) push  $(ORG)$(EXAMPLES_SERVICE_IMAGE):$(GIT_BRANCH)

push-images-to-kind: ## Push docker images to kind
	$(DOCKER) tag docker-compose_workflow-service:latest $(ORG)$(WORKFLOW_SERVICE_IMAGE):test
	$(DOCKER) tag docker-compose_examples-service:latest $(ORG)$(EXAMPLES_SERVICE_IMAGE):test
	$(DOCKER) tag docker-compose_notification-service:latest $(ORG)$(NOTIFICATION_SERVICE_IMAGE):test
	kind load docker-image $(ORG)$(WORKFLOW_SERVICE_IMAGE):test
	kind load docker-image $(ORG)$(EXAMPLES_SERVICE_IMAGE):test
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

update-version: ## update release version
	find . -type f | xargs sed -i 's/$(VERSION)/$(RELEASE_VERSION)/g'

bump-version: ## update post-release version and update commit message
	find . -type f | xargs sed -i 's/$(RELEASE_VERSION)/$(NEXT_VERSION)/g'

bump-git-commit: ## adds all files and bumps the version
	git add -u .
	git commit -m 'Version bump to $(NEXT_VERSION)'

git-release: ## adds all release files and commits
	git add -u .
	git commit -m 'Release $(RELEASE_VERSION)'

git-tag: TAG = v$(RELEASE_VERSION)
git-tag: ## tag commit and prepare for image release
	git tag -a $(TAG) -m "$(TAG)"
	$(eval TAG=$(TAG))

release-all: update-version release git-release git-tag build-images tag-images push-images bump-version install bump-git-commit

release-manifests:
	./hack/scripts/release.sh $(RELEASE_VERSION)
	./hack/scripts/stable_images.sh v$(RELEASE_VERSION)

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

format-files:
	mvn spring-javaformat:apply
	mvn impsort:sort

.PHONY: install-kubernetes-dependencies-and-wait deploy-to-kind

install-kubernetes-dependencies-and-wait: install-kubernetes-dependencies
	sleep 10

deploy-to-kind: install-kubernetes-dependencies-and-wait wait-kubernetes-dependencies build-images tag-images push-images-to-kind
	kubectl kustomize hack/manifests/testing | kubectl apply -f -
	kubectl wait --timeout=300s --for=condition=Ready pods --all -n default
	kubectl get pods -A
