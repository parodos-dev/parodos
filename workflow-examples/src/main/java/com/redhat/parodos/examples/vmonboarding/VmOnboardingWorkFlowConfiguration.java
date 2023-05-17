package com.redhat.parodos.examples.vmonboarding;

import java.util.Date;
import java.util.List;

import com.redhat.parodos.examples.ocponboarding.task.NotificationWorkFlowTask;
import com.redhat.parodos.examples.vmonboarding.checker.AnsibleCompletionWorkFlowCheckerTask;
import com.redhat.parodos.examples.vmonboarding.checker.IpAddressProvisioningWorkFlowCheckerTask;
import com.redhat.parodos.examples.vmonboarding.escalation.ServiceNowTicketFulfillmentEscalationWorkFlowTask;
import com.redhat.parodos.examples.vmonboarding.task.ServiceNowTicketCreationWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Escalation;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.annotation.Parameter;
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
			@Qualifier("serviceNowTicketFulfillmentEscalationWorkFlow") WorkFlow serviceNowTicketFulfillmentEscalationWorkFlow) {
		return new IpAddressProvisioningWorkFlowCheckerTask(serviceNowTicketFulfillmentEscalationWorkFlow,
				new Date().getTime() / 1000 + 30);
	}

	@Bean(name = "ipAddressProvisioningWorkFlowChecker")
	@Checker(cronExpression = "*/5 * * * * ?")
	WorkFlow ipAddressProvisioningWorkFlowChecker(
			@Qualifier("ipAddressProvisioningWorkFlowCheckerTask") IpAddressProvisioningWorkFlowCheckerTask ipAddressProvisioningWorkFlowCheckerTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("ipAddressProvisioningWorkFlowChecker")
				.execute(ipAddressProvisioningWorkFlowCheckerTask).build();
	}

	@Bean(name = "ansibleCompletionWorkFlowCheckerTask")
	AnsibleCompletionWorkFlowCheckerTask ansibleCompletionWorkFlowCheckerTask() {
		return new AnsibleCompletionWorkFlowCheckerTask();
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
			@Qualifier("ansibleCompletionWorkFlowChecker") WorkFlow ansibleCompletionWorkFlowChecker) {
		ServiceNowTicketCreationWorkFlowTask serviceNowTicketCreationWorkFlowTask = new ServiceNowTicketCreationWorkFlowTask();
		serviceNowTicketCreationWorkFlowTask
				.setWorkFlowCheckers(List.of(ipAddressProvisioningWorkFlowChecker, ansibleCompletionWorkFlowChecker));
		return serviceNowTicketCreationWorkFlowTask;
	}

	@Bean(name = "notificationWorkFlowTask")
	NotificationWorkFlowTask notificationWorkFlowTask(
			@Value("${NOTIFICATION_SERVER_URL:test}") String notificationServiceUrl) {
		return new NotificationWorkFlowTask(notificationServiceUrl);
	}

	// VM ONBOARDING WORKFLOW - Sequential Flow:
	@Bean(name = "vmOnboardingWorkFlow")
	@Infrastructure(parameters = { @Parameter(key = "hostname", description = "The hostname",
			type = WorkParameterType.TEXT, optional = false) })
	WorkFlow vmOnboardingWorkFlow(
			@Qualifier("serviceNowTicketCreationWorkFlowTask") ServiceNowTicketCreationWorkFlowTask serviceNowTicketCreationWorkFlowTask,
			@Qualifier("notificationWorkFlowTask") NotificationWorkFlowTask notificationWorkFlowTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("vmOnboardingWorkFlow")
				.execute(serviceNowTicketCreationWorkFlowTask).then(notificationWorkFlowTask).build();
	}

}
