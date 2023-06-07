package com.redhat.parodos.examples.prebuilt.project;

import java.util.Date;
import java.util.List;

import com.redhat.parodos.tasks.project.ProjectAccessRequestApprovalWorkFlowTask;
import com.redhat.parodos.tasks.project.ProjectAccessRequestWorkFlowTask;
import com.redhat.parodos.tasks.project.checker.ProjectAccessRequestApprovalWorkFlowCheckerTask;
import com.redhat.parodos.tasks.project.escalation.ProjectAccessRequestEscalationWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Escalation;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ProjectAccessRequestWorkFlowConfiguration {

	@Bean
	ProjectAccessRequestEscalationWorkFlowTask projectAccessRequestEscalationWorkFlowTask(
			@Value("${SERVICE_URL:localhost}") String serviceUrl, @Value("${SERVICE_PORT:8080}") String servicePort,
			@Value("${MAIL_SERVICE_URL:service}") String mailServiceUrl) {
		return new ProjectAccessRequestEscalationWorkFlowTask(serviceUrl, servicePort, mailServiceUrl);
	}

	@Bean(name = "projectAccessRequestEscalationWorkFlow")
	@Escalation
	WorkFlow projectAccessRequestEscalationWorkFlow(
			@Qualifier("projectAccessRequestEscalationWorkFlowTask") ProjectAccessRequestEscalationWorkFlowTask projectAccessRequestEscalationWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("projectAccessRequestEscalationWorkFlow")
				.execute(projectAccessRequestEscalationWorkFlowTask).build();
	}

	@Bean
	ProjectAccessRequestApprovalWorkFlowCheckerTask projectAccessRequestApprovalWorkFlowCheckerTask(
			@Qualifier("jiraTicketApprovalEscalationWorkFlow") WorkFlow projectAccessRequestEscalationWorkFlow,
			@Value("${SERVICE_URL:localhost}") String serviceUrl, @Value("${SERVICE_PORT:8080}") String servicePort,
			@Value("${MAIL_SERVICE_URL:service}") String mailServiceUrl,
			@Value("${MAIL_SERVICE_SITE_NAME_PROJECT:site}") String mailServiceSiteName) {
		return new ProjectAccessRequestApprovalWorkFlowCheckerTask(projectAccessRequestEscalationWorkFlow,
				new Date().getTime() / 1000 + 30, serviceUrl, servicePort, mailServiceUrl, mailServiceSiteName);
	}

	@Bean(name = "projectAccessRequestApprovalWorkFlowChecker")
	@Checker(cronExpression = "*/5 * * * * ?")
	WorkFlow projectAccessRequestApprovalWorkFlowChecker(
			@Qualifier("projectAccessRequestApprovalWorkFlowCheckerTask") ProjectAccessRequestApprovalWorkFlowCheckerTask projectAccessRequestApprovalWorkFlowCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("projectAccessRequestApprovalWorkFlowChecker")
				.execute(projectAccessRequestApprovalWorkFlowCheckerTask).build();
	}

	@Bean
	ProjectAccessRequestWorkFlowTask projectAccessRequestWorkFlowTask(
			@Value("${SERVICE_URL:localhost}") String serviceUrl, @Value("${SERVICE_PORT:8080}") String servicePort,
			@Value("${SERVICE_ACCOUNT_NAME:test}") String serviceAccountName,
			@Value("${SERVICE_ACCOUNT_PASSWORD:test}") String serviceAccountPassword) {
		return new ProjectAccessRequestWorkFlowTask(serviceUrl, servicePort, serviceAccountName,
				serviceAccountPassword);
	}

	@Bean
	ProjectAccessRequestApprovalWorkFlowTask projectAccessRequestApprovalWorkFlowTask(
			@Qualifier("projectAccessRequestApprovalWorkFlowChecker") WorkFlow projectAccessRequestApprovalWorkFlowChecker,
			@Value("${SERVICE_URL:localhost}") String serviceUrl, @Value("${SERVICE_PORT:8080}") String servicePort,
			@Value("${MAIL_SERVICE_URL:service}") String mailServiceUrl) {
		ProjectAccessRequestApprovalWorkFlowTask projectAccessRequestApprovalWorkFlowTask = new ProjectAccessRequestApprovalWorkFlowTask(
				serviceUrl, servicePort, mailServiceUrl);
		projectAccessRequestApprovalWorkFlowTask
				.setWorkFlowCheckers(List.of(projectAccessRequestApprovalWorkFlowChecker));
		return projectAccessRequestApprovalWorkFlowTask;
	}

	@Bean(name = "projectAccessRequestWorkFlow")
	@Infrastructure
	WorkFlow projectAccessRequestWorkFlow(
			@Qualifier("projectAccessRequestWorkFlowTask") ProjectAccessRequestWorkFlowTask projectAccessRequestWorkFlowTask,
			@Qualifier("projectAccessRequestApprovalWorkFlowTask") ProjectAccessRequestApprovalWorkFlowTask projectAccessRequestApprovalWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("projectAccessRequestWorkFlow")
				.execute(projectAccessRequestWorkFlowTask).then(projectAccessRequestApprovalWorkFlowTask).build();
	}

}
