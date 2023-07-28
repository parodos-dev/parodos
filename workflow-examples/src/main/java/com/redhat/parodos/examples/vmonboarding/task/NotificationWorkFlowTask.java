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
package com.redhat.parodos.examples.vmonboarding.task;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * send message to notification service
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class NotificationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private final Notifier notifier;

	private final String subject;

	public NotificationWorkFlowTask(Notifier notifier, String subject) {
		super();
		this.notifier = notifier;
		this.subject = subject;
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	public WorkReport execute(WorkContext workContext) {
		notifier.send(subject, buildMessage(subject));
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private String buildMessage(String subject) {
		String message = getOptionalParameterValue("NOTIFICATION_MESSAGE", "");
		taskLogger.logInfoWithSlf4j(message);
		return "Task %s completed with success. %n".formatted(subject) + message;
	}

}
