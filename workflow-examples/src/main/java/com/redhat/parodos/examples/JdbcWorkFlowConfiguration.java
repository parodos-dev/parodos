package com.redhat.parodos.examples;

import com.redhat.parodos.tasks.jdbc.JdbcWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdbcWorkFlowConfiguration {

	@Bean
	JdbcWorkFlowTask jdbcTask() {
		return new JdbcWorkFlowTask();
	}

	@Bean
	@Infrastructure
	WorkFlow jdbcWorkFlow(JdbcWorkFlowTask jdbcTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("jdbcWorkFlow").execute(jdbcTask).build();
	}

}
