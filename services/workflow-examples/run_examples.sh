#!/bin/bash
# Author: lshannon
# Sample Script to run the WorkFlow Examples

#process_response() {
#  echo "$1" | awk -F\" '{print $2}'
#}

get_workflow_id() {
  curl -X 'GET' -s \
    "http://localhost:8080/api/v1/workflows/" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' | jq '.[] | select(.name=="'$1'")' | jq -r '.id'
}

get_checker_workflow() {
  curl -X 'GET' -s \
    "http://localhost:8080/api/v1/workflows/$1/tasks" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' | jq '.[] | select(has("workFlowChecker"))' | jq -r '.workFlowChecker' | head -n 1
}

get_next_workflow() {
  curl -X 'GET' -s \
    "http://localhost:8080/api/v1/workflows/$1/tasks" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' | jq '.[] | select(has("nextWorkFlow"))' | jq -r '.nextWorkFlow' | head -n 1
}

run_simple_flow() {
  echo "******** Running The Simple Sequence Flow ********"
  echo "                                                  "
  echo "                                                  "
  workflow_id=get_workflow_id "simpleSequentialWorkFlowDefinition"
  curl -X 'POST' -v \
    "http://localhost:8080/api/v1/workflows/$workflow_id/executions" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '[
          {
            "taskName": "restAPIWorkFlowTaskDefinition",
            "arguments": [
              {
                "key": "username",
                "value": "Peter"
              }
            ]
          },
          {
            "taskName": "loggingWorkFlowTaskDefinition",
            "arguments": [
              {
                "key": "api-server",
                "value": "http://api.com"
              }
            ]
          }
        ]'
  echo "                                                "
  echo "******** Simple Sequence Flow Completed ********"
  echo "                                                "
}

run_complex_flow() {
  echo "                                                "
  echo "******** Running The Complex WorkFlow ********"
  echo "                                               "
  echo "                                               "
  ASSESSMENT_WORKFLOW_ID=$(get_workflow_id "onboardingAssessmentDefinition")
  echo "ASSESSMENT_WORKFLOW_ID: $ASSESSMENT_WORKFLOW_ID"
  echo "Running the Assessment to see what WorkFlows are eligable for this situation:"
  INFRASTRUCTURE_OPTION=$(curl -X 'POST' -s "http://localhost:8080/api/v1/workflows/$ASSESSMENT_WORKFLOW_ID/executions" -H 'accept: */*' -H 'Content-Type: application/json' -d '[]' | jq -r '.output.newOptions[0].workFlowId')
  echo "The Following Option Is Available: $INFRASTRUCTURE_OPTION"
  echo "                                               "
  echo "                                               "
  echo "Running the onboarding WorkFlow (executes 3 tasks in Parallel with a WorkFlowChecker)"
  ONBOARDING_WORKFLOW_ID=$(get_workflow_id "$INFRASTRUCTURE_OPTION")
  echo "ONBOARDING_WORKFLOW_ID: $ONBOARDING_WORKFLOW_ID"
  EXECUTION_ID="$(curl -X 'POST' -s \
    "http://localhost:8080/api/v1/workflows/$ONBOARDING_WORKFLOW_ID/executions" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '[
              {
                "taskName": "certWorkFlowTaskDefinition",
                "arguments": [
                  {
                    "key": "username",
                    "value": "Peter"
                  }
                ]
              },
              {
                "taskName": "adGroupWorkFlowTaskDefinition",
                "arguments": [
                  {
                    "key": "api-server",
                    "value": "api.com"
                  }
                ]
              }
            ]')"
  echo "                                               "
  echo "                                               "
  echo "Onboarding workflow execution id: $EXECUTION_ID"
  ONBOARDING_WORKFLOW_CHECKER_ID=$(get_checker_workflow "$ONBOARDING_WORKFLOW_ID")
  echo "Executing the WorkFlowChecker (onboardingWorkFlowCheck): $ONBOARDING_WORKFLOW_CHECKER_ID"
  echo "                                               "
  echo "                                               "
  EXECUTION_ID="$(curl -X 'POST' -s \
    "http://localhost:8080/api/v1/workflows/$ONBOARDING_WORKFLOW_CHECKER_ID/executions" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '[]')"
  echo "Onboarding workflow Checker execution id: $EXECUTION_ID"
  echo "                                               "
  echo "                                               "
  NAMESPACE_WORKFLOW_ID=$(get_next_workflow "$ONBOARDING_WORKFLOW_ID")
  echo "Running the Namespace WorkFlow (executes 1 task with a WorkFlowChecker)"
  EXECUTION_ID="$(curl -X 'POST' -s \
    "http://localhost:8080/api/v1/workflows/$NAMESPACE_WORKFLOW_ID/executions" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '[]')"
  echo "Namespace workflow execution id: $EXECUTION_ID"
  echo "Executing the WorkFlowChecker (namespaceWorkFlowCheck)."
  echo "                                               "
  echo "                                               "
  echo "Namespace workflow execution id: $EXECUTION_ID"
  NAMESPACE_WORKFLOW_CHECKER_ID=$(get_checker_workflow "$NAMESPACE_WORKFLOW_ID")
  echo "Executing the WorkFlowChecker (namespaceWorkFlowCheck): $NAMESPACE_WORKFLOW_CHECKER_ID"
  echo "                                               "
  echo "                                               "
  EXECUTION_ID="$(curl -X 'POST' -s \
    "http://localhost:8080/api/v1/workflows/$NAMESPACE_WORKFLOW_CHECKER_ID/executions" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '[]')"
  echo "Namespace workflow Checker execution id: $EXECUTION_ID"
  echo "                                               "
  echo "                                               "
  NETWORK_WORKFLOW_ID=$(get_next_workflow "$NAMESPACE_WORKFLOW_ID")
  echo "Executing the final Workflow (netWorkingWorkflow)"
  EXECUTION_ID="$(curl -X 'POST' -s \
    "http://localhost:8080/api/v1/workflows/$NAMESPACE_WORKFLOW_ID/executions" \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '[]')"
  echo "network workflow execution id: $EXECUTION_ID"
}

run_complex_flow
