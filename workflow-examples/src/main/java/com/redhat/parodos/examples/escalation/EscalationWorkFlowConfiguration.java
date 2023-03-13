package com.redhat.parodos.examples.escalation;

import java.util.Date;

import com.redhat.parodos.examples.escalation.task.SimpleTaskOne;
import com.redhat.parodos.examples.escalation.task.SimpleTaskOneEscalator;
import com.redhat.parodos.examples.escalation.task.SimpleTaskTwo;
import com.redhat.parodos.examples.escalation.checker.SimpleTaskOneChecker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Escalation;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 *
 * A sample of how to configure a Workflow that has Tasks, Checkers and Escalation(s).
 *
 * All the Workflow Beans and WorkflowTask Bean instances are defined here
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Gloria Ciavarrini (Github: gciavarrini)
 *
 */
@Configuration
public class EscalationWorkFlowConfiguration {

	// ********** Start First Workflow **************

	@Bean
	@Infrastructure
	public WorkFlow workflowStartingCheckingAndEscalation(SimpleTaskOne simpleTaskOne) {
		// @formatter:off
		return SequentialFlow.Builder
				.aNewSequentialFlow()
				.named("workflowStartingCheckingAndEscalation")
				.execute(simpleTaskOne)
				.build();
		// @formatter:on
	}

	// Task executed by workflowStartingCheckingAndEscalation
	@Bean
	public SimpleTaskOne simpleTaskOne(
			@Qualifier("simpleTaskOneCheckerWorkflow") WorkFlow simpleTaskOneCheckerWorkflow) {
		SimpleTaskOne taskOne = new SimpleTaskOne();
		taskOne.setWorkFlowChecker(simpleTaskOneCheckerWorkflow);
		return taskOne;
	}

	// ********** End First Workflow **************

	// ********** Start Checker + Escalation **************
	@Bean
	@Checker(cronExpression = "*/5 * * * * ?")
	public WorkFlow simpleTaskOneCheckerWorkflow(
			@Qualifier("simpleTaskOneCheckerTask") SimpleTaskOneChecker simpleTaskOneCheckerTask) {
		// @formatter:off
		return SequentialFlow.Builder
				.aNewSequentialFlow()
				.named("simpleTaskOneCheckerWorkflow")
				.execute(simpleTaskOneCheckerTask)
				.build();
		// @formatter:on
	}

	@Bean
	public SimpleTaskOneChecker simpleTaskOneCheckerTask(
			@Qualifier("simpleTaskOneEscalatorWorkflow") WorkFlow simpleTaskOneEscalatorWorkflow) {
		return new SimpleTaskOneChecker(simpleTaskOneEscalatorWorkflow, new Date().getTime() / 1000 + 30);
	}

	@Bean
	@Escalation
	public WorkFlow simpleTaskOneEscalatorWorkflow(
			@Qualifier("simpleTaskOneEscalator") SimpleTaskOneEscalator simpleTaskOneEscalator) {
		// @formatter:off
		return SequentialFlow.Builder
				.aNewSequentialFlow()
				.named("simpleTaskOneEscalatorWorkflow")
				.execute(simpleTaskOneEscalator)
				.build();
		// @formatter:on

	}

	@Bean
	public SimpleTaskOneEscalator simpleTaskOneEscalator() {
		return new SimpleTaskOneEscalator();
	}

	// ********** End Checker + Escalation **************

	// ********** Start Second Workflow **************
	// This is the workflow that runs if simpleTaskOneCheckerWorkflow (Checker for task
	// SimpleTaskOne) is successful
	@Bean
	@Infrastructure
	public WorkFlow workflowContinuesAfterCheckingEscalation(SimpleTaskTwo simpleTaskTwo) {
		// @formatter:off
		return SequentialFlow.Builder
				.aNewSequentialFlow()
				.named("workflowContinuesAfterCheckingEscalation")
				.execute(simpleTaskTwo)
				.build();
		// @formatter:on
	}

	@Bean
	public SimpleTaskTwo simpleTaskTwo() {
		return new SimpleTaskTwo();
	}
	// ********** End Second Workflow **************

}
