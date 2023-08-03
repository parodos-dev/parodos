package com.redhat.parodos.examples.prebuilt.project;

import java.util.Date;
import java.util.List;

import com.redhat.parodos.infrastructure.Notifier;
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
			@Value("${SERVICE_URL:http://localhost:8080}") String serviceUrl, Notifier notifier) {
		return new ProjectAccessRequestEscalationWorkFlowTask(serviceUrl, notifier);
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
			@Qualifier("projectAccessRequestEscalationWorkFlow") WorkFlow projectAccessRequestEscalationWorkFlow,
			@Value("${SERVICE_URL:http://localhost:8080}") String serviceUrl,
			@Value("${SERVICE_USERNAME:test}") String serviceUsername,
			@Value("${SERVICE_PASSWORD:test}") String servicePassword) {
		return new ProjectAccessRequestApprovalWorkFlowCheckerTask(projectAccessRequestEscalationWorkFlow,
				new Date().getTime() / 1000 + 30, serviceUrl, serviceUsername, servicePassword);
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
			@Value("${SERVICE_URL:http://localhost:8080}") String serviceUrl,
			@Value("${SERVICE_USERNAME:test}") String serviceUsername,
			@Value("${SERVICE_PASSWORD:test}") String servicePassword) {
		return new ProjectAccessRequestWorkFlowTask(serviceUrl, serviceUsername, servicePassword);
	}

	@Bean
	ProjectAccessRequestApprovalWorkFlowTask projectAccessRequestApprovalWorkFlowTask(
			@Value("${SERVICE_URL:http://localhost:8080}") String serviceUrl, Notifier notifier,
			@Qualifier("projectAccessRequestApprovalWorkFlowChecker") WorkFlow projectAccessRequestApprovalWorkFlowChecker) {
		ProjectAccessRequestApprovalWorkFlowTask projectAccessRequestApprovalWorkFlowTask = new ProjectAccessRequestApprovalWorkFlowTask(
				serviceUrl, notifier);
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
