package com.redhat.parodos.examples.prebuilt;

import com.redhat.parodos.tasks.notification.NotificationWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.utils.CredUtils;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrebuiltWorkFlowConfiguration {

	@Bean
	NotificationWorkFlowTask notificationTask() {
		return new NotificationWorkFlowTask("http://localhost:8081",
				"Basic " + CredUtils.getBase64Creds("test", "test"));
	}

	@Bean(name = "prebuiltWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow prebuiltSequentialWorkFlow(@Qualifier("notificationTask") NotificationWorkFlowTask notificationTask) {
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("prebuiltWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW).execute(notificationTask)
				.build();
	}

}
