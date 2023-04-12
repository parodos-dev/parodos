package com.redhat.parodos.examples.master;

import com.redhat.parodos.examples.master.checker.NamespaceApprovalWorkFlowCheckerTask;
import com.redhat.parodos.examples.master.checker.SslCertificationApprovalWorkFlowCheckerTask;
import com.redhat.parodos.examples.master.task.AdGroupsWorkFlowTask;
import com.redhat.parodos.examples.master.task.LoadBalancerWorkFlowTask;
import com.redhat.parodos.examples.master.task.NamespaceWorkFlowTask;
import com.redhat.parodos.examples.master.task.OnboardingAssessmentTask;
import com.redhat.parodos.examples.master.task.SingleSignOnWorkFlowTask;
import com.redhat.parodos.examples.master.task.SplunkMonitoringWorkFlowTask;
import com.redhat.parodos.examples.master.task.SslCertificationWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Assessment;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.annotation.Parameter;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.parameter.WorkFlowParameterType;
import com.redhat.parodos.workflows.workflow.ParallelFlow;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.Executors;

@Configuration
public class MasterWorkFlowConfiguration {

	// Assessment workflow
	@Bean
	WorkFlowOption onboardingOption() {
		return new WorkFlowOption.Builder("onboardingOption", "masterWorkFlow")
				.addToDetails("An example workflow option of a complex master WorkFlow with Status checks")
				.displayName("Onboarding").setDescription("An example of a complex WorkFlow").build();
	}

	// An AssessmentTask returns one or more WorkFlowOption wrapped in a WorkflowOptions
	@Bean
	OnboardingAssessmentTask onboardingAssessmentTask(
			@Qualifier("onboardingOption") WorkFlowOption awesomeToolsOption) {
		return new OnboardingAssessmentTask(awesomeToolsOption);
	}

	// A Workflow designed to execute and return WorkflowOption(s) that can be executed
	// next. In this case there is only one.
	@Bean(name = "onboardingAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
	@Assessment
	WorkFlow assessmentWorkFlow(
			@Qualifier("onboardingAssessmentTask") OnboardingAssessmentTask onboardingAssessmentTask) {
		// @formatter:off
        return SequentialFlow
        		.Builder
        		.aNewSequentialFlow()
                .named("onboardingAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
                .execute(onboardingAssessmentTask)
                .build();
        // @formatter:on
	}

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
	@Infrastructure(parameters = { @Parameter(key = "comment", description = "The workflow comment",
			type = WorkFlowParameterType.TEXT, optional = false) })
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
	@Checker(cronExpression = "*/5 * * * * ?")
	WorkFlow namespaceApprovalWorkFlowChecker(
			@Qualifier("namespaceApprovalWorkFlowCheckerTask") NamespaceApprovalWorkFlowCheckerTask namespaceApprovalWorkFlowCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("namespaceApprovalWorkFlowChecker")
				.execute(namespaceApprovalWorkFlowCheckerTask).build();
	}

	@Bean
	NamespaceWorkFlowTask namespaceWorkFlowTask(
			@Qualifier("namespaceApprovalWorkFlowChecker") WorkFlow namespaceApprovalWorkFlowChecker) {
		NamespaceWorkFlowTask namespaceWorkFlowTask = new NamespaceWorkFlowTask();
		namespaceWorkFlowTask.setWorkFlowCheckers(Arrays.asList(namespaceApprovalWorkFlowChecker));
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
	@Checker(cronExpression = "*/5 * * * * ?")
	WorkFlow sslCertificationApprovalWorkFlowChecker(
			@Qualifier("sslCertificationApprovalWorkFlowCheckerTask") SslCertificationApprovalWorkFlowCheckerTask sslCertificationApprovalWorkFlowCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("sslCertificationApprovalWorkFlowChecker")
				.execute(sslCertificationApprovalWorkFlowCheckerTask).build();
	}

	@Bean
	SslCertificationWorkFlowTask sslCertificationWorkFlowTask(
			@Qualifier("sslCertificationApprovalWorkFlowChecker") WorkFlow sslCertificationApprovalWorkFlowChecker) {
		SslCertificationWorkFlowTask sslCertificationWorkFlowTask = new SslCertificationWorkFlowTask();
		sslCertificationWorkFlowTask.setWorkFlowCheckers(Arrays.asList(sslCertificationApprovalWorkFlowChecker));
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

	@Bean(name = "masterWorkFlow")
	@Infrastructure(parameters = {
			@Parameter(key = "workloadId", description = "The workload id", type = WorkFlowParameterType.TEXT,
					optional = false),
			@Parameter(key = "projectUrl", description = "The project url", type = WorkFlowParameterType.URL,
					optional = true) })
	WorkFlow masterWorkFlow(@Qualifier("subWorkFlowThree") WorkFlow subWorkFlowThree,
			@Qualifier("subWorkFlowFour") WorkFlow subWorkFlowFour) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("masterWorkFlow").execute(subWorkFlowThree)
				.then(subWorkFlowFour).build();
	}

}
