package com.redhat.parodos.examples.negative.simple;

import com.redhat.parodos.examples.negative.simple.task.DoNothingWorkFlowTask;
import com.redhat.parodos.examples.negative.simple.task.FailedWithAlertMessageWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FailedWithAlertMessageWorkFlowConfiguration {

	@Bean
	FailedWithAlertMessageWorkFlowTask FailedWithAlertMessageWorkFlowTask() {
		return new FailedWithAlertMessageWorkFlowTask();
	}

	@Bean(name = "doNothingAgainWorkFlowTask")
	DoNothingWorkFlowTask doNothingWorkFlowTask() {
		return new DoNothingWorkFlowTask();
	}

	@Bean(name = "failedWithAlertMessageWorkFlow")
	@Infrastructure
	WorkFlow failedWithAlertMessageWorkFlow(
			@Qualifier("failedWithAlertMessageWorkFlowTask") FailedWithAlertMessageWorkFlowTask failedWithAlertMessageWorkFlowTask,
			@Qualifier("doNothingAgainWorkFlowTask") DoNothingWorkFlowTask doNothingWorkFlowTask) {
		// @formatter:off
		return SequentialFlow
				.Builder.aNewSequentialFlow()
				.named("failedWithAlertMessageWorkFlow")
				.execute(doNothingWorkFlowTask)
				.then(failedWithAlertMessageWorkFlowTask)
				.build();
		// @formatter:on
	}

}
