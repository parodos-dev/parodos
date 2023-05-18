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

import java.util.List;

import com.redhat.parodos.examples.ocponboarding.task.dto.notification.NotificationRequest;
import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

/**
 * send message to notification service
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class NotificationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private static final String NOTIFICATION_SUBJECT_PREF = "VM Demo";

	private static final String NOTIFICATION_USERNAME = "test";

	private static final String NOTIFICATION_PASSWORD = "test";

	private final String notificationServiceUrl;

	private final String notificationSubject;

	public NotificationWorkFlowTask(String notificationServiceUrl, String notificationSubject) {
		super();
		this.notificationServiceUrl = notificationServiceUrl;
		this.notificationSubject = notificationSubject;
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	public WorkReport execute(WorkContext workContext) {
		try {
			NotificationRequest request;
			if (notificationSubject.equalsIgnoreCase("IP Address")) { // IP Address
				String ip = getRequiredParameterValue(workContext, "IP");
				request = NotificationRequest.builder().usernames(List.of(NOTIFICATION_USERNAME))
						.subject(NOTIFICATION_SUBJECT_PREF + notificationSubject).body(buildMessage(ip)).build();
			} else { // Tomcat Provisioning
				request = NotificationRequest.builder().usernames(List.of(NOTIFICATION_USERNAME))
						.subject(NOTIFICATION_SUBJECT_PREF + notificationSubject).body(buildMessage(buildMessage())).build();
			}
			HttpEntity<NotificationRequest> notificationRequestHttpEntity = RestUtils.getRequestWithHeaders(request,
					NOTIFICATION_USERNAME, NOTIFICATION_PASSWORD);

			ResponseEntity<String> response = RestUtils.executePost(notificationServiceUrl + "/api/v1/messages",
					notificationRequestHttpEntity);

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

	private String buildMessage(String ip) {
		return "Ip address: " + ip;
	}

	private String buildMessage() {
		return "Tomcat installed to your vm";
	}

}
