#!/bin/bash
# Author: lshannon
# Sample Script to run the WorkFlow Examples

process_response () {
    echo "$1" | awk -F\" '{print $2}'
}

echo "******** Running The Simple Sequence Flow ********"
echo "                                                  "
echo "                                                  "
curl -X 'POST' -v \
  'http://localhost:8080/api/v1/workflows/infrastructures/' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "workFlowParameters": {
    "URL_PASSED_IN_FROM_SERVICE": "https://httpbin.org/post",
    "PAYLOAD_PASSED_IN_FROM_SERVICE": "Hi"
  },
  "workFlowId": "simpleSequentialWorkFlow__INFRASTRUCTURE_WORKFLOW"
}'
echo "                                                "
echo "******** Simple Sequence Flow Completed ********"
echo "                                                "
echo "                                                "
echo "******** Running The Simple Parallel Flow ********"
echo "                                                  "
echo "                                                "
curl -X 'POST' \
  'http://localhost:8080/api/v1/workflows/infrastructures/' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "workFlowId": "simpleParallelWorkFlow__INFRASTRUCTURE_WORKFLOW"
}'
echo "                                                "
echo "                                                "
echo "******** Simple Parallel Flow Completed ********"
echo "                                                "
echo "******** Running The Complex WorkFlow ********"
echo "                                               "
echo "                                               "
echo "Running the Assessment to see what WorkFlows are eligable for this situation:"
INFRASTRUCTURE_OPTION="$(curl -X 'POST' 'http://localhost:8080/api/v1/workflows/assessments/'  -H 'accept: */*' -H 'Content-Type: application/json' -d '{ "workFlowParameters": { "INPUT": "some value"}, "workFlowId": "onoardingAssessment_ASSESSMENT_WORKFLOW"}')"
echo "The Following Option Is Available: $INFRASTRUCTURE_OPTION"
echo "                                               "
echo "                                               "
TRANSACTION_ONE="$(curl -X 'POST' \
  'http://localhost:8080/api/v1/workflows/infrastructures/' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "workFlowId": "oboardingWorkFlow_INFRASTRUCTURE_WORKFLOW"
}')"
echo "                                               "
echo "                                               "
echo "Running the onboarding WorkFlow (executes 3 tasks in Parallel with a WorkFlowChecker"
transaction_id="$(process_response "$TRANSACTION_ONE")"
echo "Executing the WorkFlowChecker (onboardingWorkFlowCheck). It is hard coded to return 'true'"
curl -X 'GET' \
  "http://localhost:8080/api/v1/workflowchecker/$transaction_id" \
  -H 'accept: */*'
echo "                                               "
echo "                                               "

onboardingWorkFlowCheckRespone="$(curl -X 'GET' \
  "http://localhost:8080/api/v1/workflowchecker/$transaction_id" \
  -H 'accept: */*')"
if echo "$onboardingWorkFlowCheckRespone=" | grep -q '"readyToRun"[: ]*true'
then
    TRANSACTION_TWO="$(curl -X 'POST' \
    'http://localhost:8080/api/v1/workflows/infrastructures/' \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{
    "workFlowId": "nameSpaceWorkFlow_INFRASTRUCTURE_WORKFLOW"
  }')"
  transaction_id="$(process_response "$TRANSACTION_TWO")"
else
  echo "Stopping here because readyToRun not true."
  exit 1
fi

echo "                                               "
echo "                                               "
echo "Executing the WorkFlowChecker (nameSpaceWorkFlowChecker) It is hard coded to return 'true'"
curl -X 'GET' \
  "http://localhost:8080/api/v1/workflowchecker/$transaction_id" \
  -H 'accept: */*'
echo "                                               "
echo "                                               "
echo "Executing the final Workflow (netWorkingWorkflow)"
curl -X 'POST' \
  'http://localhost:8080/api/v1/workflows/infrastructures/' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "workFlowId": "networkingWorkFlow_INFRASTRUCTURE_WORKFLOW"
}'
echo
