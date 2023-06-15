package com.redhat.parodos.examples.negative.simple;

import com.redhat.parodos.examples.negative.simple.task.DoNothingWorkFlowTask;
import com.redhat.parodos.examples.negative.simple.task.PendingWithAlertMessageWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PendingWithAlertMessageWorkFlowConfiguration {

	@Bean
	PendingWithAlertMessageWorkFlowTask pendingWithAlertMessageWorkFlowTask() {
		return new PendingWithAlertMessageWorkFlowTask();
	}

	@Bean
	DoNothingWorkFlowTask doNothingWorkFlowTask() {
		return new DoNothingWorkFlowTask();
	}

	@Bean(name = "pendingWithAlertMessageWorkFlow")
	@Infrastructure
	WorkFlow pendingWithAlertMessageWorkFlow(
			@Qualifier("pendingWithAlertMessageWorkFlowTask") PendingWithAlertMessageWorkFlowTask pendingWithAlertMessageWorkFlowTask,
			@Qualifier("doNothingWorkFlowTask") DoNothingWorkFlowTask doNothingWorkFlowTask) {
		// @formatter:off
		return SequentialFlow
				.Builder.aNewSequentialFlow()
				.named("pendingWithAlertMessageWorkFlow")
				.execute(doNothingWorkFlowTask)
				.then(pendingWithAlertMessageWorkFlowTask)
				.build();
		// @formatter:on
	}

}
