SERVICE_JAR_FILE=$(ls -1 ../workflow-service/target/workflow-service-*.jar)
WORKFLOWS_EXAMPLE_JAR_FILE=$(ls -1 ../workflow-examples/target/workflow-examples-*-jar-with-dependencies.jar)
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
LOADER_PATH=$WORKFLOWS_EXAMPLE_JAR_FILE
###
PARODOS_AUTH_KEY="Basic dGVzdDp0ZXN0"
JIRA_APPROVER="70121:ed537c36-9d5c-44a8-a36f-044832bc11bb"
JIRA_TOKEN="MO4qY2LiCUJktvaKFXTK7383"
JIRA_URL="https://parodos.atlassian.net"
JIRA_USER="richwong98@gmail.com"
###
SERVER_PORT=8080 LOADER_PATH=$LOADER_PATH SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE java -jar $SERVICE_JAR_FILE
