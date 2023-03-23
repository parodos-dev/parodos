export SERVER_PORT=8080

java -jar -Dspring.profiles.active=local \
    -Dloader.path=../workflow-examples/target/workflow-examples-1.0.5-SNAPSHOT-jar-with-dependencies.jar \
    ../workflow-service/target/workflow-service-1.0.5-SNAPSHOT.jar
