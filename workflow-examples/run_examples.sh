#!/bin/bash
# Author: lshannon
# Sample Script to run the WorkFlow Examples

#process_response() {
#  echo "$1" | awk -F\" '{print $2}'
#}
#

set -e -o pipefail

SERVERIP=${SERVERIP:-127.0.0.1}
SERVERPORT=${SERVERPORT:-8080}
export TARGET_URL="http://${SERVERIP}:${SERVERPORT}"

echo "Starting example with '${TARGET_URL}' server"

COOKIEFP="$(mktemp)"
TOKEN=""

refresh_token() {
    TOKEN=$(grep "XSRF-TOKEN" $COOKIEFP | awk '{print $7}' || true)
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

# parodos_v1_curl [uri] [rest of the curl args]
# all uris are relative to TARGET_URL/api/v1
parodos_v1_curl() {
  local uri=$1
  shift
  curl "${TARGET_URL}/api/v1/${uri}" \
    -s \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H "X-XSRF-TOKEN: ${TOKEN}" \
    -b $COOKIEFP \
    "$@"
}

get_workflow_name() {
  parodos_v1_curl workflowdefinitions | jq '.[] | select(.id=="'$1'")' | jq -r '.name'
}

get_workflow_id() {
  parodos_v1_curl workflowdefinitions | jq '.[] | select(.name=="'$1'")' | jq -r '.id'
}

get_checker_workflow() {
  parodos_v1_curl workflowdefinitions/$1/ \
    | jq '.tasks[] | select(has("workFlowChecker"))' | jq -r '.workFlowChecker' | head -n 1
}

get_next_workflow() {
  parodos_v1_curl workflowdefinitions/$1/ \
    | jq '.tasks[] | select(has("nextWorkFlow"))' | jq -r '.nextWorkFlow' | head -n 1
}

wait_project_start() {
  echo_blue "******** Checking project is running ********"
  for i in {1..100}
  do
    CODE=$(parodos_v1_curl projects -LI -s -o /dev/null -w '%{http_code}\n' || true)
    refresh_token
    [ "$CODE" -eq "200" ] && break
    sleep 2s
    [ $i -eq "100" ] && @fail "Project didn't started yet"
  done
}

function execute_workflow() {
  params="$1"
  exitOnFailure="$2"
  response=$(parodos_v1_curl workflows -d "$params")

    status=$(echo "$response" | jq -r '.workStatus')
    if [[ "$status" == "FAILED" ]]; then
      if "$exitOnFailure"; then
        @fail "Error: HTTP request failed" >&2
      else
        echo "Info: HTTP request failed" >&2
      fi
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

  PROJECT_ID=$(parodos_v1_curl projects -d '{ "name": "project-1", "description": "an example project" }' | jq -r '.id')
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
                    "value": "https://httpbin.org/post"
                  },
                  {
                    "key": "payload",
                    "value": "'Hello!'"
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
  response=$(execute_workflow "$params" true)
  echo "workflow finished successfully with response: $response"

  echo "                                                "
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
  PROJECT_ID=$(parodos_v1_curl projects -d '{ "name": "project-1", "description": "an example project" }' | jq -r '.id')
  [ ${#PROJECT_ID} -eq "36" ] || @fail "Project ID ${PROJECT_ID} is not present"
  echo "Project id is " $(echo_green $PROJECT_ID)

  echo "                                                "
  echo_blue "******** Running The Complex WorkFlow ********"
  echo "Running the Assessment to see what WorkFlows are eligible for this situation:"

  params='{
      "projectId": "'$PROJECT_ID'",
      "workFlowName": "onboardingAssessment_ASSESSMENT_WORKFLOW",
      "works": []
  }'
  response=$(execute_workflow "$params" true)
  echo "workflow finished successfully with response: $response"
  echo "                                               "
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
  response=$(execute_workflow "$params" false)
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

  PROJECT_ID=$(parodos_v1_curl projects -d '{ "name": "project-1", "description": "an example project" }' | jq -r '.id')
  echo "Project id is " $(echo_green $PROJECT_ID)


  echo_blue "******** Running the Starting WorkFlow ***********"
  echo "executes 1 task with a WorkFlowChecker"
  params='{
      "projectId": "'$PROJECT_ID'",
      "workFlowName": "workflowStartingCheckingAndEscalation",
      "works": []
  }'
  response=$(execute_workflow "$params" false)
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
