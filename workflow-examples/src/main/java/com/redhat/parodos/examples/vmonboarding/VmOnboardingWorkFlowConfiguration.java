package com.redhat.parodos.examples.vmonboarding;

import java.util.Date;
import java.util.List;

import com.redhat.parodos.examples.vmonboarding.checker.AnsibleCompletionWorkFlowCheckerTask;
import com.redhat.parodos.examples.vmonboarding.checker.IpAddressProvisioningWorkFlowCheckerTask;
import com.redhat.parodos.examples.vmonboarding.escalation.ServiceNowTicketFulfillmentEscalationWorkFlowTask;
import com.redhat.parodos.examples.vmonboarding.task.NotificationWorkFlowTask;
import com.redhat.parodos.examples.vmonboarding.task.OnboardingVmAssessmentTask;
import com.redhat.parodos.examples.vmonboarding.task.ServiceNowTicketCreationWorkFlowTask;
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
public class VmOnboardingWorkFlowConfiguration {

	// WORKFLOW - Sequential Flow:
	// @formatter:off
    // (1)- ServiceNowTicketCreationWorkFlowTask
    //      (1.c1)- IpAddressProvisioningWorkFlowChecker
    //      		-> IpAddressProvisioningWorkFlowCheckerTask
    //      			-> ServiceNowTicketFulfillmentEscalationWorkFlow -> ServiceNowTicketFulfillmentEscalationWorkFlowTask
    //      (1.c2)- AnsibleCompletionWorkFlowChecker
    // 				-> AnsibleCompletionWorkFlowCheckerTask
    // (2)- NotificationWorkFlowTask
    // @formatter:on

	@Bean(name = "serviceNowTicketFulfillmentEscalationWorkFlowTask")
	ServiceNowTicketFulfillmentEscalationWorkFlowTask serviceNowTicketFulfillmentEscalationWorkFlowTask(
			@Value("${MAIL_SERVICE_URL:service}") String mailServiceUrl,
			@Value("${MAIL_SERVICE_SITE_NAME_ESCALATION:site}") String mailServiceSiteName) {
		return new ServiceNowTicketFulfillmentEscalationWorkFlowTask(mailServiceUrl, mailServiceSiteName);
	}

	@Bean(name = "serviceNowTicketFulfillmentEscalationWorkFlow")
	@Escalation
	WorkFlow serviceNowTicketFulfillmentEscalationWorkFlow(
			@Qualifier("serviceNowTicketFulfillmentEscalationWorkFlowTask") ServiceNowTicketFulfillmentEscalationWorkFlowTask serviceNowTicketFulfillmentEscalationWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("serviceNowTicketFulfillmentEscalationWorkFlow")
				.execute(serviceNowTicketFulfillmentEscalationWorkFlowTask).build();
	}

	@Bean(name = "ipAddressProvisioningWorkFlowCheckerTask")
	IpAddressProvisioningWorkFlowCheckerTask ipAddressProvisioningWorkFlowCheckerTask(
			@Qualifier("serviceNowTicketFulfillmentEscalationWorkFlow") WorkFlow serviceNowTicketFulfillmentEscalationWorkFlow,
			@Value("${AAP_URL:aap-url}") String aapUrl, @Value("${AAP_USER_NAME:aap-user}") String username,
			@Value("${AAP_PASSWORD:aap-password}") String password) {
		return new IpAddressProvisioningWorkFlowCheckerTask(serviceNowTicketFulfillmentEscalationWorkFlow,
				new Date().getTime() / 1000 + 30, aapUrl, username, password);
	}

	@Bean(name = "ipAddressProvisioningWorkFlowChecker")
	@Checker(cronExpression = "*/5 * * * * ?")
	WorkFlow ipAddressProvisioningWorkFlowChecker(
			@Qualifier("ipAddressProvisioningWorkFlowCheckerTask") IpAddressProvisioningWorkFlowCheckerTask ipAddressProvisioningWorkFlowCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("ipAddressProvisioningWorkFlowChecker")
				.execute(ipAddressProvisioningWorkFlowCheckerTask).build();
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
			@Qualifier("ipAddressProvisioningWorkFlowChecker") WorkFlow ipAddressProvisioningWorkFlowChecker,
			@Qualifier("ansibleCompletionWorkFlowChecker") WorkFlow ansibleCompletionWorkFlowChecker,
			@Value("${SERVICE_NOW_URL:service-now}") String serviceNowUrl,
			@Value("${SERVICE_NOW_USER_NAME:service-now-user}") String username,
			@Value("${SERVICE_NOW_PASSWORD:service-now-password}") String password) {
		ServiceNowTicketCreationWorkFlowTask serviceNowTicketCreationWorkFlowTask = new ServiceNowTicketCreationWorkFlowTask(
				serviceNowUrl, username, password);
		serviceNowTicketCreationWorkFlowTask
				.setWorkFlowCheckers(List.of(ipAddressProvisioningWorkFlowChecker, ansibleCompletionWorkFlowChecker));
		return serviceNowTicketCreationWorkFlowTask;
	}

	@Bean(name = "notificationIpAddressWorkFlowTask")
	NotificationWorkFlowTask notificationIpAddressWorkFlowTask(
			@Value("${NOTIFICATION_SERVER_URL:test}") String notificationServiceUrl) {
		return new NotificationWorkFlowTask(notificationServiceUrl, "IP Address");
	}

	@Bean(name = "notificationTomcatProvisioningWorkFlowTask")
	NotificationWorkFlowTask notificationTomcatProvisioningWorkFlowTask(
			@Value("${NOTIFICATION_SERVER_URL:test}") String notificationServiceUrl) {
		return new NotificationWorkFlowTask(notificationServiceUrl, "Tomcat Provisioning");
	}

	// VM ONBOARDING WORKFLOW - Sequential Flow:
	@Bean(name = "vmOnboardingWorkFlow")
	@Infrastructure(parameters = {
			@Parameter(key = "VM_TYPE", description = "VM type", type = WorkParameterType.SELECT, optional = false,
					selectOptions = { "RHEL", "Windows" }),
			@Parameter(key = "hostname", description = "The hostname", type = WorkParameterType.TEXT,
					optional = false) })
	WorkFlow vmOnboardingWorkFlow(
			@Qualifier("serviceNowTicketCreationWorkFlowTask") ServiceNowTicketCreationWorkFlowTask serviceNowTicketCreationWorkFlowTask,
			@Qualifier("notificationIpAddressWorkFlowTask") NotificationWorkFlowTask notificationIpAddressWorkFlowTask,
			@Qualifier("notificationTomcatProvisioningWorkFlowTask") NotificationWorkFlowTask notificationTomcatProvisioningWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("vmOnboardingWorkFlow")
				.execute(serviceNowTicketCreationWorkFlowTask)
				.then(notificationIpAddressWorkFlowTask)
				.then(notificationTomcatProvisioningWorkFlowTask)
				.build();
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
