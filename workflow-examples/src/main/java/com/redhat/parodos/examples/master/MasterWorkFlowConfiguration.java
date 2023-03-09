package com.redhat.parodos.examples.master;

import com.redhat.parodos.examples.master.task.*;
import com.redhat.parodos.examples.master.task.checker.NamespaceApprovalWorkFlowCheckerTask;
import com.redhat.parodos.examples.master.task.checker.SslCertificationApprovalWorkFlowCheckerTask;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.annotation.Parameter;
import com.redhat.parodos.workflow.parameter.WorkFlowParameterType;
import com.redhat.parodos.workflows.workflow.ParallelFlow;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class MasterWorkFlowConfiguration {

	// SUB WORKFLOW ONE:
	// Parallel Flow:
	// - AdGroupsWorkFlowTask
	// - SplunkMonitoringWorkFlowTask

	@Bean
	AdGroupsWorkFlowTask adGroupsWorkFlowTask() {
		return new AdGroupsWorkFlowTask();
	}

	@Bean
	SplunkMonitoringWorkFlowTask splunkMonitoringWorkFlowTask() {
		return new SplunkMonitoringWorkFlowTask();
	}

	@Bean(name = "subWorkFlowOne")
	@Infrastructure
	WorkFlow subWorkFlowOne(@Qualifier("adGroupsWorkFlowTask") AdGroupsWorkFlowTask adGroupsWorkFlowTask,
			@Qualifier("splunkMonitoringWorkFlowTask") SplunkMonitoringWorkFlowTask splunkMonitoringWorkFlowTask) {
		return ParallelFlow.Builder.aNewParallelFlow().named("subWorkFlowOne")
				.execute(adGroupsWorkFlowTask, splunkMonitoringWorkFlowTask).with(Executors.newFixedThreadPool(2))
				.build();
	}

	// SUB WORKFLOW TWO:
	// Sequential Flow:
	// - SubWorkFlowOne
	// - NamespaceWorkFlowTask
	// - NamespaceApprovalWorkFlowCheckerTask

	@Bean
	NamespaceApprovalWorkFlowCheckerTask namespaceApprovalWorkFlowCheckerTask() {
		return new NamespaceApprovalWorkFlowCheckerTask();
	}

	@Bean(name = "namespaceApprovalWorkFlowChecker")
	@Checker(cronExpression = "0 0/1 * * * ?")
	WorkFlow namespaceApprovalWorkFlowChecker(
			@Qualifier("namespaceApprovalWorkFlowCheckerTask") NamespaceApprovalWorkFlowCheckerTask namespaceApprovalWorkFlowCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("namespaceApprovalWorkFlowChecker")
				.execute(namespaceApprovalWorkFlowCheckerTask).build();
	}

	@Bean
	NamespaceWorkFlowTask namespaceWorkFlowTask(
			@Qualifier("namespaceApprovalWorkFlowChecker") WorkFlow namespaceApprovalWorkFlowChecker) {
		NamespaceWorkFlowTask namespaceWorkFlowTask = new NamespaceWorkFlowTask();
		namespaceWorkFlowTask.setWorkFlowChecker(namespaceApprovalWorkFlowChecker);
		return namespaceWorkFlowTask;
	}

	@Bean(name = "subWorkFlowTwo")
	@Infrastructure
	WorkFlow subWorkFlowTwo(@Qualifier("subWorkFlowOne") WorkFlow subWorkFlowOne,
			@Qualifier("namespaceWorkFlowTask") NamespaceWorkFlowTask namespaceWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("subWorkFlowTwo").execute(subWorkFlowOne)
				.then(namespaceWorkFlowTask).build();
	}

	// SUB WORKFLOW THREE:
	// Parallel Flow:
	// - SslCertificationWorkFlowTask
	// - SslCertificationApprovalWorkFlowCheckerTask
	// - SubWorkFlowTwo

	@Bean
	SslCertificationApprovalWorkFlowCheckerTask sslCertificationApprovalWorkFlowCheckerTask() {
		return new SslCertificationApprovalWorkFlowCheckerTask();
	}

	@Bean(name = "sslCertificationApprovalWorkFlowChecker")
	@Checker(cronExpression = "0 0/1 * * * ?")
	WorkFlow sslCertificationApprovalWorkFlowChecker(
			@Qualifier("sslCertificationApprovalWorkFlowCheckerTask") SslCertificationApprovalWorkFlowCheckerTask sslCertificationApprovalWorkFlowCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("sslCertificationApprovalWorkFlowChecker")
				.execute(sslCertificationApprovalWorkFlowCheckerTask).build();
	}

	@Bean
	SslCertificationWorkFlowTask sslCertificationWorkFlowTask(
			@Qualifier("sslCertificationApprovalWorkFlowChecker") WorkFlow sslCertificationApprovalWorkFlowChecker) {
		SslCertificationWorkFlowTask sslCertificationWorkFlowTask = new SslCertificationWorkFlowTask();
		sslCertificationWorkFlowTask.setWorkFlowChecker(sslCertificationApprovalWorkFlowChecker);
		return sslCertificationWorkFlowTask;
	}

	@Bean(name = "subWorkFlowThree")
	@Infrastructure
	WorkFlow subWorkFlowThree(
			@Qualifier("sslCertificationWorkFlowTask") SslCertificationWorkFlowTask sslCertificationWorkFlowTask,
			@Qualifier("subWorkFlowTwo") WorkFlow subWorkFlowTwo) {
		return ParallelFlow.Builder.aNewParallelFlow().named("subWorkFlowThree")
				.execute(sslCertificationWorkFlowTask, subWorkFlowTwo).with(Executors.newFixedThreadPool(2)).build();
	}

	// SUB WORKFLOW FOUR:
	// Parallel Flow:
	// - LoadBalancerWorkFlowTask
	// - SingleSignOnWorkFlowTask

	@Bean
	LoadBalancerWorkFlowTask loadBalancerWorkFlowTask() {
		return new LoadBalancerWorkFlowTask();
	}

	@Bean
	SingleSignOnWorkFlowTask singleSignOnWorkFlowTask() {
		return new SingleSignOnWorkFlowTask();
	}

	@Bean(name = "subWorkFlowFour")
	@Infrastructure
	WorkFlow subWorkFlowFour(@Qualifier("loadBalancerWorkFlowTask") LoadBalancerWorkFlowTask loadBalancerWorkFlowTask,
			@Qualifier("singleSignOnWorkFlowTask") SingleSignOnWorkFlowTask singleSignOnWorkFlowTask) {
		return ParallelFlow.Builder.aNewParallelFlow().named("subWorkFlowFour")
				.execute(loadBalancerWorkFlowTask, singleSignOnWorkFlowTask).with(Executors.newFixedThreadPool(2))
				.build();
	}

	// USER WORKFLOW
	// Sequential Flow:
	// - subWorkFlowThree
	// - subWorkFlowFour

	@Bean(name = "userWorkFlow")
	@Infrastructure(parameters = {
			@Parameter(key = "projectId", description = "The project id", type = WorkFlowParameterType.TEXT,
					optional = false),
			@Parameter(key = "projectRepoUrl", description = "The project repo url", type = WorkFlowParameterType.URL,
					optional = true) })
	WorkFlow userWorkFlow(@Qualifier("subWorkFlowThree") WorkFlow subWorkFlowThree,
			@Qualifier("subWorkFlowFour") WorkFlow subWorkFlowFour) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("masterWorkFlow").execute(subWorkFlowThree)
				.then(subWorkFlowFour).build();
	}

}
