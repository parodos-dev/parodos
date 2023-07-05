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
package com.redhat.parodos.tasks.project.escalation;

import java.util.List;
import java.util.UUID;

import com.redhat.parodos.tasks.project.dto.NotificationRequest;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.ACCESS_REQUEST_ESCALATION_USERNAME;
import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.ACCESS_REQUEST_ID;
import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.NOTIFICATION_SUBJECT_ACCESS_REQUEST_ESCALATION;

/**
 * Project access request escalation workflow task
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class ProjectAccessRequestEscalationWorkFlowTask extends BaseWorkFlowTask {

	private final String serviceUrl;

	private final String servicePort;

	private final String notificationServiceUrl;

	private final String notificationServicePort;

	private final String notificationServiceAccountName;

	private final String notificationServiceAccountPassword;

	public ProjectAccessRequestEscalationWorkFlowTask(String serviceUrl, String servicePort,
			String notificationServiceUrl, String notificationServicePort, String notificationServiceAccountName,
			String notificationServiceAccountPassword) {
		super();
		this.serviceUrl = serviceUrl;
		this.servicePort = servicePort;
		this.notificationServiceUrl = notificationServiceUrl;
		this.notificationServicePort = notificationServicePort;
		this.notificationServiceAccountName = notificationServiceAccountName;
		this.notificationServiceAccountPassword = notificationServiceAccountPassword;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start projectAccessRequestEscalationWorkFlowTask...");
		String escalationUsername;
		UUID accessRequestId;
		try {
			accessRequestId = UUID.fromString(getRequiredParameterValue(ACCESS_REQUEST_ID));
			escalationUsername = getRequiredParameterValue(ACCESS_REQUEST_ESCALATION_USERNAME);
		}
		catch (MissingParameterException e) {
			log.error("Exception when trying to get required parameter(s): {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		String projectAccessRequestStatusUrl = String.format("%s:%s/api/v1/projects/access/%s/status", serviceUrl,
				servicePort, accessRequestId);
		NotificationRequest request = NotificationRequest.builder().usernames(List.of(escalationUsername))
				.subject(NOTIFICATION_SUBJECT_ACCESS_REQUEST_ESCALATION).body(getMessage(projectAccessRequestStatusUrl))
				.build();
		HttpEntity<NotificationRequest> notificationRequestHttpEntity = RestUtils.getRequestWithHeaders(request,
				notificationServiceAccountName, notificationServiceAccountPassword);

		ResponseEntity<String> response = RestUtils.executePost(
				String.format("%s:%s/api/v1/messages", notificationServiceUrl, notificationServicePort),
				notificationRequestHttpEntity);
		try {
			if (response.getStatusCode().is2xxSuccessful()) {
				log.info("Rest call completed: {}", response.getBody());
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
			}
			log.error("Call to the API was not successful. Response: {}", response.getStatusCode());
		}
		catch (Exception e) {
			log.error("There was an issue with the REST call: {}", e.getMessage());
		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	private String getMessage(String url) {
		return "Hi there," + "\n" + "A project request below has been escalated as being pending for a while." + "\n"
				+ "Url: " + url + "\n" + "Thank you," + "\n" + "The Parodos Team";
	}

}
