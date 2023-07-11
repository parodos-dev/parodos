package com.redhat.parodos.examples;

import java.io.IOException;

import com.redhat.parodos.tasks.kubeapi.KubeapiWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * Sample configuration for Kubeapi Task
 *
 */
@Configuration
public class KubeapiWorkFlowConfiguration {

	@Bean
	KubeapiWorkFlowTask kubeapiTask() throws IOException {
		return new KubeapiWorkFlowTask();
	}

	@Bean
	@Infrastructure
	WorkFlow kubeapiWorkFlow(@Qualifier("kubeapiTask") KubeapiWorkFlowTask kubeapiTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("kubeapiWorkFlow").execute(kubeapiTask).build();
	}

}
