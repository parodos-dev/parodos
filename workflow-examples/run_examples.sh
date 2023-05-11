#!/bin/bash
# Author: lshannon
# Sample Script to run the WorkFlow Examples

#process_response() {
#  echo "$1" | awk -F\" '{print $2}'
#}
#

set -e

SERVER_IP=${SERVER_IP:-127.0.0.1}
SERVER_PORT=${SERVER_PORT:-8080}
export TARGET_URL="http://${SERVER_IP}:${SERVER_PORT}"

echo "Starting example with '${TARGET_URL}' server"

COOKIEFP="$(mktemp)"
TOKEN=""

refresh_token() {
    TOKEN=$(grep "XSRF-TOKEN" $COOKIEFP | awk '{print $7}')
}


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

#set -x

get_workflow_name() {
  curl -X 'GET' -s \
    "${TARGET_URL}/api/v1/workflowdefinitions" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' | jq '.[] | select(.id=="'$1'")' | jq -r '.name'
}

get_workflow_id() {
  curl -X 'GET' -s \
    "${TARGET_URL}/api/v1/workflowdefinitions" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H "X-XSRF-TOKEN: ${TOKEN}" \
    -b $COOKIEFP \
    -H 'Content-Type: application/json' | jq '.[] | select(.name=="'$1'")' | jq -r '.id'
}

get_workflow_options() {
  curl -X 'GET' -s \
    "${TARGET_URL}/api/v1/workflows/$1/context?param=WORKFLOW_OPTIONS" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H "X-XSRF-TOKEN: ${TOKEN}" \
    -b $COOKIEFP \
    -H 'Content-Type: application/json' | jq -r
}

get_workflow_status() {
  curl -X 'GET' -s \
    "${TARGET_URL}/api/v1/workflows/$1/status" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H "X-XSRF-TOKEN: ${TOKEN}" \
    -b $COOKIEFP \
    -H 'Content-Type: application/json' | jq -r '.status'
}

wait_for_workflow_status() {
  for i in {1..10}
  do
    STATUS=$(get_workflow_status $1)
    echo "Workflow status: $STATUS"
    [ "$STATUS" == "$2" ] && break
    sleep 2s
    refresh_token
    [ $i -eq "10" ] && @fail "Workflow didn't finish yet (status: $STATUS)"
  done
}

get_checker_workflow() {
  curl -X 'GET' -s \
    "${TARGET_URL}/api/v1/workflowdefinitions/$1/" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H "X-XSRF-TOKEN: ${TOKEN}" \
    -b $COOKIEFP \
    -H 'Content-Type: application/json' | jq '.tasks[] | select(has("workFlowChecker"))' | jq -r '.workFlowChecker' | head -n 1
}

get_next_workflow() {
  curl -X 'GET' -s \
    "${TARGET_URL}/api/v1/workflowdefinitions/$1/" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H "X-XSRF-TOKEN: ${TOKEN}" \
    -b $COOKIEFP \
    -H 'Content-Type: application/json' | jq '.tasks[] | select(has("nextWorkFlow"))' | jq -r '.nextWorkFlow' | head -n 1
}

wait_project_start() {
  echo_blue "******** Checking project is running ********"
  for i in {1..100}
  do
    CODE=$(curl -LI -c $COOKIEFP -s "${TARGET_URL}/api/v1/projects" \
      -H 'accept: */*' \
      -o /dev/null \
      -H 'Authorization: Basic dGVzdDp0ZXN0' \
      -H 'Content-Type: application/json' \
      -w '%{http_code}\n' || exit 0)
    refresh_token
    [ $CODE -eq "200" ] && break
    sleep 2s
    [ $i -eq "100" ] && @fail "Project didn't started yet"
  done
}

function execute_workflow() {
  params="$1"
  expectedWorkStatus="$2"
  response=$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/workflows" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H "X-XSRF-TOKEN: ${TOKEN}" \
    -b $COOKIEFP \
    -H 'Content-Type: application/json' \
    -d "$params")

    workStatus=$(echo "$response" | jq -r '.workStatus')
    echo "Info: HTTP request finished with workStatus=${workStatus}" >&2
    if [[ "$workStatus" != "$expectedWorkStatus" ]]; then
      @fail "Error: HTTP request response with unexpected workStatus: expected=${expectedWorkStatus}, actual=${workStatus}" >&2
    fi

    # infrastructureOption=$(echo "$response" | jq -r '.workFlowOptions.newOptions[0].workFlowName')
    echo "$response"
}

run_simple_flow() {
  echo "Running simple flow"
  wait_project_start
  echo "Project is ✔️ on ${TARGET_URL}"
  echo " "

  echo_blue "******** Create Project ********"
  echo "                                                "
  # generate random project name
  PROJECT_NAME="project-$RANDOM"
  PROJECT_ID=$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/projects" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' \
    -H "X-XSRF-TOKEN: ${TOKEN}" \
    -b $COOKIEFP \
    -d '{
                 "name": "'"${PROJECT_NAME}"'",
                 "description": "an example project"
               }' | jq -r '.id')
  [ ${#PROJECT_ID} -eq "36" ] || @fail "Project ID ${PROJECT_ID} is not present"
  echo "Project id is " $(echo_green $PROJECT_ID)

  echo_blue "******** Running The Simple Sequence Flow ********"
  echo "                                                  "
  echo "                                                  "
  workflow_id=$(get_workflow_id "simpleSequentialWorkFlowDefinition")

  params='{
      "projectId": "'$PROJECT_ID'",
      "workFlowName": "simpleSequentialWorkFlow_INFRASTRUCTURE_WORKFLOW",
      "works": [
          {
              "workName": "restCallTask",
              "arguments": [
                  {
                    "key": "url",
                    "value": "http://localhost:8080/actuator/health"
                  }
              ]
          },
          {
              "workName": "loggingTask",
              "arguments": [
                  {
                      "key": "user-id",
                      "value": "test-user-id"
                  },
                  {
                      "key": "api-server",
                      "value": "test-api-server"
                  }
              ]
          }
      ]
    }'
  response=$(execute_workflow "$params" "IN_PROGRESS")
  echo "workflow started successfully with response: $response"
  echo "                                                "
  workflow_id=$(echo "$response" | jq -r '.workFlowExecutionId')
  wait_for_workflow_status $workflow_id "COMPLETED"
  echo_blue "******** Simple Sequence Flow Completed ********"
  echo "                                                "
}

run_complex_flow() {
  echo "                                                "
  echo "Running Complex Workflow"
  wait_project_start
  echo "Project is ✔️ on ${TARGET_URL}"
  echo " "
  echo_blue "******** Create Project ********"
  echo "                                                "
  PROJECT_NAME="project-$RANDOM"
  PROJECT_ID=$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/projects" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H "X-XSRF-TOKEN: ${TOKEN}" \
    -b $COOKIEFP \
    -H 'Content-Type: application/json' \
    -d '{
                 "name": "'"${PROJECT_NAME}"'",
                 "description": "an example project"
               }' | jq -r '.id')
  [ ${#PROJECT_ID} -eq "36" ] || @fail "Project ID ${PROJECT_ID} is not present"
  echo "Project id is " $(echo_green $PROJECT_ID)

  echo "                                                "
  echo_blue "******** Running The Complex WorkFlow ********"
  echo "Running the Assessment to see what WorkFlows are eligible for this situation:"

  params='{
      "projectId": "'$PROJECT_ID'",
      "workFlowName": "onboardingComplexAssessment_ASSESSMENT_WORKFLOW",
      "works": []
  }'
  response=$(execute_workflow "$params" "IN_PROGRESS")
  workflow_id=$(echo "$response" | jq -r '.workFlowExecutionId')
  wait_for_workflow_status $workflow_id "COMPLETED"
  echo "workflow finished successfully with response: $response"
  echo "                                               "
  response=$(get_workflow_options "$workflow_id")

  echo "Workflow options response: $response"
  INFRASTRUCTURE_OPTION=$(echo "$response" | jq -r '.workFlowOptions.newOptions[0].workFlowName')
  echo "The Following Option Is Available:" $(echo_green ${INFRASTRUCTURE_OPTION})
  [ ${#INFRASTRUCTURE_OPTION} -gt "10" ] || @fail "There is no valid INFRASTRUCTURE_OPTION"

  echo "                                               "
  echo_blue "Running the onboarding WorkFlow"
  echo "(executes 3 tasks in Parallel with a WorkFlowChecker)"

  ONBOARDING_WORKFLOW_ID=$(get_workflow_id "$INFRASTRUCTURE_OPTION")
  [ ${#ONBOARDING_WORKFLOW_ID} -eq "36" ] || @fail "There is no valid ONBOARDING_WORKFLOW_ID: '${ONBOARDING_WORKFLOW_ID}'"
  ONBOARDING_WORKFLOW_NAME=$INFRASTRUCTURE_OPTION
  [ ${#ONBOARDING_WORKFLOW_NAME} -gt "10" ] || @fail "There is no valid ONBOARDING_WORKFLOW_NAME: '${ONBOARDING_WORKFLOW_NAME}'"

  echo "- ONBOARDING_WORKFLOW_ID:   " $(echo_green $ONBOARDING_WORKFLOW_ID)
  echo "- ONBOARDING_WORKFLOW_NAME: " $(echo_green $ONBOARDING_WORKFLOW_NAME)
  params='{
      "projectId": "'$PROJECT_ID'",
      "workFlowName": "'$ONBOARDING_WORKFLOW_NAME'",
      "works": [
          {
              "workName": "certWorkFlowTask",
              "arguments": [
                  {
                      "key": "user-id",
                      "value": "test-user-id"
                  },
                  {
                      "key": "api-server",
                      "value": "api.com"
                  }
              ]
          },
          {
              "workName": "adGroupWorkFlowTask",
              "arguments": [
                  {
                      "key": "user-id",
                      "value": "test-user-id"
                  },
                  {
                      "key": "api-server",
                      "value": "api.com"
                  }
             ]
          },
          {
              "workName": "dynatraceWorkFlowTask",
              "arguments": [
                  {
                      "key": "user-id",
                      "value": "test-user-id"
                  },
                  {
                      "key": "api-server",
                      "value": "api.com"
                  }
              ]
          }
      ]
  }'
  response=$(execute_workflow "$params" "IN_PROGRESS")
  EXECUTION_ID="$(echo "$response" | jq -r '.workFlowExecutionId')"
  echo "                                               "
  echo "                                               "
  echo "Onboarding workflow execution id:" $(echo_green $EXECUTION_ID)
  [ ${#EXECUTION_ID} -eq "36" ] || @fail "There is no valid EXECUTION_ID: '${EXECUTION_ID}'"
}

run_escalation_flow() {
  echo "******** Running The Escalation WorkFlow ********"
  echo "                                                  "
  echo "                                                  "

  wait_project_start
  PROJECT_NAME="project-$RANDOM"
  PROJECT_ID=$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/projects" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H "X-XSRF-TOKEN: ${TOKEN}" \
    -b $COOKIEFP \
    -H 'Content-Type: application/json' \
    -d '{
                 "name": "'"${PROJECT_NAME}"'",
                 "description": "an example project"
               }' | jq -r '.id')
  echo "Project id is " $(echo_green $PROJECT_ID)


  echo_blue "******** Running the Starting WorkFlow ***********"
  echo "executes 1 task with a WorkFlowChecker"
  params='{
      "projectId": "'$PROJECT_ID'",
      "workFlowName": "workflowStartingCheckingAndEscalation",
      "works": []
  }'
  response=$(execute_workflow "$params" "IN_PROGRESS")
  EXECUTION_ID="$(echo "$response" | jq -r '.workFlowExecutionId')"

  echo "                                                "
  echo "******** Simple Escalation Flow Completed (check logs as the checkers are still running) ********"
  echo "                                                "
}


if [ $# -eq 0 ]; then
  run_escalation_flow
  exit 0
fi

case $1 in
  "escalation")
    run_escalation_flow
    ;;

  "complex")
    run_complex_flow
    ;;

  "simple")
    run_simple_flow
    ;;
  *)
    echo_red "##### Unsupported argument #####"
    echo "Options: escalation (default) , complex, simple"
    ;;
esac
