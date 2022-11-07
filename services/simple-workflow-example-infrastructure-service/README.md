# Simple Example Of Infrastructure WorkFlow Configuration

This project is a very basic example of some simple Workflows for the Parodos Infrastructure Service. Its basically a 'Hello World'. Looks for more complex examples soon.

## Using This Sample

The following is a review of how Tasks and WorkFlows can be configured within their own Jar for consumption by the BeanWorkflowRegistryImpl contained in the Infrastructure Service.

### Adding The Dependencies To Create The Project

The following dependencies and version are required to configure WorkFlows.

```xml

	 <properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<java.version>11</java.version>
		<parodos.version>0.0.1-SNAPSHOT</parodos.version>
		<spring.framework.version>5.3.10</spring.framework.version>
		<maven-release-plugin.version>2.5.3</maven-release-plugin.version>
        	<maven-compiler-plugin.version>3.8.1</maven-compiler-plugin.version>
        	<maven-surefire-plugin.version>2.22.2</maven-surefire-plugin.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context</artifactId>
			<version>${spring.framework.version}</version>
		</dependency>
		<dependency>
		    <groupId>org.springframework</groupId>
		    <artifactId>spring-web</artifactId>
		    <version>${spring.framework.version}</version>
		</dependency>
		 <dependency>
			<groupId>com.redhat.parodos</groupId>
			<artifactId>workflow-engine</artifactId>
			<version>${parodos.version}</version>
		</dependency>
		<dependency>
			<groupId>com.redhat.parodos</groupId>
			<artifactId>parodos-model-api</artifactId>
			<version>${parodos.version}</version>
		</dependency>
	</dependencies>

```

To get the Parodos dependencies you will need to run a maven install from the root of the project folder. This will build all the Java dependencies.

```shell

mvn install

```

### A Word On Dependencies

Please review the dependencies included in the Infrastructure Service and ensure when creating Tasks and WorkFlows in an external Jar (such as outlined in this project) that dependencies are not included that override existing dependencies that the Infrastructure Service has.

If you find yourself creating over complex Tasks and WorkFlow, you are not using Parodos for its intended use case. Parodos is intended to tie together existing logic and tools. Tasks and WorkFlows should be posting messages on queues, calling APIs and other light weight integrations.

## Adding The simple-workflow-example-infrastructure-service To The InfraStructure Service

Once the Jar has been compiled, ensure its added to the pom.xml of the InfraStructure Service. This will allow the BeanWorkflowRegistryImpl to register the Tasks and WorkFlows. 


```xml

		<dependency>
			 <groupId>com.redhat.parodos</groupId>
			 <artifactId>simple-workflow-example-infrastructure-service</artifactId>
			 <version>${parodos.version}</version>
		</dependency>

```

Future versions of the Infrastructure Service will include means of registering Tasks and WorkFlows that are not dependent on the Spring Framework and/or rebuilding the application.


## Testing The Application With The simple-workflow-example-infrastructure-service WorkFlows

Starting the Infrastructure Service in the 'local' profile to run locally without security enabled. DO NOT ENABLE THIS PROFILE IN PRODUCTION

```shell

java -jar target/infrastructure-service-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=local

```
After starting the Swagger Endpoint can be accessed with the following URL: http://localhost:8080/swagger-ui/index.html

To run the InfrastructureTaskWorkFlow registered, post the following JSON into the POST: api/event endpoint.

```json

{
  "workFlowName": "AwesomeToolsAndEnvironment_INFRASTRUCTURE_EVENT_WORKFLOW",
  "requestDetails": {
    "PAYLOAD_PASSED_IN_FROM_SERVICE": "Json String",
    "URL_PASSED_IN_FROM_SERVICE": "https://httpbin.org/post",
    "INFRASTRUCTURE_DISPLAY_VALUE" : "Awesome WorkFlow For Developers",
    "PROJECT_NAME": "Test"
  }
}


```
![Swagger Endpoint](readme-images/1.png)

This will generate a ExistingInfrastructureEntity (persisted to an in-memory H2 DB) that act as a reference point to track downstream progress of the InfrastructureOption's creation.

![Swagger Endpoint](readme-images/2.png)
