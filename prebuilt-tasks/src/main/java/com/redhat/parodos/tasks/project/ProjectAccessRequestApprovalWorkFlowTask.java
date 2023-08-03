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
package com.redhat.parodos.tasks.project;

import java.util.Arrays;
import java.util.UUID;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.notification.sdk.model.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * Project access request approval workflow task
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class ProjectAccessRequestApprovalWorkFlowTask extends BaseWorkFlowTask {

	private static final String ACCESS_REQUEST_ID = "ACCESS_REQUEST_ID";

	private static final String ACCESS_REQUEST_APPROVAL_USERNAMES = "ACCESS_REQUEST_APPROVAL_USERNAMES";

	private static final String NOTIFICATION_SUBJECT_ACCESS_REQUEST_APPROVAL = "Project Access Request Approval";

	private final String serviceUrl;

	private final Notifier notifier;

	public ProjectAccessRequestApprovalWorkFlowTask(String serviceUrl, Notifier notifier) {
		super();
		this.serviceUrl = serviceUrl;
		this.notifier = notifier;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start projectAccessRequestApprovalWorkFlowTask...");
		UUID accessRequestId;
		String approvalUsernames;
		try {
			accessRequestId = UUID.fromString(getRequiredParameterValue(ACCESS_REQUEST_ID));
			approvalUsernames = getRequiredParameterValue(ACCESS_REQUEST_APPROVAL_USERNAMES);
		}
		catch (MissingParameterException e) {
			log.error("Exception when trying to get required parameter(s): {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO = new NotificationMessageCreateRequestDTO();
		notificationMessageCreateRequestDTO.setSubject(NOTIFICATION_SUBJECT_ACCESS_REQUEST_APPROVAL);
		notificationMessageCreateRequestDTO.setUsernames(Arrays.stream(approvalUsernames.split(",")).toList());
		notificationMessageCreateRequestDTO
				.setBody(getMessage("%s/api/v1/projects/access/%s".formatted(serviceUrl, accessRequestId)));
		notifier.send(notificationMessageCreateRequestDTO);
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private String getMessage(String url) {
		return "Hi there," + "\n"
				+ "A project request awaits your approval. Use the url below to approve or reject the request." + "\n"
				+ "Url: " + url + "\n" + "Thank you," + "\n" + "The Parodos Team";
	}

}
