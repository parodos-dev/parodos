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
import com.redhat.parodos.examples.ocponboarding.task.AppLinkEmailNotificationWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.JiraTicketCreationWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.JiraTicketEmailNotificationWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.NotificationWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.OcpAppDeploymentWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.assessment.OnboardingOcpAssessmentTask;
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
	WorkFlowOption onboardingOcpOption() {
		return new WorkFlowOption.Builder("ocpOnboarding", "ocpOnboardingWorkFlow")
				.addToDetails("this is for the app to deploy on OCP").displayName("Onboarding to OCP")
				.setDescription("this is for the app to deploy on OCP").build();
	}

	@Bean
	WorkFlowOption move2kube() {
		return new WorkFlowOption.Builder("move2kube", "move2KubeWorkFlow_INFRASTRUCTURE_WORKFLOW")
				.addToDetails("Transform the application into a Kubernetes application.").displayName("move2kube")
				.setDescription("Transform application using move2kube").build();
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

	// An AssessmentTask returns one or more WorkFlowOption wrapped in a WorkflowOptions
	@Bean
	OnboardingOcpAssessmentTask onboardingAssessmentTask(
			@Qualifier("onboardingOcpOption") WorkFlowOption onboardingOcpOption,
			@Qualifier("badRepoOption") WorkFlowOption badRepoOption,
			@Qualifier("notSupportOption") WorkFlowOption notSupportOption,
			@Qualifier("move2kube") WorkFlowOption move2kube) {
		return new OnboardingOcpAssessmentTask(
				List.of(onboardingOcpOption, badRepoOption, notSupportOption, move2kube));
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
	// - JiraTicketEmailNotificationWorkFlowTask
	// @formatter:on

	@Bean
	JiraTicketApprovalEscalationWorkFlowTask jiraTicketApprovalEscalationWorkFlowTask(
			@Value("${MAIL_SERVICE_URL:service}") String mailServiceUrl,
			@Value("${MAIL_SERVICE_SITE_NAME_ESCALATION:site}") String mailServiceSiteName) {
		return new JiraTicketApprovalEscalationWorkFlowTask(mailServiceUrl, mailServiceSiteName);
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
	JiraTicketEmailNotificationWorkFlowTask jiraTicketEmailNotificationWorkFlowTask(
			@Value("${MAIL_SERVICE_URL:service}") String mailServiceUrl,
			@Value("${MAIL_SERVICE_SITE_NAME_JIRA:site}") String mailServiceSiteName) {
		return new JiraTicketEmailNotificationWorkFlowTask(mailServiceUrl, mailServiceSiteName);
	}

	@Bean(name = "workFlowA")
	@Infrastructure
	WorkFlow workFlowA(
			@Qualifier("jiraTicketCreationWorkFlowTask") JiraTicketCreationWorkFlowTask jiraTicketCreationWorkFlowTask,
			@Qualifier("jiraTicketEmailNotificationWorkFlowTask") JiraTicketEmailNotificationWorkFlowTask jiraTicketEmailNotificationWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("workFlowA").execute(jiraTicketCreationWorkFlowTask)
				.then(jiraTicketEmailNotificationWorkFlowTask).build();
	}

	// WORKFLOW B - Sequential Flow:
	// - OcpAppDeploymentWorkFlowTask
	// - NotificationWorkFlowTask
	// - AppLinkEmailNotificationWorkFlowTask
	@Bean
	OcpAppDeploymentWorkFlowTask ocpAppDeploymentWorkFlowTask(
			@Value("${CLUSTER_API_URL:cluster}") String clusterApiUrl) {
		return new OcpAppDeploymentWorkFlowTask(clusterApiUrl);
	}

	@Bean
	NotificationWorkFlowTask notificationWorkFlowTask(
			@Value("${NOTIFICATION_SERVER_URL:test}") String notificationServiceUrl) {
		return new NotificationWorkFlowTask(notificationServiceUrl);
	}

	@Bean
	AppLinkEmailNotificationWorkFlowTask appLinkEmailNotificationWorkFlowTask(
			@Value("${MAIL_SERVICE_URL:service}") String mailServiceUrl,
			@Value("${MAIL_SERVICE_SITE_NAME_APP:site}") String mailServiceSiteName) {
		return new AppLinkEmailNotificationWorkFlowTask(mailServiceUrl, mailServiceSiteName);
	}

	@Bean(name = "workFlowB")
	@Infrastructure
	WorkFlow workFlowB(
			@Qualifier("ocpAppDeploymentWorkFlowTask") OcpAppDeploymentWorkFlowTask ocpAppDeploymentWorkFlowTask,
			@Qualifier("notificationWorkFlowTask") NotificationWorkFlowTask notificationWorkFlowTask,
			@Qualifier("appLinkEmailNotificationWorkFlowTask") AppLinkEmailNotificationWorkFlowTask appLinkEmailNotificationWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("workFlowB").execute(ocpAppDeploymentWorkFlowTask)
				.then(notificationWorkFlowTask).then(appLinkEmailNotificationWorkFlowTask).build();
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
