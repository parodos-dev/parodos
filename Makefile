
COMPOSE ?= docker-compose
DOCKER ?= docker

ORG=quay.io/parados/
WORKFLOW_SERVICE_IMAGE=workflow-service
NOTIFICATION_SERVICE_IMAGE=notification-service

GIT_BRANCH := $(shell git rev-parse --abbrev-ref HEAD | sed s,^main$$,latest,g)
GIT_HASH := $(shell git rev-parse HEAD)

build-images:
	$(COMPOSE) -f ./docker-compose/docker-compose.yml build


tag-images:
	$(DOCKER) tag docker-compose_workflow-service:latest $(ORG)$(WORKFLOW_SERVICE_IMAGE):$(GIT_HASH)
	$(DOCKER) tag docker-compose_notification-service:latest $(ORG)$(NOTIFICATION_SERVICE_IMAGE):$(GIT_HASH)

	$(DOCKER) tag docker-compose_workflow-service:latest $(ORG)$(WORKFLOW_SERVICE_IMAGE):$(GIT_BRANCH)
	$(DOCKER) tag docker-compose_notification-service:latest $(ORG)$(NOTIFICATION_SERVICE_IMAGE):$(GIT_BRANCH)

push-images:
	$(DOCKER) push  $(ORG)/$(WORKFLOW_SERVICE_IMAGE):$(GIT_HASH)
	$(DOCKER) push  $(ORG)/$(NOTIFICATION_SERVICE_IMAGE):$(GIT_HASH)

	$(DOCKER) push  $(ORG)/$(WORKFLOW_SERVICE_IMAGE):$(GIT_BRANCH)
	$(DOCKER) push  $(ORG)/$(NOTIFICATION_SERVICE_IMAGE):$(GIT_BRANCH)

push-images-to-kind:
	$(DOCKER) tag docker-compose_workflow-service:latest $(ORG)$(WORKFLOW_SERVICE_IMAGE):test
	$(DOCKER) tag docker-compose_notification-service:latest $(ORG)$(NOTIFICATION_SERVICE_IMAGE):test
	kind load docker-image $(ORG)$(WORKFLOW_SERVICE_IMAGE):test
	kind load docker-image $(ORG)$(NOTIFICATION_SERVICE_IMAGE):test

install-nginx:
	kubectl label node kind-control-plane  "ingress-ready=true" || exit 0
	kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

install-kubernetes-dependencies: install-nginx

wait-kubernetes-dependencies:
	kubectl wait --namespace ingress-nginx \
	  --for=condition=ready pod \
	  --selector=app.kubernetes.io/component=controller \
	  --timeout=90s
