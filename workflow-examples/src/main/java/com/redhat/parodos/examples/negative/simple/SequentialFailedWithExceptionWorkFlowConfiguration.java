package com.redhat.parodos.examples.negative.simple;

import com.redhat.parodos.examples.negative.simple.task.FailedWithExceptionWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SequentialFailedWithExceptionWorkFlowConfiguration {

	@Bean
	FailedWithExceptionWorkFlowTask failedWithExceptionWorkFlowTask() {
		return new FailedWithExceptionWorkFlowTask();
	}

	@Bean(name = "sequentialFailedWithExceptionWorkFlow")
	@Infrastructure
	WorkFlow simpleFailedWorkFlow(
			@Qualifier("failedWithExceptionWorkFlowTask") FailedWithExceptionWorkFlowTask failedWorkFlowTask) {
		// @formatter:off
		return SequentialFlow
				.Builder.aNewSequentialFlow()
				.named("sequentialFailedWithExceptionWorkFlow")
				.execute(failedWorkFlowTask)
				.build();
		// @formatter:on
	}

}
