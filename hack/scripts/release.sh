#!/bin/bash

export VERSION=${1:-1.0}

echo "Writing manifests for ${VERSION}";

BASE_PATH=$(dirname "$0")
RELEASE_PATH="$BASE_PATH/../manifests/release/"
mkdir -p $RELEASE_PATH || echo "Already exists"

cat > $RELEASE_PATH/kustomization.yaml <<EOF
kind: Kustomization
bases:
- ../backstage/

images:
- name: quay.io/parodos-dev/workflow-service:main
  newTag: "$VERSION"

- name: quay.io/parodos-dev/notification-service:main
  newTag: "$VERSION"

- name: quay.io/parodos-dev/backstage-parodos:latest-openshift
  newTag: "$VERSION-openshift"
EOF


kubectl kustomize $RELEASE_PATH > manifests.yaml
