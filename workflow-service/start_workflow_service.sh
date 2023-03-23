SERVICE_JAR_FILE=$(ls -1 ../workflow-service/target/workflow-service-*.jar)
WORKFLOWS_EXAMPLE_JAR_FILE=$(ls -1 ../workflow-examples/target/workflow-examples-*-jar-with-dependencies.jar)
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
LOADER_PATH=$WORKFLOWS_EXAMPLE_JAR_FILE
SERVER_PORT=8080 LOADER_PATH=$LOADER_PATH SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE java -jar $SERVICE_JAR_FILE
