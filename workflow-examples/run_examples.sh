#!/bin/bash
# Author: lshannon
# Sample Script to run the WorkFlow Examples

#process_response() {
#  echo "$1" | awk -F\" '{print $2}'
#}

get_workflow_name() {
  curl -X 'GET' -s \
    "http://localhost:8080/api/v1/workflowdefinitions" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' | jq '.[] | select(.id=="'$1'")' | jq -r '.name'
}

get_workflow_id() {
  curl -X 'GET' -s \
    "http://localhost:8080/api/v1/workflowdefinitions" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' | jq '.[] | select(.name=="'$1'")' | jq -r '.id'
}

get_checker_workflow() {
  curl -X 'GET' -s \
    "http://localhost:8080/api/v1/workflowdefinitions/$1/" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' | jq '.tasks[] | select(has("workFlowChecker"))' | jq -r '.workFlowChecker' | head -n 1
}

get_next_workflow() {
  curl -X 'GET' -s \
    "http://localhost:8080/api/v1/workflowdefinitions/$1/" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' | jq '.tasks[] | select(has("nextWorkFlow"))' | jq -r '.nextWorkFlow' | head -n 1
}

run_simple_flow() {
  echo "******** Running The Simple Sequence Flow ********"
  echo "                                                  "
  echo "                                                  "
  workflow_id=$(get_workflow_id "simpleSequentialWorkFlowDefinition")
  curl -X 'POST' -v \
    "http://localhost:8080/api/v1/workflows" \
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
  echo "******** Running The Complex WorkFlow ********"
  echo "                                               "
  echo "                                               "
  echo "Running the Assessment to see what WorkFlows are eligable for this situation:"
  INFRASTRUCTURE_OPTION=$(curl -X 'POST' -s \
    'http://localhost:8080/api/v1/workflows' \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{
          "name": "onboardingAssessment_ASSESSMENT_WORKFLOW",
          "tasks": []
        }' | jq -r '.workFlowOptions.newOptions[0].workFlowId')
  echo "The Following Option Is Available: $INFRASTRUCTURE_OPTION"
  echo "                                               "
  echo "                                               "
  echo "Running the onboarding WorkFlow (executes 3 tasks in Parallel with a WorkFlowChecker)"
  ONBOARDING_WORKFLOW_ID=$(get_workflow_id "$INFRASTRUCTURE_OPTION")
  ONBOARDING_WORKFLOW_NAME=$INFRASTRUCTURE_OPTION
  echo "ONBOARDING_WORKFLOW_ID: $ONBOARDING_WORKFLOW_ID"
  echo "ONBOARDING_WORKFLOW_NAME: $ONBOARDING_WORKFLOW_NAME"
  EXECUTION_ID="$(curl -X 'POST' -s \
    'http://localhost:8080/api/v1/workflows/' \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{
      "name": "'$ONBOARDING_WORKFLOW_NAME'",
      "tasks": [
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
    }' | jq -r '.workFlowId')"
  echo "                                               "
  echo "                                               "
  echo "Onboarding workflow execution id: $EXECUTION_ID"
  ONBOARDING_WORKFLOW_CHECKER_ID=$(get_checker_workflow "$ONBOARDING_WORKFLOW_ID")
  ONBOARDING_WORKFLOW_CHECKER_NAME=$(get_workflow_name "$ONBOARDING_WORKFLOW_CHECKER_ID")
  echo "Executing the WorkFlowChecker (onboardingWorkFlowCheck): $ONBOARDING_WORKFLOW_CHECKER_NAME"
  echo "                                               "
  echo "                                               "
  EXECUTION_ID="$(curl -X 'POST' -s \
    "http://localhost:8080/api/v1/workflows/" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{
      "name": "'"$ONBOARDING_WORKFLOW_CHECKER_NAME"'",
      "tasks": []
    }' | jq -r '.workFlowId')"
  echo "Onboarding workflow Checker execution id: $EXECUTION_ID"
  echo "                                               "
  echo "                                               "
  NAMESPACE_WORKFLOW_ID=$(get_next_workflow "$ONBOARDING_WORKFLOW_ID")
  NAMESPACE_WORKFLOW_NAME=$(get_workflow_name "$NAMESPACE_WORKFLOW_ID")
  echo "Running the Namespace WorkFlow (executes 1 task with a WorkFlowChecker)"
  EXECUTION_ID="$(curl -X 'POST' -s \
    "http://localhost:8080/api/v1/workflows/" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{
        "name": "'$NAMESPACE_WORKFLOW_NAME'",
        "tasks": []
      }' | jq -r '.workFlowId')"
  echo "Namespace workflow execution id: $EXECUTION_ID"
  echo "Executing the WorkFlowChecker (namespaceWorkFlowCheck)."
  echo "                                               "
  echo "                                               "
  echo "Namespace workflow execution id: $EXECUTION_ID"
  NAMESPACE_WORKFLOW_CHECKER_ID=$(get_checker_workflow "$NAMESPACE_WORKFLOW_ID")
  NAMESPACE_WORKFLOW_CHECKER_NAME=$(get_workflow_name "$NAMESPACE_WORKFLOW_CHECKER_ID")
  echo "Executing the WorkFlowChecker (namespaceWorkFlowCheck): $NAMESPACE_WORKFLOW_CHECKER_NAME"
  echo "                                               "
  echo "                                               "
  EXECUTION_ID="$(curl -X 'POST' -s \
    "http://localhost:8080/api/v1/workflows/" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{
        "name": "'$NAMESPACE_WORKFLOW_CHECKER_NAME'",
        "tasks": []
      }' | jq -r '.workFlowId')"
  echo "Namespace workflow Checker execution id: $EXECUTION_ID"
  echo "                                               "
  echo "                                               "
  NETWORK_WORKFLOW_ID=$(get_next_workflow "$NAMESPACE_WORKFLOW_ID")
  NETWORK_WORKFLOW_NAME=$(get_workflow_name "$NETWORK_WORKFLOW_ID")
  echo "Executing the final Workflow (netWorkingWorkflow)"
  EXECUTION_ID="$(curl -X 'POST' -s \
    "http://localhost:8080/api/v1/workflows/" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{
        "name": "'$NETWORK_WORKFLOW_NAME'",
        "tasks": []
      }' | jq -r '.workFlowId')"
  echo "network workflow execution id: $EXECUTION_ID"
}

run_complex_flow
