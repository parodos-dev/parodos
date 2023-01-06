# WorkFlow Examples

This repository contains some examples of different WorkFlows that can be created with the Parodos infrastructure-service

Its recommended that you read the README for the 'parodos-model-api' and the 'infrastructure-service' prior to working with this sample.

## Using This Sample

The following is a review of how Assessment and Infrastructure WorkFlows can be configured within their own Jar for consumption by the BeanWorkflowRegistryImpl contained in the Infrastructure Service. *Note*: Future versions of the Infrastructure Service will include means of registering Tasks and WorkFlows that are not dependent on the Spring Framework and/or rebuilding the application.

### Compiling The Code

To get the Parodos dependencies you will need to run a maven install from the **root** of the project folder. This will build all the Java dependencies.

```shell

mvn install

```

### Adding The workflow-examples To The InfraStructure Service

Once the Jar has been compiled, ensure its added to the pom.xml of the InfraStructure Service. This will allow the BeanWorkflowRegistryImpl to register the Tasks and WorkFlows. 


```xml

<dependency>
	 <groupId>com.redhat.parodos</groupId>
	 <artifactId>workflow-examples</artifactId>
	 <version>${parodos.version}</version>
</dependency>


```
Compiled the application by running the following in the 'infrastructure-service' folder.

```shell

mvn install

```
Then start it by running the 'start_infrastructure_service.sh' script. This will start the application in 'local' mode. This profile is intended for local use. DO NOT ENABLE THIS PROFILE IN PRODUCTION. Starting in any other profile will enable security and integrate it with Keycloak for authentication.


### Testing The Application With The WorkFlow-Examples

Execute the 'run_examples.sh' script to run the workflows from the commandline. The outputs of the script will help demonstrate the logic of the Workflows as they execute

## Notes

### Package Structures and InfraStructure Service's BeanWorkFlowRegistryImpl

If you are using the out of the box BeanWorkFlowRegistryImpl in the infrastructure service, ensure your packages have the base of 'com.redhat.parodos'. If a different structure is used 'com.mycompany.parodos', any WorkFlows and Tasks defined will not be detected by the BeanWorkFlowRegistryImpl.

### A Word On Dependencies

Please review the dependencies included in the infrastructure service and ensure when creating Tasks and WorkFlows in an external Jar (such as outlined in this project) that dependencies are not included that override existing dependencies that the Infrastructure Service has.

### Tips On Task and WorkFlow Complexity

If you find yourself creating over complex Tasks and WorkFlow, you are not using Parodos for its intended use case. Parodos is intended to tie together existing logic and tools. Tasks and WorkFlows should be posting messages on queues, calling APIs and other light weight integrations.

# Authors

Luke Shannon (Github: lshannon)

