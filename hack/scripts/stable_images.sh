#!/bin/bash
#
export VERSION=${1:-1.0}

echo "Pushing stable images for ${VERSION}";
SERVICES=("workflow-service" "notification-service")


for service in "${SERVICES[@]}"; do
    source_image="quay.io/parodos-dev/${service}:${VERSION}"
    target_image="quay.io/parodos-dev/${service}:stable"

    docker pull "${source_image}"
    docker tag "${source_image}" "${target_image}"
    docker push "${target_image}"
done
