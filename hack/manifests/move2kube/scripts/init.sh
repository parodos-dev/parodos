#!/bin/bash

set -e

apk update
apk add jq

PARAMS=$(cat <<EOF
    {"id":"","name":"spring","description":"","timestamp":""}
EOF
)

WID=$(curl http://move2kube.move2kube.svc.cluster.local:8080/api/v1/workspaces \
    -d  "${PARAMS}" \
    -H 'Content-Type: application/json'  | jq -r .id)

echo "WORKSPACE ID --> ${WID}";

curl "http://move2kube.move2kube.svc.cluster.local:8080/api/v1/workspaces/${WID}/inputs" \
    -X POST \
    -F 'file=@/opt/config/m2kconfig.yaml;type=application/x-yaml' \
    -F 'type=configs'
