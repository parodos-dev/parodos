#Multi-staged Docker

FROM maven:3-adoptopenjdk-11 as build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ ./src
RUN mvn clean package

FROM adoptopenjdk/openjdk11:latest

RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

COPY --from=build /app/target/*.jar /app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","-Djava.security.egd=file:/dev/./urandom", "/app.jar"]