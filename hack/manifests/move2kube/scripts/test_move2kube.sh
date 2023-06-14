#!/bin/bash

echo_green() {
  COLOR="\e[32m";
  ENDCOLOR="\e[0m";
  printf "$COLOR%b$ENDCOLOR\n" "$1";
}

echo_green "Starting"

SERVERIP=${SERVERIP:-workflow-service.default.svc.cluster.local}
SERVERPORT=${SERVERPORT:-8080}
export TARGET_URL="http://${SERVERIP}:${SERVERPORT}"

echo "TARGET_URL: ${TARGET_URL}"
PROJECT_NAME="project-$RANDOM"
echo $PROJECT_NAME
PROJECT_ID=$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/projects" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' \
    -d '{ "name": "'${PROJECT_NAME}'", "description": "an example project"}' | jq -r .id
)
echo "Project id is " $(echo_green $PROJECT_ID)

PARAMS=$(cat <<EOF
{
  "projectId": "${PROJECT_ID}",
  "workFlowName": "move2KubeWorkFlow_INFRASTRUCTURE_WORKFLOW",
  "arguments": [],
  "works": [
    {
      "workName": "preparationWorkflow",
      "type": "WORKFLOW",
      "arguments": [],
      "works": [
        {
          "workName": "getSources",
          "type": "WORKFLOW",
          "arguments": [],
          "works": [
            {
              "workName": "gitCloneTask",
              "type": "TASK",
              "arguments": [
                {
                  "key": "uri",
                  "value": "git@github.com:eloycoto/spring-petclinic.git"
                },
                {
                  "key": "branch",
                  "value": "main"
                },
                {
                  "key": "credentials",
                  "value": "/opt/keys/id_rsa"
                }
              ]
            },
            {
              "workName": "gitBranchTask",
              "type": "TASK",
              "arguments": [
                {
                  "key": "branch",
                  "value": "move2kubeUpdates"
                }
              ]
            },
            {
              "workName": "gitArchiveTask",
              "type": "TASK",
              "arguments": [
                {
                  "key": "path",
                  "value": null
                }
              ]
            }
          ]
        },

    {
      "workName": "move2KubeProject",
      "type": "WORKFLOW",
      "arguments": [],
      "works": [
        {
          "workName": "move2KubeTask",
          "type": "TASK",
          "arguments": []
        }
      ]
    }
      ]
    },

        {
          "workName": "move2KubePlan",
          "type": "TASK",
          "arguments": []
        },

        {
          "workName": "move2KubeTransform",
          "type": "TASK",
          "arguments": []
        },

    {
      "workName": "move2KubeRetrieve",
      "type": "TASK",
      "arguments": []
    },
    {
      "workName": "gitCommitTask",
      "type": "TASK",
      "arguments": [
        {
          "key": "commitMessage",
          "value": "feat: add migration to kubernetes"
        }
      ]
    },
    {
      "workName": "gitPushTask",
      "type": "TASK",
      "arguments": [
        {
          "key": "remote",
          "value": "origin"
        },
        {
          "key": "credentials",
          "value": "/opt/keys/id_rsa"
        }
      ]
    }
  ]
}
EOF
)


WORKFLOW_EXEC_ID=$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/workflows" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' \
    -d "${PARAMS}" | jq -r '.workFlowExecutionId')

sleep 5

curl -u test:test "${TARGET_URL}/api/v1/workflows/${WORKFLOW_EXEC_ID}/status" | jq .

echo "URL for checking this is:"

echo "kubectl -exec -ti client -- curl -u test:test ${TARGET_URL}/api/v1/workflows/${WORKFLOW_EXEC_ID}/status | jq ."

echo "http://localhost:7007/parodos/onboarding/${PROJECT_ID}/${WORKFLOW_EXEC_ID}/workflow-detail"
