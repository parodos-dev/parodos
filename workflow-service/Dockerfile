FROM registry.access.redhat.com/ubi9/openjdk-11-runtime

WORKDIR /app

COPY target/*.jar ./workflow-service.jar

EXPOSE 8080

CMD ["java", "-jar", "workflow-service.jar"]
