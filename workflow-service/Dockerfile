FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

WORKDIR /app

COPY workflow-service/target/workflow-service-*.*.*[^a-z].jar ./workflow-service.jar

EXPOSE 8080

ENTRYPOINT java -jar workflow-service.jar
