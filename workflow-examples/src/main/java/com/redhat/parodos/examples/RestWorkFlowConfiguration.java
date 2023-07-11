package com.redhat.parodos.examples;

import com.redhat.parodos.tasks.rest.RestWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestWorkFlowConfiguration {

	@Bean
	RestWorkFlowTask restTask() {
		return new RestWorkFlowTask();
	}

	@Bean
	@Infrastructure
	WorkFlow restWorkFlow(@Qualifier("restTask") RestWorkFlowTask restTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("restWorkFlow").execute(restTask).build();
	}

}
