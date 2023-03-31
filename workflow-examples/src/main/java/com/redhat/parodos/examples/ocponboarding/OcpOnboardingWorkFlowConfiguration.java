package com.redhat.parodos.examples.ocponboarding;

import com.redhat.parodos.examples.ocponboarding.checker.JiraTicketApprovalWorkFlowCheckerTask;
import com.redhat.parodos.examples.ocponboarding.task.AppLinkEmailNotificationWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.JiraTicketCreationWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.JiraTicketEmailNotificationWorkFlowTask;
import com.redhat.parodos.examples.ocponboarding.task.OcpAppDeploymentWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.annotation.Parameter;
import com.redhat.parodos.workflow.parameter.WorkFlowParameterType;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OcpOnboardingWorkFlowConfiguration {

	// WORKFLOW A - Sequential Flow:
	// - JiraTicketCreationWorkFlowTask -> JiraTicketApprovalWorkFlowCheckerTask
	// - JiraTicketEmailNotificationWorkFlowTask

	@Bean
	JiraTicketApprovalWorkFlowCheckerTask jiraTicketApprovalWorkFlowCheckerTask() {
		return new JiraTicketApprovalWorkFlowCheckerTask();
	}

	@Bean(name = "jiraTicketApprovalWorkFlowChecker")
	@Checker(cronExpression = "*/5 * * * * ?")
	WorkFlow jiraTicketApprovalWorkFlowChecker(
			@Qualifier("jiraTicketApprovalWorkFlowCheckerTask") JiraTicketApprovalWorkFlowCheckerTask jiraTicketApprovalWorkFlowCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("jiraTicketApprovalWorkFlowCheckerTask")
				.execute(jiraTicketApprovalWorkFlowCheckerTask).build();
	}

	@Bean
	JiraTicketCreationWorkFlowTask jiraTicketCreationWorkFlowTask(
			@Qualifier("jiraTicketApprovalWorkFlowChecker") WorkFlow jiraTicketApprovalWorkFlowChecker) {
		JiraTicketCreationWorkFlowTask jiraTicketCreationWorkFlowTask = new JiraTicketCreationWorkFlowTask();
		jiraTicketCreationWorkFlowTask.setWorkFlowCheckers(List.of(jiraTicketApprovalWorkFlowChecker));
		return jiraTicketCreationWorkFlowTask;
	}

	@Bean
	JiraTicketEmailNotificationWorkFlowTask jiraTicketEmailNotificationWorkFlowTask() {
		return new JiraTicketEmailNotificationWorkFlowTask();
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
	// - JiraTicketEmailNotificationWorkFlowTask
	@Bean
	OcpAppDeploymentWorkFlowTask ocpAppDeploymentWorkFlowTask() {
		return new OcpAppDeploymentWorkFlowTask();
	}

	@Bean
	AppLinkEmailNotificationWorkFlowTask appLinkEmailNotificationWorkFlowTask() {
		return new AppLinkEmailNotificationWorkFlowTask();
	}

	@Bean(name = "workFlowB")
	@Infrastructure
	WorkFlow workFlowB(
			@Qualifier("ocpAppDeploymentWorkFlowTask") OcpAppDeploymentWorkFlowTask ocpAppDeploymentWorkFlowTask,
			@Qualifier("appLinkEmailNotificationWorkFlowTask") AppLinkEmailNotificationWorkFlowTask appLinkEmailNotificationWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("workFlowB").execute(ocpAppDeploymentWorkFlowTask)
				.then(appLinkEmailNotificationWorkFlowTask).build();
	}

	// OCP ONBOARDING WORKFLOW - Sequential Flow:
	// - workFlowA
	// - workFlowB

	@Bean(name = "ocpOnboardingWorkFlow")
	@Infrastructure(parameters = { @Parameter(key = "namespace", description = "The namespace in the ocp cluster",
			type = WorkFlowParameterType.TEXT, optional = false) })
	WorkFlow ocpOnboardingWorkFlow(@Qualifier("workFlowA") WorkFlow workFlowA,
			@Qualifier("workFlowB") WorkFlow workFlowB) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("ocpOnboardingWorkFlow").execute(workFlowA)
				.then(workFlowB).build();
	}

}
