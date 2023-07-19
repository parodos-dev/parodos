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
package com.redhat.parodos.examples.ocponboarding.task;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * An example of a task that send a Jira ticket email notification
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class JiraTicketUpdateNotificationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private static final String ISSUE_LINK_PARAMETER_NAME = "ISSUE_LINK";

	private static final String NOTIFICATION_SUBJECT = "Jira ticket update notification";

	private final Notifier notifier;

	public JiraTicketUpdateNotificationWorkFlowTask(Notifier notifier) {
		super();
		this.notifier = notifier;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start jiraTicketUpdateNotificationWorkFlowTask...");
		String jiraTicketUrl;
		try {
			jiraTicketUrl = getRequiredParameterValue(ISSUE_LINK_PARAMETER_NAME);
		}
		catch (MissingParameterException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
		notifier.send(NOTIFICATION_SUBJECT, buildMessage(jiraTicketUrl));
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private String buildMessage(String jiraTicketUrl) {
		return "Hi there," + "\n" + "The jira ticket with updates of your request is provided below." + "\n"
				+ "Jira ticket url: " + jiraTicketUrl + "\n" + "Thank you," + "\n" + "The Parodos Team";
	}

}