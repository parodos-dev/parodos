package com.redhat.parodos.infrastructure;

import static com.redhat.parodos.infrastructure.task.InfrastructureTaskAware.INFRASTRUCTURE_TASK_WORKFLOW;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.redhat.parodos.infrastructure.option.InfrastructureOption;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * This is a Sample InfrastructureOption. It represents everything a developer
 * needs to get coding (ie: CI/CD, AD groups, environments, etc). The logic for
 * how this is all provision lives outside of Parodos. The goal of Parodos is to
 * help determine when this stack should be provisioned and stream line the
 * process for the developers hoping to use all these tools
 * 
 * Also included in this configuration is the Workflow to create this option
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Configuration
public class InfrastructureConfig {

	// There will be lots of beans of InfrastructureOption types - names and
	// qualifiers will make sure the right tasks are put into the workflow
	@Bean(name = "awesomeToolStack")
	InfrastructureOption awesomeToolStack() {
		return new InfrastructureOption
				.Builder("Awesome Tool Stack With Correct Permissions And Config", "AwesomeToolsAndEnvironment" + INFRASTRUCTURE_TASK_WORKFLOW)
				.displayName("Managed Environment")
				.addToDetails("Charge Back: .30/hr")
				.addToDetails("Team must supply their own logging solution")
				.addToDetails("SLA: 99.999 (based on Azure's SLA)")
				.addToDetails("24/7 support with a 2 hour response time")
				.addToDetails("After developing with these tools for 1 hour you will feel better about your life")
				.setDescription("Managed Environment complete with CI/CD and configured Pipelines").build();
	}

	// There will be lots of beans of InfrastructureTask type - names and qualifiers
	// will make sure the right tasks are put into the workflow
	@Bean(name = "restApiTask")
	CallCustomRestAPITask restApiTask() {
		return new CallCustomRestAPITask();
	}

	// There will be lots of beans of InfrastructureTask type - names and qualifiers
	// will make sure the right tasks are put into the workflow
	@Bean(name = "anotherTask")
	AnotherTask anotherTask() {
		return new AnotherTask();
	}

	// There will be lots of beans of InfrastructureTask type - names and qualifiers
	// will make sure the right tasks are put into the workflow
	@Bean(name= "awesomeToolStackWorkFlow" + INFRASTRUCTURE_TASK_WORKFLOW)
	WorkFlow superAwesomeTechnologyStackWorkflow(@Qualifier("restApiTask") CallCustomRestAPITask restApiTask, @Qualifier("anotherTask") AnotherTask anotherTask) {
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("AwesomeToolsAndEnvironment_" + INFRASTRUCTURE_TASK_WORKFLOW)
				.execute(restApiTask)
				.then(anotherTask)
				.build();
	}
}
