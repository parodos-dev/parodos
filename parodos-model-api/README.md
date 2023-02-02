# Parodos Model API

This project contains all the dependencies to externally create and configure Workflows which are composed of WorkflowTasks. 

The goal of a Workflow is to achieve an outcome for a developer. For example, the OCP onboarding Workflow might perform all the steps to set up permissions, networking, required monitoring and security tooling and OCP configuration for a developer to start using OCP to develop applications.

The goal of WorkflowTasks are too perform the necessary steps in the Workflow to achieve the desired outcome.

Developers/Quality Assurance/Application Support and any other member of a software delivery team consume Workflows via the Parodos Backstage plugins (https://github.com/janus-idp/backstage-plugins), or through another interface developed by the enterprise.

The IDP internal development team build Workflows and their composing WorkflowTasks in partnership with infrastructure operations/networking/audit/security/DevOps (any team in the enterprise owning/operating/governing tools and environment) using this API.

The following is a UML diagram of the current Parodos Object Model.

![UML](readme-images/uml.png)

# Adding The Client Library To A Project

```xml

	<dependency>
		<groupId>com.redhat.parodos.workflow</groupId>
		<artifactId>workflow-engine</artifactId>
		<version>${parodos.version}</version>
	</dependency>

```

The source for this package is in the Parodos repo. Use the parent pom.xml at the root of the project to build and install it into your local maven repo.

```shell

mvn clean install

```

# Concepts and Example Usage

The foundation of Parodos is the Workflow object. In Parodos there are a few specified Workflows that are common in the IDP space. They can be found in the WorkFlowType enum. At present these are:

- Assessment (takes inputs and gives a list of Workflow options for a user to choose from)
- Checker (determines the status of a manual process that is blocking other workflows from running)
- Infrastructure (creates tooling and environments)

Each of these Workflows are composed of WorkflowTasks that are perform actions relevant to the WorkFlowType. These are covered in detail below.

Executions of the WorkflowTasks can be done in:

- Serial
- Parallel
- Predicate
- Repeating

At present the Parodos Workflow engine wraps the Easy Flows project (https://github.com/j-easy/easy-flows). Future releases of Parodos may provide options to leverage other Workflow engines. For this first version of Parodos, easy-flows provides all the necessary functionality.

With this structure a developer can use the base type of Workflow and WorkflowTask to create any Workflow they wish.

However, Parodos has provided some structure around WorkflowTask to help developers create Workflows that are specific to the IDP space and can be consumed by the Parodos UI (Backstage plugins found in https://github.com/janus-idp/backstage-plugins)

## Parodos WorkflowTask

Parodos provides to structure around the types of tasks with some abstract implementations.

** BaseAssessmentTask ** - these WorkflowTasks accepts a collection of inputs, and returns WorkflowOptions based on custom logic that can be defined in the task. These are foundation of Workflows that help developers (and other team members) determine what Workflows they can run for their use case. For example, an assessment might review code looking for a pipeline definition or the presence of a certain 3rd party service - both of which would imply different workflows.

** BaseWorkFlowTask ** - these WorkflowTasks are intended to call downstream systems that iniate automation. The class can optionally contain a WorkflowChecker workflow. This is a Workflow that contains one or more WorkFlowCheckerTask

** WorkFlowCheckerTask ** - WorkflowTasks that check the status of manual processes triggered by other Workflows. WorkFlowCheckerTasks can be long running when place into a WorkFlow when the definition of the Workflow its contained inside is of type WorkFlowCheckerDefinition (the workflow-service provides scheduling for these tasks) and its outcomes 


### Workflow Definitions and Options

There are objects to represent the descriptions of Workflow and WorkflowTask. These are used to provide descriptions to the user from the UI layer, but also in persistance

** WorkFlowDefinition ** - a description of a WorkFlow including its WorkFlowType

** WorkFlowCheckerDefinition ** - a description of a WorkFlow that performs checks on the outcome of previous WorkFlows. The workflow-service will execute these on a schedule

** WorkFlowTaskDefinition ** - a description of WorkFlowTask. Specific tasks can extend this, pre-populating fields based on their specificity 

** WorkFlowOption ** - a description of a Workflow that can be presented to a user as a potential Workflow to execute

** WorkFlowOptions ** - a collection of WorkFlow options grouped in a categories specific to IDP common workflows. These are upgrade, migrate, new (this is onboarding for the first time) and other (these are team related workflows like adding a new developer to a project)

### Delegates

The model includes some classes that contain useful logic related to creating and running Workflows and WorkflowTasks

** WorkContextDelegate ** - a utility class containing help functions related to working with the WorkContext

This project also specifies definition classes, which are required for versioning and for UI related functionality.


## Using The API

The following provides some simple examples of how to use the API

### Creating WorkFlowTask

TODO

#### Creaing an AssessmentTask

TODO

### Creating WorkFlowCheckerTasks

TODO

### Creating A WorkFlow

TODO

### Creating A AssessmentWorkflow

TODO

### Creating A WorkFlowCheckerWorkFlow

TODO

## Registering the Customizations With The Infrastructure Service For BeanWorkflowRegistryImpl Consumption

Provided all the code was created in accordance with the above examples, simply generate a Jar file and place this in the classpath of the Infrastructure Service. Upon start up it will register the configured Task(s) and WorkFlow(s).

Future versions of Parodos will include other options for creating and registering WorkFlowTasks and WorkFlows

## Demo Implementation

For a full example show how to configure all these Task(s) and WorkFlows(s), please refer to the 'workflow-examples'