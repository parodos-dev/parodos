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

import java.util.Objects;
import java.util.UUID;

import com.redhat.parodos.tasks.project.dto.MessageRequest;
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

import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.ACCESS_REQUEST_APPROVAL_USERS_EMAILS;
import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.ACCESS_REQUEST_ID;
import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.MAIL_SUBJECT_ACCESS_REQUEST_APPROVAL;
import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.PARAMETER_USERNAME;

/**
 * Project access request approval workflow task
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class ProjectAccessRequestApprovalWorkFlowTask extends BaseWorkFlowTask {

	private final String mailServiceUrl;

	private final String serviceUrl;

	private final String servicePort;

	public ProjectAccessRequestApprovalWorkFlowTask(String serviceUrl, String servicePort, String mailServiceUrl) {
		super();
		this.serviceUrl = serviceUrl;
		this.servicePort = servicePort;
		this.mailServiceUrl = mailServiceUrl;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start projectAccessRequestApprovalWorkFlowTask...");
		String username, approvalUsersEmails;
		UUID accessRequestId;
		try {
			username = getRequiredParameterValue(PARAMETER_USERNAME);
			accessRequestId = UUID.fromString(getRequiredParameterValue(ACCESS_REQUEST_ID));
			approvalUsersEmails = getRequiredParameterValue(ACCESS_REQUEST_APPROVAL_USERS_EMAILS);
			log.info("Project access request to project id: {}, username: {} to be sent to: {}",
					getProjectId(workContext), username, approvalUsersEmails);
		}
		catch (MissingParameterException e) {
			log.error("Exception when trying to get required parameter(s): {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		String approvalUrl = String.format("http://%s:%s/api/v1/projects/access/%s", serviceUrl, servicePort,
				accessRequestId);
		MessageRequest messageRequest = new MessageRequest(username, MAIL_SUBJECT_ACCESS_REQUEST_APPROVAL, approvalUsersEmails.split(","), getMessage(approvalUrl));
		ResponseEntity<String> responseEntity = null;
		try {
			HttpEntity<MessageRequest> requestEntity = new HttpEntity<>(messageRequest);
			responseEntity = RestUtils.executePost(mailServiceUrl, requestEntity);
		}
		catch (Exception e) {
			log.error("Error occurred when submitting message: {}", e.getMessage());
		}

		if (!Objects.isNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful()
				&& !Objects.isNull(responseEntity.getBody()) && responseEntity.getBody().contains("Mail Sent")) {
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	private String getMessage(String url) {
		return "Hi there," + "\n"
				+ "A project request awaits your approval. Use the url below to approve or reject the request." + "\n"
				+ "Url: " + url + "\n" + "Thank you," + "\n" + "The Parodos Team";
	}

}
