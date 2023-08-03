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
package com.redhat.parodos.tasks.project.checker;

import java.util.Objects;
import java.util.UUID;

import com.redhat.parodos.tasks.project.dto.AccessStatusResponseDTO;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;

/**
 * Project access request approval workflow checker task
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class ProjectAccessRequestApprovalWorkFlowCheckerTask extends BaseWorkFlowCheckerTask {

	private static final String ACCESS_REQUEST_ID = "ACCESS_REQUEST_ID";

	private final String serviceUrl;

	private final String serviceUsername;

	private final String servicePassword;

	public ProjectAccessRequestApprovalWorkFlowCheckerTask(WorkFlow projectAccessRequestApprovalEscalationWorkFlow,
			long sla, String serviceUrl, String serviceUsername, String servicePassword) {
		super(projectAccessRequestApprovalEscalationWorkFlow, sla);
		this.serviceUrl = serviceUrl;
		this.serviceUsername = serviceUsername;
		this.servicePassword = servicePassword;
	}

	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start projectAccessRequestApprovalWorkFlowCheckerTask...");
		UUID accessRequestId;
		try {
			accessRequestId = UUID.fromString(getRequiredParameterValue(ACCESS_REQUEST_ID));
		}
		catch (MissingParameterException e) {
			log.error("Exception when trying to get required parameter: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		try {
			String urlString = "%s/api/v1/projects/access/%s/status".formatted(serviceUrl, accessRequestId);
			ResponseEntity<AccessStatusResponseDTO> responseDTO = RestUtils.restExchange(urlString, serviceUsername,
					servicePassword, AccessStatusResponseDTO.class);
			if (!responseDTO.getStatusCode().is2xxSuccessful()) {
				log.error("Call to the api was not successful: {}", responseDTO.getStatusCode());
			}
			else {
				log.info("Rest call completed with response: {}", responseDTO.getBody());
				switch (Objects.requireNonNull(responseDTO.getBody()).getStatus()) {
					case APPROVED -> {
						log.info("Project access request {} is approved", responseDTO.getBody().getAccessRequestId());
						return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
					}
					case REJECTED -> {
						log.info("Project access request {} is rejected", responseDTO.getBody().getAccessRequestId());
						return new DefaultWorkReport(WorkStatus.REJECTED, workContext);
					}
					default -> log.info("Project access request {} is waiting for approval",
							responseDTO.getBody().getAccessRequestId());
				}
			}
		}
		catch (Exception e) {
			log.error("There was an issue with the REST call: {}", e.getMessage());
		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

}
