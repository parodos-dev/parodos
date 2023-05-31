package com.redhat.parodos.examples.prebuilt;

import com.redhat.parodos.tasks.notification.NotificationWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflow.task.infrastructure.Notifier;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class PrebuiltWorkFlowConfiguration {

	@Bean
	NotificationWorkFlowTask notificationTask(Notifier notifier) {
		return new NotificationWorkFlowTask(notifier);
	}

	@Bean(name = "prebuiltWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow prebuiltSequentialWorkFlow(@Qualifier("notificationTask") NotificationWorkFlowTask notificationTask) {
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("prebuiltWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW).execute(notificationTask)
				.build();
	}

}
