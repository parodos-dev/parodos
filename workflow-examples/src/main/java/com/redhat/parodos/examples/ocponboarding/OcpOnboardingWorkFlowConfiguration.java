/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.examples.ocponboarding;

import java.util.Date;
import java.util.List;

import com.redhat.parodos.examples.ocponboarding.checker.JiraTicketApprovalWorkFlowCheckerTask;
import com.redhat.parodos.examples.ocponboarding.escalation.JiraTicketApprovalEscalationWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.AppLinkNotificationWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.JiraTicketCreationWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.JiraTicketUpdateNotificationWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.OcpAppDeploymentWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.assessment.OnboardingOcpAssessmentTask;
import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflow.annotation.Assessment;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Escalation;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.annotation.Parameter;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OcpOnboardingWorkFlowConfiguration {

	// Assessment workflow
	@Bean
	WorkFlowOption ocpOnboardingOption() {
		return new WorkFlowOption.Builder("ocpOnboarding", "ocpOnboardingWorkFlow")
				.addToDetails("this is for the app to deploy on OCP").displayName("Onboarding to OCP")
				.setDescription("this is for the app to deploy on OCP").build();
	}

	@Bean
	WorkFlowOption move2kube() {
		return new WorkFlowOption.Builder("move2kube", "move2KubeWorkFlow_INFRASTRUCTURE_WORKFLOW")
				.addToDetails("Transform the application into a Kubernetes application.").displayName("move2kube")
				.setDescription("Transform application using move2kube").recommended(true).build();
	}

	@Bean
	WorkFlowOption badRepoOption() {
		return new WorkFlowOption.Builder("badRepoOption",
				"simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
						.addToDetails("Container Fundamentals Training Required").displayName("Training Required")
						.setDescription("Container Fundamentals Training Required").build();
	}

	@Bean
	WorkFlowOption notSupportOption() {
		return new WorkFlowOption.Builder("notSupportOption",
				"simpleSequentialWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
						.addToDetails("Non-Supported Workflow Steps").displayName("Not Supported")
						.setDescription("Non-Supported Workflow Steps").build();
	}

	@Bean
	WorkFlowOption analyzeOption() {
		return new WorkFlowOption.Builder("analyzeOption", "AnalyzeApplicationAssessment")
				.addToDetails("Analyze an application's source code before migrating it."
						+ " Produces a containerization report by MTA")
				.displayName("Migration Analysis").setDescription("Migration Analysis step").build();
	}

	@Bean
	WorkFlowOption alertMessageOption() {
		return new WorkFlowOption.Builder("alert message option", "failedWithAlertMessageWorkFlow")
				.addToDetails("this is to show alert in workflow").displayName("workflow with alert message")
				.setDescription("this is to show alert in workflow").build();
	}

	// An AssessmentTask returns one or more WorkFlowOption wrapped in a WorkflowOptions
	@Bean
	OnboardingOcpAssessmentTask onboardingAssessmentTask(
			@Qualifier("ocpOnboardingOption") WorkFlowOption ocpOnboardingOption,
			@Qualifier("badRepoOption") WorkFlowOption badRepoOption,
			@Qualifier("notSupportOption") WorkFlowOption notSupportOption,
			@Qualifier("move2kube") WorkFlowOption move2kube, @Qualifier("analyzeOption") WorkFlowOption analyzeOption,
			@Qualifier("alertMessageOption") WorkFlowOption alertMessageOption,
			@Qualifier("vmOnboardingOption") WorkFlowOption vmOnboardingOption) {
		return new OnboardingOcpAssessmentTask(List.of(ocpOnboardingOption, badRepoOption, notSupportOption, move2kube,
				analyzeOption, alertMessageOption, vmOnboardingOption));
	}

	// A Workflow designed to execute and return WorkflowOption(s) that can be executed
	// next. In this case there is only one.
	@Bean(name = "onboardingAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
	@Assessment
	WorkFlow assessmentWorkFlow(
			@Qualifier("onboardingAssessmentTask") OnboardingOcpAssessmentTask onboardingAssessmentTask) {
		// @formatter:off
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("onboardingAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
				.execute(onboardingAssessmentTask)
				.build();
		// @formatter:on
	}

	// WORKFLOW A - Sequential Flow:
	// @formatter:off
	// - JiraTicketCreationWorkFlowTask -> JiraTicketApprovalWorkFlowCheckerTask -> JiraTicketApprovalEscalationWorkFlowTask
	// - JiraTicketUpdateNotificationWorkFlowTask
	// @formatter:on

	@Bean
	JiraTicketApprovalEscalationWorkFlowTask jiraTicketApprovalEscalationWorkFlowTask(Notifier notifier,
			@Value("${ESCALATION_USER_ID:test}") String escalationUserId) {
		return new JiraTicketApprovalEscalationWorkFlowTask(notifier, escalationUserId);
	}

	@Bean(name = "jiraTicketApprovalEscalationWorkFlow")
	@Escalation
	WorkFlow jiraTicketApprovalEscalationWorkFlow(
			@Qualifier("jiraTicketApprovalEscalationWorkFlowTask") JiraTicketApprovalEscalationWorkFlowTask jiraTicketApprovalEscalationWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("jiraTicketApprovalEscalationWorkFlow")
				.execute(jiraTicketApprovalEscalationWorkFlowTask).build();
	}

	@Bean
	JiraTicketApprovalWorkFlowCheckerTask jiraTicketApprovalWorkFlowCheckerTask(
			@Qualifier("jiraTicketApprovalEscalationWorkFlow") WorkFlow jiraTicketApprovalEscalationWorkFlow,
			@Value("${JIRA_URL:test}") String url, @Value("${JIRA_USER:user}") String username,
			@Value("${JIRA_TOKEN:token}") String password) {
		return new JiraTicketApprovalWorkFlowCheckerTask(jiraTicketApprovalEscalationWorkFlow,
				new Date().getTime() / 1000 + 30, url, username, password);
	}

	@Bean(name = "jiraTicketApprovalWorkFlowChecker")
	@Checker(cronExpression = "*/5 * * * * ?")
	WorkFlow jiraTicketApprovalWorkFlowChecker(
			@Qualifier("jiraTicketApprovalWorkFlowCheckerTask") JiraTicketApprovalWorkFlowCheckerTask jiraTicketApprovalWorkFlowCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("jiraTicketApprovalWorkFlowChecker")
				.execute(jiraTicketApprovalWorkFlowCheckerTask).build();
	}

	@Bean
	JiraTicketCreationWorkFlowTask jiraTicketCreationWorkFlowTask(
			@Qualifier("jiraTicketApprovalWorkFlowChecker") WorkFlow jiraTicketApprovalWorkFlowChecker,
			@Value("${JIRA_URL:test}") String url, @Value("${JIRA_USER:user}") String username,
			@Value("${JIRA_TOKEN:token}") String password, @Value("${JIRA_APPROVER:approver}") String approverId) {
		JiraTicketCreationWorkFlowTask jiraTicketCreationWorkFlowTask = new JiraTicketCreationWorkFlowTask(url,
				username, password, approverId);
		jiraTicketCreationWorkFlowTask.setWorkFlowCheckers(List.of(jiraTicketApprovalWorkFlowChecker));
		return jiraTicketCreationWorkFlowTask;
	}

	@Bean
	JiraTicketUpdateNotificationWorkFlowTask jiraTicketUpdateNotificationWorkFlowTask(Notifier notifier) {
		return new JiraTicketUpdateNotificationWorkFlowTask(notifier);
	}

	@Bean(name = "workFlowA")
	@Infrastructure
	WorkFlow workFlowA(
			@Qualifier("jiraTicketCreationWorkFlowTask") JiraTicketCreationWorkFlowTask jiraTicketCreationWorkFlowTask,
			@Qualifier("jiraTicketUpdateNotificationWorkFlowTask") JiraTicketUpdateNotificationWorkFlowTask jiraTicketUpdateNotificationWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("workFlowA").execute(jiraTicketCreationWorkFlowTask)
				.then(jiraTicketUpdateNotificationWorkFlowTask).build();
	}

	// WORKFLOW B - Sequential Flow:
	// - OcpAppDeploymentWorkFlowTask -> AppLinkNotificationWorkFlowTask -
	// CompletionNotificationWorkFlowTask

	@Bean
	OcpAppDeploymentWorkFlowTask ocpAppDeploymentWorkFlowTask(
			@Value("${CLUSTER_API_URL:cluster}") String clusterApiUrl) {
		return new OcpAppDeploymentWorkFlowTask(clusterApiUrl);
	}

	@Bean
	AppLinkNotificationWorkFlowTask appLinkNotificationWorkFlowTask(Notifier notifier) {
		return new AppLinkNotificationWorkFlowTask(notifier);
	}

	@Bean(name = "workFlowB")
	@Infrastructure
	WorkFlow workFlowB(
			@Qualifier("ocpAppDeploymentWorkFlowTask") OcpAppDeploymentWorkFlowTask ocpAppDeploymentWorkFlowTask,
			@Qualifier("appLinkNotificationWorkFlowTask") AppLinkNotificationWorkFlowTask appLinkNotificationWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("workFlowB").execute(ocpAppDeploymentWorkFlowTask)
				.then(appLinkNotificationWorkFlowTask).build();
	}

	// OCP ONBOARDING WORKFLOW - Sequential Flow:
	// - workFlowA
	// - workFlowB

	@Bean(name = "ocpOnboardingWorkFlow")
	@Infrastructure(parameters = { @Parameter(key = "NAMESPACE", description = "The namespace in the ocp cluster",
			type = WorkParameterType.TEXT, optional = false) })
	WorkFlow ocpOnboardingWorkFlow(@Qualifier("workFlowA") WorkFlow workFlowA,
			@Qualifier("workFlowB") WorkFlow workFlowB) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("ocpOnboardingWorkFlow").execute(workFlowA)
				.then(workFlowB).build();
	}

}