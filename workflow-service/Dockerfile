FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

WORKDIR /app

COPY workflow-service/target/workflow-service-*.*.*[^a-z].jar ./workflow-service.jar
COPY workflow-examples/target/*jar-with-dependencies.jar ./workflow-examples.jar

EXPOSE 8080

ENV SYS_PROPS="-Dloader.path=workflow-examples.jar"

ENTRYPOINT java ${SYS_PROPS} -jar workflow-service.jar
