#!/bin/bash
# Author: lshannon
# Sample Script to run the WorkFlow Examples

#process_response() {
#  echo "$1" | awk -F\" '{print $2}'
#}

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
    -H 'Content-Type: application/json' | jq '.[] | select(.name=="'$1'")' | jq -r '.id'
}

get_checker_workflow() {
  curl -X 'GET' -s \
    "${TARGET_URL}/api/v1/workflowdefinitions/$1/" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' | jq '.tasks[] | select(has("workFlowChecker"))' | jq -r '.workFlowChecker' | head -n 1
}

get_next_workflow() {
  curl -X 'GET' -s \
    "${TARGET_URL}/api/v1/workflowdefinitions/$1/" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' | jq '.tasks[] | select(has("nextWorkFlow"))' | jq -r '.nextWorkFlow' | head -n 1
}

run_simple_flow() {
  echo "******** Running The Simple Sequence Flow ********"
  echo "                                                  "
  echo "                                                  "
  workflow_id=$(get_workflow_id "simpleSequentialWorkFlowDefinition")
  curl -X 'POST' -v \
    "${TARGET_URL}/api/v1/workflows" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{
        "name": "simpleSequentialWorkFlow_INFRASTRUCTURE_WORKFLOW",
        "tasks": []
      }'
  echo "                                                "
  echo "******** Simple Sequence Flow Completed ********"
  echo "                                                "
}

run_complex_flow() {
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
                 "description": "an example project"
               }' | jq -r '.id')
  [ ${#PROJECT_ID} -eq "36" ] || @fail "Project ID ${PROJECT_ID} is not present"
  echo "Project id is " $(echo_green $PROJECT_ID)

  echo "                                                "
  echo_blue "******** Running The Complex WorkFlow ********"
  echo "Running the Assessment to see what WorkFlows are eligable for this situation:"
  INFRASTRUCTURE_OPTION=$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/workflows" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' \
    -d '{
          "projectId": "'$PROJECT_ID'",
          "workFlowName": "onboardingAssessment_ASSESSMENT_WORKFLOW",
          "workFlowTasks": []
        }' | jq -r '.workFlowOptions.newOptions[0].workFlowName')
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
  EXECUTION_ID="$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/workflows/" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' \
    -d '{
          "projectId": "'$PROJECT_ID'",
      "workFlowName": "'$ONBOARDING_WORKFLOW_NAME'",
      "workFlowTasks": [
    {
                    "taskName": "certWorkFlowTask",
                    "arguments": [
                      {
                        "key": "username",
                        "value": "Peter"
                      }
                    ]
                  },
                  {
                    "taskName": "adGroupWorkFlowTask",
                    "arguments": [
                      {
                        "key": "api-server",
                        "value": "api.com"
                      }
                    ]
                  }
      ]
    }' | jq -r '.workFlowExecutionId')"
  echo "                                               "
  echo "                                               "
  echo "Onboarding workflow execution id:" $(echo_green $EXECUTION_ID)
  [ ${#EXECUTION_ID} -eq "36" ] || @fail "There is no valid EXECUTION_ID: '${EXECUTION_ID}'"
  ONBOARDING_WORKFLOW_CHECKER_ID=$(get_checker_workflow "$ONBOARDING_WORKFLOW_ID")
  [ ${#ONBOARDING_WORKFLOW_CHECKER_ID} -eq "36" ] || @fail "There is no valid ONBOARDING_WORKFLOW_CHECKER_ID: '${ONBOARDING_WORKFLOW_CHECKER_ID}'"
  ONBOARDING_WORKFLOW_CHECKER_NAME=$(get_workflow_name "$ONBOARDING_WORKFLOW_CHECKER_ID")
  [ ${#ONBOARDING_WORKFLOW_CHECKER_NAME} -gt "10" ] || @fail "There is no valid ONBOARDING_WORKFLOW_CHECKER_NAME: '${ONBOARDING_WORKFLOW_CHECKER_NAME}'"
  echo " "
  echo_blue "******** Executing the WorkFlowChecker ***********"
  echo "onboardingWorkFlowCheck:" $(echo_green $ONBOARDING_WORKFLOW_CHECKER_NAME)
  EXECUTION_ID="$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/workflows/" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' \
    -d '{
          "projectId": "'$PROJECT_ID'",
      "workFlowName": "'"$ONBOARDING_WORKFLOW_CHECKER_NAME"'",
      "workFlowTasks": []
    }' | jq -r '.workFlowExecutionId')"

  echo "Onboarding workflow Checker execution id:" $(echo_green $EXECUTION_ID)
  NAMESPACE_WORKFLOW_ID=$(get_next_workflow "$ONBOARDING_WORKFLOW_ID")
  NAMESPACE_WORKFLOW_NAME=$(get_workflow_name "$NAMESPACE_WORKFLOW_ID")

  echo " "
  echo_blue "******** Running the Namespace WorkFlow ***********"
  echo "executes 1 task with a WorkFlowChecker"
  EXECUTION_ID="$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/workflows/" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' \
    -d '{
          "projectId": "'$PROJECT_ID'",
        "workFlowName": "'$NAMESPACE_WORKFLOW_NAME'",
        "workFlowTasks": []
      }' | jq -r '.workFlowExecutionId')"

  echo "Executing the WorkFlowChecker (namespaceWorkFlowCheck)."
  echo "Namespace workflow execution id:" $(echo_green $EXECUTION_ID)
  NAMESPACE_WORKFLOW_CHECKER_ID=$(get_checker_workflow "$NAMESPACE_WORKFLOW_ID")
  [ ${#NAMESPACE_WORKFLOW_CHECKER_ID} -eq "36" ] || @fail "There is no valid NAMESPACE_WORKFLOW_CHECKER_ID: '${NAMESPACE_WORKFLOW_CHECKER_ID}'"
  NAMESPACE_WORKFLOW_CHECKER_NAME=$(get_workflow_name "$NAMESPACE_WORKFLOW_CHECKER_ID")
  [ ${#NAMESPACE_WORKFLOW_CHECKER_NAME} -gt "10" ] || @fail "There is no valid NAMESPACE_WORKFLOW_CHECKER_NAME: '${NAMESPACE_WORKFLOW_CHECKER_NAME}'"

  echo "Executing the WorkFlowChecker (namespaceWorkFlowCheck):" $(echo_green $NAMESPACE_WORKFLOW_CHECKER_NAME)
  EXECUTION_ID="$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/workflows/" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' \
    -d '{
          "projectId": "'$PROJECT_ID'",
        "workFlowName": "'$NAMESPACE_WORKFLOW_CHECKER_NAME'",
        "workFlowTasks": []
      }' | jq -r '.workFlowExecutionId')"

  [ ${#EXECUTION_ID} -eq "36" ] || @fail "There is no valid EXECUTION_ID: '${EXECUTION_ID}'"
  echo "Namespace workflow Checker execution id:" $(echo_green $EXECUTION_ID)


  NETWORK_WORKFLOW_ID=$(get_next_workflow "$NAMESPACE_WORKFLOW_ID")
  [ ${#NETWORK_WORKFLOW_ID} -eq "36" ] || @fail "There is no valid NETWORK_WORKFLOW_ID: '${NETWORK_WORKFLOW_ID}'"
  NETWORK_WORKFLOW_NAME=$(get_workflow_name "$NETWORK_WORKFLOW_ID")
  [ ${#NETWORK_WORKFLOW_NAME} -gt "10" ] || @fail "There is no valid NETWORK_WORKFLOW_NAME: '${NETWORK_WORKFLOW_NAME}'"
  echo " "
  echo_blue "Executing the final Workflow (netWorkingWorkflow)"
  EXECUTION_ID="$(curl -X 'POST' -s \
    "${TARGET_URL}/api/v1/workflows/" \
    -H 'accept: */*' \
    -H 'Authorization: Basic dGVzdDp0ZXN0' \
    -H 'Content-Type: application/json' \
    -d '{
          "projectId": "'$PROJECT_ID'",
        "workFlowName": "'$NETWORK_WORKFLOW_NAME'",
        "workFlowTasks": []
      }' | jq -r '.workFlowExecutionId')"
  echo "network workflow execution id:" $(echo_green $EXECUTION_ID)
}

run_complex_flow
