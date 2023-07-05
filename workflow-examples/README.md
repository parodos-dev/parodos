# WorkFlow Examples

This repository contains some examples of different WorkFlows that can be created with the Parodos
infrastructure-service

Its recommended that you read the README for the 'parodos-model-api' prior to working with this sample. The README of
the 'workflow-service' will explain how to execute the workflow.

## What Is In This Sample

In this version of Parodos, Workflow projects are Java project. All configuration is done using the Spring Framework.
These samples are built with Maven, however Gradle could also be used.

These are the examples in this project:

### Simple

This package shows different WorkflowTask(s) being created. The project shows how to create both Sequential and
Parallel workflows.

### Complex

This package shows how to work with WorkflowTasks that create a manual process that needs to be monitored before further
WorkFlowTasks can be created. In this project WorkflowCheckers and other related concepts are created and configured.

## Compiling The Code

To get the Parodos dependencies you will need to run a maven install from the **root** of the project folder. This will
build all the Java dependencies.

```shell

mvn install

```

### Adding The workflow-examples to the workflow-service

Once the Jar has been compiled, ensure it's added to the pom.xml of the workflow-service. This will allow the
`BeanWorkflowRegistryImpl` to register the WorkflowTasks and Workflows.

```xml

<dependency>
	 <groupId>dev.parodos</groupId>
	 <artifactId>workflow-examples</artifactId>
	 <version>${parodos.version}</version>
</dependency>
<dependency>
    <groupId>dev.parodos</groupId>
    <artifactId>prebuilt-tasks</artifactId>
    <version>${revision}</version>
</dependency>


```

Care must be taken when adding Workflow projects to the service to ensure that Workflows do not contradict other
Workflows or dependencies in the Workflow and to not override existing dependencies in the workflow-service.

Refer to the README of the `workflow-service` for how to build and start the application with this dependency in place.

### Running to Examples

To run these examples:

- start the workflow-service in LOCAL mode for testing 'start_workflow_service.sh'
- run the examples in this project 'run_examples.sh'

## Overview of Sample Code and Configuration

### Defining Workflows and WorkflowTasks

WorkflowTasks are the units that do the work in a Workflow. A Workflow executes Workflow tasks in an order specified
when the Workflow object is created. For more details on these objects and their specific types, please review to the
parodos-model-api for a detailed description of the object model.

In this project how to configure these objects will be covered.

### Simple Workflow

In this package the following WorkflowTasks are defined:

- LoggingWorkFlowTask - write to the log of the 'workflow-service'
- RestAPIWorkFlowTask - makes a REST call to a service and logs the response
- SecureAPIGetTestTask - makes a secure call to a REST service and logs the response. Creates a base64 encoded header
  using values passed in as parameters

The package contains the configuration file SimpleWorkFlowConfiguration where all the WorkflowTasks are configured and
brought together in Workflows.

The main logic of a WorkflowFlowTask is configured in the `execute` method. As can be seen the SecureAPIGetTestTask
WorkflowTask.

```java

public WorkReport execute(WorkContext workContext) {
		try {
			String urlString = getParameterValue(workContext, SECURED_URL);
			String username = getParameterValue(workContext, USERNAME);
			String password = getParameterValue(workContext, PASSWORD);
			log.info("Calling: urlString: {} username: {}", urlString, username);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> result = restTemplate.exchange(urlString, HttpMethod.GET,
					getRequestWithHeaders(username, password), String.class);
			if (result.getStatusCode().is2xxSuccessful()) {
				log.info("Rest call completed: {}", result.getBody());
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
			}
			log.error("Call to the API was not successful. Response: {}", result.getStatusCode());
		}
		catch (Exception e) {
			log.error("There was an issue with the REST call: {}", e.getMessage());

		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

```

The execute section does the 'work' required by the task. It also needs to return a WorkReport indicating the
WorkStatus (ie: COMPLETED, FAILED). In the case of a Sequential Workflow, the workflow will stop executing if the
returned WorkReport has a WorkStatus of FAILED.

Parameters are obtained by overriding the following method defined in the WorkFlowTask interface.

For more information on the object model, please review the parodos-model-api README.

```java

@Override
	public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkFlowTaskParameter.builder().key(SECURED_URL)
						.description("The URL of the Secured API you wish to call").optional(false)
						.type(WorkFlowTaskParameterType.URL).build(),
				WorkFlowTaskParameter.builder().key(USERNAME).description("Please enter your username authentication")
						.optional(false).type(WorkFlowTaskParameterType.TEXT).build(),
				WorkFlowTaskParameter.builder().key(PASSWORD)
						.description("Please enter your password for authentication (it will not be stored)")
						.optional(false).type(WorkFlowTaskParameterType.PASSWORD).build());
	}

```

Workflows are configured in a class with the @Configuration annotation. It is expected there will be at least one @Bean
method returning a Workflow reference.

In this sample two such workflows can be seen:

```java

	@Bean
	RestAPIWorkFlowTask restCall() {
		return new RestAPIWorkFlowTask(URL_DEFINED_AT_CONFIGURATION_OF_TASK);
	}


	@Bean(name = "simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow simpleSequentialWorkFlowTask(@Qualifier("restCall") RestAPIWorkFlowTask restCall, LoggingWorkFlowTask loggingTask) {
		// @formatter:off
		return SequentialFlow
				.Builder.aNewSequentialFlow().named("simple Sequential Infrastructure WorkFlow")
				.execute(restCall)
				.then(loggingTask)
				.build();
		// @formatter:on
	}


```

For the Workflow, the LogginWorkFlowTask argument is not defined in a @Bean method. This is because that WorkflowTask
has the @Component annotation and can be created by Spring's bean factory using the default constructor. In contracts
RestAPIWorkFlowTask needs a value supplied in the constructor to be created. As a result it is created in a method
using the @Bean annotation. It's best practise to create a unique name for this Bean as there might be multiple version
of Bean of this type created. In this example the default name is used (method name) to identify the Bean.

In this same configuration file, a simple Parallel Workflow can be defined.

```java

//START Parallel Example (WorkflowTasks and Workflow Definitions)
	@Bean
	LoggingWorkFlowTask simpleParallelTask1() {
		return new LoggingWorkFlowTask();
	}

	@Bean
	LoggingWorkFlowTask simpleParallelTask2() {
		return new LoggingWorkFlowTask();
	}

	@Bean
	LoggingWorkFlowTask simpleParallelTask3() {
		return new LoggingWorkFlowTask();
	}

	@Bean(name = "simpleParallelWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow simpleParallelWorkFlowTask(@Qualifier("simpleParallelTask1") LoggingWorkFlowTask simpleParallelTask1,
			@Qualifier("simpleParallelTask2") LoggingWorkFlowTask simpleParallelTask2,
			@Qualifier("simpleParallelTask3") LoggingWorkFlowTask simpleParallelTask3) {
		// @formatter:off
		return ParallelFlow
				.Builder.aNewParallelFlow().named("simple Parallel WorkFlow")
				.execute(simpleParallelTask1, simpleParallelTask2, simpleParallelTask3)
				.with(Executors.newFixedThreadPool(3)).build();
		// @formatter:on
	}
	//END Parallel Example (WorkflowTasks and Workflow Definitions)

```

In this example 3 instances of the LoggingWorkFlowTask tasks are created and used in the Workflow. This is obviously
just for example purposes as the instance of the Bean created by the Component could have been sufficed for reference in
the Workflow.

In this example the usage of @Infrastructure can also be seen. By providing this annotation on the Workflow definition
it allows the workflow-service to perform underlying tasks specific to that Workflow type. Refer to the
parodos-model-api README for more information on these annotations.

To run these examples:

- start the workflow-service in LOCAL mode for testing 'start_workflow_service.sh'
- run the examples in this project 'run_examples.sh'

### Complex Workflow

In this package the concept of a WorkflowChecker is demonstrated. This is a special WorkflowTask who executes calls to a 
checkWorkFlowStatus method. It is intended to monitor if a manual process initiated by another Workflow has
completed.

As can be seen in the MockApprovalWorkFlowCheckerTask when defining a WorkflowTask, after extending
BaseWorkFlowCheckerTask, only the checkWorkFlowStatus method needs to be defined. In this example is returns a COMPLETED
workflow status on the first call. In practise, this method might be called many times before such a state is obtained.

The ComplexWorkFlowConfiguration is where things get interesting. This file demonstrates running different WorkflowTasks
and checking on their status before running other Workflows.

Let's review the Workflows and their WorkflowCheckers.

The concept of an Assessment Workflow is introduced in this configuration. This specific WorkflowType runs its
execution logic and returns WorkflowOptions. A WorkflowOption provides a description of a Workflow and is useful when
creating a UI that provides Users with a choice of which workflow to run.

When defining an AssessmentTask, one or more WorkflowOption have to be supplied to the Task. In the execution logic it
will determine which (if any) of these Options can be returned. The WorkflowOptions wrapper contains some groupings to
place Workflow options that are IDP (internal developer platform) specific (ie: upgrading, migrating, onboarding
workflows).

In this configuration section a Workflow is created that does an Assessment and returns a WorkflowOption.

```java

	@Bean
	WorkFlowOption onboardingOption() {
		return new WorkFlowOption.Builder("onboardingOption",
				"onboardingWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
						.addToDetails("An example of a complex WorkFlow with Status checks").displayName("Onboarding")
						.setDescription("An example of a complex WorkFlow").build();
	}

	// Determines what WorkFlowOption if available based on Input
	@Bean
	OnboardingAssessmentTask onboardingAssessmentTask(
			@Qualifier("onboardingOption") WorkFlowOption awesomeToolsOption) {
		return new OnboardingAssessmentTask(awesomeToolsOption);
	}

	@Bean(name = "onboardingAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
	@Assessment
	WorkFlow assessmentWorkFlow(
			@Qualifier("onboardingAssessmentTask") OnboardingAssessmentTask onboardingAssessmentTask) {
		// @formatter:off
		return SequentialFlow.Builder.aNewSequentialFlow().named("onboarding Assessment WorkFlow")
				.execute(onboardingAssessmentTask)
				.build();
		// @formatter:on
	}

```

The WorkflowOption references a specific Workflow bean definition defined in the same file (onboardingWorkFlow" +
WorkFlowConstants.INFRASTRUCTURE_WORKFLOW).

This Workflow definition can be found in the same configuration. For simplicity’s sake, the LoggingWorkFlowTask is used
as WorkflowTask in this example by defining different instances of it, and referring to those instances by bean name 
when being referenced by a Workflow.

```java

	// Start onboardingWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW definition (this is the Workflow described in the WorkflowOption above)
	@Bean
	LoggingWorkFlowTask certWorkFlowTask(@Qualifier("namespaceWorkFlow"
			+ WorkFlowConstants.CHECKER_WORKFLOW) WorkFlow namespaceWorkFlowCheckerWorkFlow) {
		LoggingWorkFlowTask loggingWorkFlow = new LoggingWorkFlowTask();
		loggingWorkFlow.setWorkFlowChecker(namespaceWorkFlowCheckerWorkFlow);
		return loggingWorkFlow;
	}

	@Bean
	LoggingWorkFlowTask adGroupWorkFlowTask(@Qualifier("onboardingWorkFlow"
			+ WorkFlowConstants.CHECKER_WORKFLOW) WorkFlow onboardingWorkFlowCheckerWorkFlow) {
		LoggingWorkFlowTask loggingWorkFlow = new LoggingWorkFlowTask();
		loggingWorkFlow.setWorkFlowChecker(onboardingWorkFlowCheckerWorkFlow);
		return loggingWorkFlow;
	}

	@Bean
	LoggingWorkFlowTask dynatraceWorkFlowTask(@Qualifier("onboardingWorkFlow"
			+ WorkFlowConstants.CHECKER_WORKFLOW) WorkFlow onboardingWorkFlowCheckerWorkFlow) {
		LoggingWorkFlowTask loggingWorkFlow = new LoggingWorkFlowTask();
		loggingWorkFlow.setWorkFlowChecker(onboardingWorkFlowCheckerWorkFlow);
		return loggingWorkFlow;
	}

	//runs the set of Tasks associated with "Onboarding"
	@Bean(name = "onboardingWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow onboardingWorkflow(@Qualifier("certWorkFlowTask") LoggingWorkFlowTask certWorkFlowTask,
			@Qualifier("adGroupWorkFlowTask") LoggingWorkFlowTask adGroupWorkFlowTask,
			@Qualifier("dynatraceWorkFlowTask") LoggingWorkFlowTask dynatraceWorkFlowTask) {
		// @formatter:off
		return ParallelFlow.Builder.aNewParallelFlow()
				.named("onboarding Infrastructure WorkFlow")
				.execute(certWorkFlowTask, adGroupWorkFlowTask, dynatraceWorkFlowTask)
				.with(Executors.newFixedThreadPool(3))
				.build();
		// @formatter:on
	}
	// End onboardingWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW definition definition

```

Here the concept of WorkflowChecker can be seen in the WorkflowTask configurations. For example certWorkFlowTask
references the WorkflowChecker namespaceWorkFlowCheckerWorkFlow.

The definition of that Workflow can be seen in the same configuration file.

```java

	@Bean("namespaceWorkFlow" + WorkFlowConstants.CHECKER_WORKFLOW)
	@Checker(nextWorkFlowName = "networkingWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW,
			cronExpression = "0 0/1 * * * ?")
	WorkFlow namespaceWorkFlowCheckerWorkFlow(@Qualifier("gateThree") MockApprovalWorkFlowCheckerTask gateThree) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("namespace Checker WorkFlow").execute(gateThree)
				.build();
	}

```

In the @Checker definition, the nextWorkFlowName can be specified. This is the Workflow that can run, provided the
WorkflowChecker workflow successfully completes (meaning all WorkflowTasks return COMPLETED). The cronExpression of the
annotation will determine how often the Workflow will be executed. This is useful for manual processes that might take
hours (or even days). As all state is persisted, when the workflow-service is restarted, execution will resume.

WorkflowCheckers can be used to determine if required manual processes, outside the scope of Parodos, have completed.

#### Workflow fallback

Fallback workflow is a type of workflow that will be triggered once the main workflow is failed. To set up a fallback workflow:
1. define a fallback workflow with tasks to be executed for the fallback purpose
2. add the reference of fallback workflow's name to the field `fallbackWorkflow` in `@Infrastructure` annotation of the main workflow. In the example below, a fallback workflow `complexFallbackWorkFlow` is assigned to `complexWorkFlow`:
```java
	@Bean(name = "complexWorkFlow")
	@Infrastructure(parameters = { ... }, 
            fallbackWorkflow = "complexFallbackWorkFlow")
	WorkFlow complexWorkFlow(@Qualifier("subWorkFlowThree") WorkFlow subWorkFlowThree,
			@Qualifier("subWorkFlowFour") WorkFlow subWorkFlowFour) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("complexWorkFlow").execute(subWorkFlowThree)
				.then(subWorkFlowFour).build();
	}
```
The property `fallbackWorkflow` in `@Infrastructure` can be removed in order to remove the fallback workflow. By default, there will be no fallback for any workflows.

#### A Note On Defining WorkflowTasks for Usage In A Workflow

***Creating a Single Instance Of The Same Workflow Tasks***

WorkflowTasks can be defined as Spring Components (@Component). In this case the workflow-service will detect the
@Component tag and create a bean for this type. NOTE: When taking this option it keys that all dependencies required to
create the WorkflowTasks are available as other Spring Components and/or configuration values. Any unspecified
dependencies in a WorkflowTasks with @Component will result in the workflow-service being unable to start. By taking
this approach the WorkflowTask can be used by multiple Workflows without any extra code outside of that used in when
defining the WorkflowTasks where @Component was declared.

***Defining a WorkflowTasks that can be change with Configuration***

The values passed into a WorkflowTasks may vary depending on the Workflow using it. In this case the WorkflowTask can be
defined as a regular Java class (do not use the @Component declaration). In a class with the declaration @Configuration,
methods can be created returning an instance of the WorkflowTasks with different configurations applied to the reference.
These methods should have the @Bean declaration. Also, multiple instances of the same type will be created (and Spring
wires by type), the @Bean declaration should include a name (@Bean("myBean") or @Bean(name = "myBean")) . How this name
is reference will be covered in the next section.

***Package Structures and Workflow-Service's BeanWorkFlowRegistryImpl***

Ensure your packages have the base of 'com.redhat.parodos'. If a different structure is used, 'com.mycompany.parodos'
for example, any WorkFlows and Tasks defined will not be detected by the BeanWorkFlowRegistryImpl of the
workflow-service.

***A Word On Dependencies***

Please review the dependencies included in the workflow-service and ensure when creating Tasks and WorkFlows in an
external Jar (such as outlined in this project) that dependencies are not included that override existing dependencies
that the workflow-service has.

***Tips On Task and WorkFlow Complexity***

If you find yourself creating over complex Tasks and WorkFlow, you are not using Parodos for its intended use case.
Parodos is intended to tie together existing logic and tools. Tasks and WorkFlows should be posting messages on queues,
calling APIs and other lightweight integrations.
