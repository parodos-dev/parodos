FROM registry.access.redhat.com/ubi9/openjdk-11-runtime

WORKDIR /app

RUN addgroup --system spring && adduser --system spring --ingroup spring

USER spring:spring

COPY target/*.jar ./notification-service.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","-Djava.security.egd=file:/dev/./urandom", "notification-service.jar"]