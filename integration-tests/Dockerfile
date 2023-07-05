FROM docker-compose_workflow-service:latest
WORKDIR /app
COPY workflow-examples/target/*jar-with-dependencies.jar ./workflows.jar
EXPOSE 8080
ENTRYPOINT java -Dloader.path=workflows.jar -jar workflow-service.jar
