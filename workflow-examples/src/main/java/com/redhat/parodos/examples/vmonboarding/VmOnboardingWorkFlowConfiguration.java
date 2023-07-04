package com.redhat.parodos.examples.vmonboarding;

import java.util.List;

import com.redhat.parodos.examples.vmonboarding.checker.AnsibleCompletionWorkFlowCheckerTask;
import com.redhat.parodos.examples.vmonboarding.checker.ServiceNowTicketApprovalWorkFlowCheckerTask;
import com.redhat.parodos.examples.vmonboarding.task.AapLaunchJobWorkFlowTask;
import com.redhat.parodos.examples.vmonboarding.task.NotificationWorkFlowTask;
import com.redhat.parodos.examples.vmonboarding.task.ServiceNowTicketCreationWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.annotation.Parameter;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VmOnboardingWorkFlowConfiguration {

	// WORKFLOW - Sequential Flow:
	// @formatter:off
		// (1)- ServiceNowTicketCreationWorkFlowTask
		//      (1.c1)- ServiceNowTicketApprovalWorkFlowChecker
		//          -> ServiceNowTicketApprovalWorkFlowCheckerTask
		//
		// (2)- NotificationTicketApprovalWorkFlowTask

	    // (3)- AnsibleCompletionWorkFlowChecker
		//     (3.c1)- AnsibleCompletionWorkFlowChecker
		//             -> AnsibleCompletionWorkFlowCheckerTask
		//
	    // (4)- notificationVmCreatedWorkFlowTask
	// @formatter:on

	@Bean(name = "incidentWorkFlowCheckerTask")
	ServiceNowTicketApprovalWorkFlowCheckerTask incidentWorkFlowCheckerTask(
			@Value("${SERVICE_NOW_URL:service-now}") String serviceNowUrl,
			@Value("${SERVICE_NOW_USERNAME:service-now-username}") String username,
			@Value("${SERVICE_NOW_PASSWORD:service-now-password}") String password) {
		return new ServiceNowTicketApprovalWorkFlowCheckerTask(serviceNowUrl, username, password);
	}

	@Bean(name = "incidentWorkFlowChecker")
	@Checker(cronExpression = "*/10 * * * * ?")
	WorkFlow incidentWorkFlowChecker(
			@Qualifier("incidentWorkFlowCheckerTask") ServiceNowTicketApprovalWorkFlowCheckerTask serviceNowTicketApprovalWorkFlowCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("incidentWorkFlowChecker")
				.execute(serviceNowTicketApprovalWorkFlowCheckerTask).build();
	}

	@Bean(name = "ansibleCompletionWorkFlowCheckerTask")
	AnsibleCompletionWorkFlowCheckerTask ansibleCompletionWorkFlowCheckerTask(
			@Value("${AAP_URL:aap-url}") String aapUrl, @Value("${AAP_USER_NAME:aap-user}") String username,
			@Value("${AAP_PASSWORD:aap-password}") String password) {
		return new AnsibleCompletionWorkFlowCheckerTask(aapUrl, username, password);
	}

	@Bean(name = "ansibleCompletionWorkFlowChecker")
	@Checker(cronExpression = "*/10 * * * * ?")
	WorkFlow ansibleCompletionWorkFlowChecker(
			@Qualifier("ansibleCompletionWorkFlowCheckerTask") AnsibleCompletionWorkFlowCheckerTask ansibleCompletionWorkFlowCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("ansibleCompletionWorkFlowChecker")
				.execute(ansibleCompletionWorkFlowCheckerTask).build();
	}

	@Bean(name = "serviceNowTicketCreationWorkFlowTask")
	ServiceNowTicketCreationWorkFlowTask serviceNowTicketCreationWorkFlowTask(
			@Qualifier("incidentWorkFlowChecker") WorkFlow incidentWorkFlowChecker,
			@Value("${SERVICE_NOW_URL:service-now}") String serviceNowUrl,
			@Value("${SERVICE_NOW_USERNAME:service-now-user}") String username,
			@Value("${SERVICE_NOW_PASSWORD:service-now-password}") String password) {
		ServiceNowTicketCreationWorkFlowTask serviceNowTicketCreationWorkFlowTask = new ServiceNowTicketCreationWorkFlowTask(
				serviceNowUrl, username, password);
		serviceNowTicketCreationWorkFlowTask.setWorkFlowCheckers(List.of(incidentWorkFlowChecker));
		return serviceNowTicketCreationWorkFlowTask;
	}

	@Bean(name = "notificationTicketApprovalWorkFlowTask")
	NotificationWorkFlowTask notificationTicketApprovalWorkFlowTask() {
		return new NotificationWorkFlowTask("Ticket Approved");
	}

	@Bean(name = "notificationVmCreatedWorkFlowTask")
	NotificationWorkFlowTask notificationVmCreatedWorkFlowTask() {
		return new NotificationWorkFlowTask("VM Created");
	}

	@Bean(name = "aapLaunchJobWorkFlowTask")
	AapLaunchJobWorkFlowTask aapLaunchJobWorkFlowTask(
			@Qualifier("ansibleCompletionWorkFlowChecker") WorkFlow ansibleCompletionWorkFlowChecker,
			@Value("${AAP_URL:aap-url}") String aapUrl, @Value("${AAP_USER_NAME:aap-user}") String username,
			@Value("${AAP_PASSWORD:aap-password}") String password,
			@Value("${AAP_WINDOWS_JOB_ID:windows-job-id}") String windowsJobTemplateId,
			@Value("${AAP_RHEL_JOB_ID:rhel-job-id}") String rhelJobTemplateId) {
		AapLaunchJobWorkFlowTask aapLaunchJobWorkFlowTask = new AapLaunchJobWorkFlowTask(aapUrl, windowsJobTemplateId,
				rhelJobTemplateId, username, password);
		aapLaunchJobWorkFlowTask.setWorkFlowCheckers(List.of(ansibleCompletionWorkFlowChecker));
		return aapLaunchJobWorkFlowTask;
	}

	// VM ONBOARDING WORKFLOW - Sequential Flow:
	@Bean(name = "vmOnboardingWorkFlow")
	@Infrastructure(parameters = { @Parameter(key = "VM_TYPE", description = "VM type", type = WorkParameterType.SELECT,
			optional = false, selectOptions = { "RHEL", "Windows" }) })
	WorkFlow vmOnboardingWorkFlow(
			@Qualifier("serviceNowTicketCreationWorkFlowTask") ServiceNowTicketCreationWorkFlowTask serviceNowTicketCreationWorkFlowTask,
			@Qualifier("notificationTicketApprovalWorkFlowTask") NotificationWorkFlowTask notificationTicketApprovalWorkFlowTask,
			@Qualifier("aapLaunchJobWorkFlowTask") AapLaunchJobWorkFlowTask aapLaunchJobWorkFlowTask,
			@Qualifier("notificationVmCreatedWorkFlowTask") NotificationWorkFlowTask notificationVmCreatedWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("vmOnboardingWorkFlow")
				.execute(serviceNowTicketCreationWorkFlowTask).then(notificationTicketApprovalWorkFlowTask)
				.then(aapLaunchJobWorkFlowTask).then(notificationVmCreatedWorkFlowTask).build();
	}

	// Assessment workflow
	@Bean
	WorkFlowOption onboardingOcpOption() {
		return new WorkFlowOption.Builder("vmOnboarding", "vmOnboardingWorkFlow")
				.addToDetails("this is for creating vm").displayName("VM Onboarding")
				.setDescription("this is for creating vm").build();
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
	OnboardingVmAssessmentTask onboardingAssessmentTask(
			@Qualifier("onboardingOcpOption") WorkFlowOption onboardingOcpOption,
			@Qualifier("badRepoOption") WorkFlowOption badRepoOption,
			@Qualifier("notSupportOption") WorkFlowOption notSupportOption) {
		return new OnboardingVmAssessmentTask(List.of(onboardingOcpOption, badRepoOption, notSupportOption));
	}

	// A Workflow designed to execute and return WorkflowOption(s) that can be executed
	// next. In this case there is only one.
	@Bean(name = "onboardingAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
	@Assessment
	WorkFlow assessmentWorkFlow(
			@Qualifier("onboardingAssessmentTask") OnboardingVmAssessmentTask onboardingAssessmentTask) {
		// @formatter:off
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("onboardingAssessment" + WorkFlowConstants.ASSESSMENT_WORKFLOW)
				.execute(onboardingAssessmentTask)
				.build();
		// @formatter:on
	}
}
