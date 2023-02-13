# WorkFlow Examples

This repository contains some examples of different WorkFlows that can be created with the Parodos infrastructure-service

Its recommended that you read the README for the 'parodos-model-api' prior to working with this sample. The README of the 'workflow-service' will explain how to execute the workflow. 

## What Is In This Sample

In this version of Parodos, Workflow projects are Java project. All configuration is done using the Spring Framework. These samples are built with Maven, however Gradle could also be used.

The examples in this project can be found in two different packages.

### Simple

This package shows different WorkflowTask(s) being created. The project shows how to create both Sequential and Parrellel workflows.

### Complex

This package shows how to work with WorkflowTasks that create a manual process that needs to be monitored before further WorkFlowTasks can be created. In this project WorkflowCheckers and other related concepts are created and configured.

## Compiling The Code

To get the Parodos dependencies you will need to run a maven install from the **root** of the project folder. This will build all the Java dependencies.

```shell

mvn install

```

### Adding The workflow-examples To The InfraStructure Service

Once the Jar has been compiled, ensure its added to the pom.xml of the workflow-service. This will allow the BeanWorkflowRegistryImpl to register the WorkflowTasks and Workflows. 


```xml

<dependency>
	 <groupId>com.redhat.parodos</groupId>
	 <artifactId>workflow-examples</artifactId>
	 <version>${parodos.version}</version>
</dependency>


```

Refer to the README of the workflow-service for how to build and start the application with this dependency in place.

This project contains a script to run the examples in this project 'run_examples.sh' and to start the workflow-service in LOCAL mode for testing 'start_workflow_service.sh'.

## Overview of Sample Code and Configuration

### Defining Workflows and WorkflowTasks

WorkflowTasks are the units that do the work in a Workflow. A Workflow executes Workflow tasks in an order specified when the Workflow object is created. For more details on these objects and their specific types, please review to the parodos-model-api for a detailed description of the object model.

In this project how to configure these objects will be covered.

### Simple Workflow

In this package the following WorkflowTasks are defined:

- LoggingWorkFlowTask
- RestAPIWorkFlowTask
- SecureAPIGetTestTask

The package contains the configuration file SimpleWorkFlowConfiguration where all the WorkflowTasks are configured and brought together in Workflows.

#### A Note On Defining WorkflowTasks

***Creating a Single Instance Of The Same Workflow Tasks***

WorkflowTasks can be defined a Spring Components (@Component). In this case the workflow-service will detect the @Component tag and create a bean for this type. NOTE: When taking this option it key that all dependencies required to create the WorkflowTasks are available as other Spring Components and/or configuration values. Any unspecified dependencies in a WorkflowTasks with @Component will result in the workflow-service being unable to start. By taking this approach the WorkflowTask can be used by multiple Workflows without any extra code outside of that used in the when defining the WorkflowTasks where @Component was declared.

***Defining a WorkflowTasks that can be change with configuration***

The values passed into a WorkflowTasks may vary depending on the Workflow using it. In this case the WorkflowTask can be defined as a regular Java class (do not use the @Component declaration). In a class with the declaration @Configuration, methods an be created returning an instance of the WorkflowTasks with different configurations applied to the reference. These methods should have the @Bean declaration. Also, multiple instances of the same type will be created (and Spring wires by type), the @Bean declaration should include a name (@Bean("myBean") or @Bean(name = "myBean")) . How this name is reference will be covered in the next section.





## Notes

### Package Structures and Workflow-Service's BeanWorkFlowRegistryImpl

Ensure your packages have the base of 'com.redhat.parodos'. If a different structure is used, 'com.mycompany.parodos' for example, any WorkFlows and Tasks defined will not be detected by the BeanWorkFlowRegistryImpl of the workflow-service.

### A Word On Dependencies

Please review the dependencies included in the workflow-service and ensure when creating Tasks and WorkFlows in an external Jar (such as outlined in this project) that dependencies are not included that override existing dependencies that the workflow-service has.

### Tips On Task and WorkFlow Complexity

If you find yourself creating over complex Tasks and WorkFlow, you are not using Parodos for its intended use case. Parodos is intended to tie together existing logic and tools. Tasks and WorkFlows should be posting messages on queues, calling APIs and other light weight integrations.

