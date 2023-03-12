#!/bin/bash
# Author: ydayagi
# Sample Script to run the TIBCO WorkFlow Example


SERVERIP=${SERVERIP:-127.0.0.1}
SERVERPORT=${SERVERPORT:-8080}
export TARGET_URL="http://${SERVERIP}:${SERVERPORT}"

echo "Starting example with '${TARGET_URL}' server"

echo_red() {
  COLOR="\e[31m";
  ENDCOLOR="\e[0m";
  printf "$COLOR%b$ENDCOLOR\n" "$1";
}

echo_green() {
  COLOR="\e[32m";
  ENDCOLOR="\e[0m";
  printf "$COLOR%b$ENDCOLOR\n" "$1";
}

echo_yellow() {
  COLOR="\e[33m";
  ENDCOLOR="\e[0m";
  printf "$COLOR%b$ENDCOLOR\n" "$1";
}

echo_blue() {
  COLOR="\e[34m";
  ENDCOLOR="\e[0m";
  printf "$COLOR%b$ENDCOLOR\n" "$1";
}

@fail() {
    echo_red "ERROR: $1"
    exit 1
}

get_workflow_id() {
  curl -X 'GET' -s \
    "${TARGET_URL}/api/v1/workflowdefinitions" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' | jq '.[] | select(.name=="'$1'")' | jq -r '.id'
}

run_tibco_flow() {
  echo "                                                "
  echo_blue "******** Checking project is running ********"
  for i in {1..100}
  do
    CODE=$(curl -LI -s "${TARGET_URL}/api/v1/projects" \
      -H 'accept: */*' \
      -o /dev/null \
      -H 'Authorization: Basic dGVzdDp0ZXN0' \
      -H 'Content-Type: application/json' \
      -w '%{http_code}\n')
    [ $CODE -eq "200" ] && break
    sleep 2s
    [ $i -eq "100" ] && @fail "Project didn't started yet"
  done
  echo "Project is ✔️ on ${TARGET_URL}"
  echo " "

  echo_blue "******** Create Project ********"
  echo "                                                "
  PROJECT_ID=$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/projects" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' \
    -d '{
                 "name": "project-1",
                 "description": "TIBCO example project"
               }' | jq -r '.id')
  [ ${#PROJECT_ID} -eq "36" ] || @fail "Project ID ${PROJECT_ID} is not present"
  echo "Project id is " $(echo_green $PROJECT_ID)

  echo "                                                "
  echo_blue "******** Running The TIBCO WorkFlow ********"

  echo "                                              "
  echo_blue "Running the TIBCO WorkFlow"
  echo ""

  TIBCO_WORKFLOW_NAME="tibcoWorkFlow"
  TIBCO_WORKFLOW_ID=$(get_workflow_id $TIBCO_WORKFLOW_NAME)
  [ ${#TIBCO_WORKFLOW_ID} -eq "36" ] || @fail "There is no valid TIBCO_WORKFLOW_ID: '${TIBCO_WORKFLOW_ID}'"
  [ ${#TIBCO_WORKFLOW_NAME} -gt "10" ] || @fail "There is no valid TIBCO_WORKFLOW_NAME: '${TIBCO_WORKFLOW_NAME}'"

  echo "- TIBCO_WORKFLOW_ID:   " $(echo_green TIBCO_WORKFLOW_ID)
  echo "- TIBCO_WORKFLOW_NAME: " $(echo_green TIBCO_WORKFLOW_NAME)
  EXECUTION_ID="$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/workflows/" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' \
    -d '{
          "projectId": "'$PROJECT_ID'",
      "workFlowName": "'$TIBCO_WORKFLOW_NAME'",
      "workFlowTasks": [
        {
          "name": "tibcoTask",
          "arguments": [
            {
              "key": "topic",
              "value": "tibcoTest"
            },
            {
              "key": "message",
              "value": "testing tibco task"
            }
          ]
        }
      ]
    }' | jq -r '.workFlowExecutionId')"
  echo "                                               "
  echo "                                               "
  echo "TIBCO workflow execution id:" $(echo_green $EXECUTION_ID)
  [ ${#EXECUTION_ID} -eq "36" ] || @fail "There is no valid EXECUTION_ID: '${EXECUTION_ID}'"
}
run_tibco_flow
