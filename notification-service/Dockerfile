FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

WORKDIR /app

COPY target/notification-service-*.*.*[^a-z].jar ./notification-service.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","-Djava.security.egd=file:/dev/./urandom", "notification-service.jar"]
