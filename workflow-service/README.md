# Parodos Workflow Service

This service is designed to call existing automation and tools to help get software teams get what they need to begin coding. This  __service is not intended to replace automation frameworks or complex business rules engines that already exist__  in enterprise environments looking to run a Parodos based Developer Platform.

Think of this service as a way to tie together existing tools/process/automation into end-to-end WorkFlows with the outcome being developers and teams can start working on software project faster and with less frustration.

## Starting The Service Locally

To compile all the components required for this project to work run the maven parent pom in the root directory of the service folder.

```shell

mvn clean package

```

Note: It is assumed 'mvn install' has already been executed at the root level of the project to generate the WorkEngine dependencies into your local maven repository. If this has not been completed the workflow-service will not compile.

The 'workflow-examples' dependency can be found in the pom.xml of the workflow-service. This is a demo configuration to test the service. It should be removed once actual Tasks and WorkFlows start getting created.

To start the application run the following from the root folder of 'infrastructure-service'.

```shell

java -Dspring.profiles.active=local -jar target/workflow-service-0.0.1-SNAPSHOT.jar

```
The 'local' is intended for local testing and runs the application without security (Keycloak managed Oauth2 flow is the default). **DO NOT USE THIS PROFILE IN PRODUCTION**

Upon start-up the Swagger Endpoints can be accessed with the following URL: http://localhost:8080/

For more information on configuring Keycloak:

https://www.keycloak.org/server/all-config

https://www.keycloak.org/guides#getting-started


![UML](readme-images/swagger.png)

## Defining Workflows And WorkFlowTasks

More detail on this subject will be covered in the 'parodos-model-api' folder of this project. To briefly review, Workflow are composed on WorkflowTasks. Both translate to Spring Beans that can be defined in their own java project and added as a dependency to the 'workflow-service'.

For a full review of the domain model and how it can be used, refer to the 'parodos-model-api' project.

In this present release teams are encouraged to think of Workflows and WorkflowTasks as stand along Java projects. As such they should have test coverage, undergo a full software release cycle and should not be updated in production trivally. Due to the persistance of both workflow definitions and WorkflowTask executions, the workflow-service can be restarted to update the definition of Workflows and WorkflowTasks.

**Note:** Future release of this service will include WorkFlowTask/WorkFlow creation/configuration options that do not require having to write Java code.

## Loading WorkFlows into the Application

The Workflow Service is designed out-of-the-box to detect and load the WorkFlows using an implementation of WorkFlowRegistry.

```java

public interface WorkFlowRegistry<T> {
	
    Set<T> getRegisteredWorkFlowNames();
    
    Map<T,WorkFlow> getAllRegisteredWorkFlows();
    
    WorkFlow getWorkFlowById(T id);
    
    Collection<T> getRegisteredWorkFlowNamesByWorkType(String typeName);
}

```

The BeanWorkflowRegistryImpl will load all Spring Beans of type: com.redhat.parodos.workflows.workflow.WorkFlow into the registry which will in turn make them available for their respective Services (they must be in the package 'com.redhat.parodos' to be detected). WorkflowTasks and Workflows can be created using @Bean and @Configuration annotations of the Spring Framework. 

This can be done as part of the workflow-service's code base, or in a separate Jar that can add to the class path of the workflow-service. 

Please review the Parodos project 'workflow-examples' for more details.

![Infrastructure](readme-images/6.png)

## Service Endpoints

The Infrastructure Service provides the following endpoints:

**work-flow-controller**

- GET /api/v1/workflows/infrastructures - Gets a list of all 'id' the InfrastructureWorkFlows available for execution
- GET /api/v1/workflows/infrastructures/{id}/parameter - Scans the InfrastructureWorkFlow and captures all the WorkFlow Parameters required for the Tasks in the WorkFlow to execute. This is consumed by the Parodos UI to dynamically generate prompts for the user to complete when requesting a WorkFlow execution
- POST /api/v1/workflows/infrastructures - Allows for the execution of an InfrastructureWorkFlow


## FAQ

### Why doesn't the service use a more mature/feature rich Business Rules engine?

It is assumed that Parodos will be running in enterprise environments where there will be many tools and platforms available. As a result Parodos has not interest in trying to compete with such tools. The approach is to send the appropriate data to these existing tools, and the most appropriate time to allow for them to be more effectively used and integrated with other tools.

### These workflows are not advanced enough for me to perform the automation tasks I need. Will more automation features be added to Parodos?

If you are finding Parodos's simple workflows not advanced enough to manage the creation and configuration of your tools you are not using Parodos in its intended purpose. Automation tools such as Ansible or Terraform should be used to manage the creation and update of infrastructure. Tools such as Jira Service desk and should manage permission workflows. Think of Parodos as a way to tie these disparate systems together for a more comprehensive experience for consumers of the tools. If you are lacking such automation and tools, it might not be the right time for you to use Parodos

### Will there be support to configure rules beyond Spring Beans?

Yes. In this first release a configuration pattern widely used across many enterprise environments was chosen. However future release will include a DSL (domain specific language) for configuring Workflows without have to write Java code.

### Is there a way to keep track of what WorkFlows have executed and their state of execution?

The 'workflow-service' provides persistence of all WorkflowTask execution. This allows the service to resume execution of WorkflowTasks after the service has been restarted. The Parodos Janus-IDP plugins also provide a view of these persistant events to provide an execution history for Workflows.




