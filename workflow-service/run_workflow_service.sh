#!/bin/bash

# This script starts the workflow service and monitors it.
# If the service dies, it will be restarted.
# This script is useful for development and testing.
# Environment variables:
#   USER_WORKFLOWS_JAR_FILE - path to the user workflows jar file (optional). If not set, the example workflows jar file
#   will be used.
#   SPRING_PROFILES_ACTIVE - Spring profiles to use (optional). If not set, the "local" profile will be used.
# The script should be executed from the workflow-service directory.

# Define variables
CHECK_INTERVAL=5
SERVICE_JAR_FILE=$(ls -1 target/workflow-service-*.jar)
WORKFLOWS_EXAMPLE_JAR_FILE=$(ls -1 ../workflow-examples/target/workflow-examples-*-jar-with-dependencies.jar)
WORKFLOWS_JAR_FILE="${USER_WORKFLOWS_JAR_FILE:-$WORKFLOWS_EXAMPLE_JAR_FILE}"
LOADER_PATH=$WORKFLOWS_JAR_FILE
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"

# Define function to start Java process
start_workflow_service_process() {
  LOADER_PATH=$LOADER_PATH SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE java -jar $SERVICE_JAR_FILE &
  SERVICE_JAVA_PID=$!
}

# Define function to stop Java process gracefully
stop_workflow_service_process() {
  curl -X POST http://localhost:8080/actuator/shutdown
  wait $SERVICE_JAVA_PID
}

# Stop Java process gracefully when script is killed
trap "stop_workflow_service_process; exit" SIGINT SIGTERM

# Start Java process
start_workflow_service_process

# Monitor Java process and restart if died
while true; do
  # Wait for a few seconds before checking the process status
  sleep $CHECK_INTERVAL

  # Check if the Java process is still running
  if ! ps -p $SERVICE_JAVA_PID > /dev/null; then
    echo "Workflow-service process is not running. Restarting..."
    stop_workflow_service_process
    start_workflow_service_process
  fi
done
