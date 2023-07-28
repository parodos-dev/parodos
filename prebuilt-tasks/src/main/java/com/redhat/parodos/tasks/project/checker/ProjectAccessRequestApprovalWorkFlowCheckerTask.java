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

import com.redhat.parodos.infrastructure.ProjectRequester;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.AccessStatusResponseDTO;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.ACCESS_REQUEST_ID;

/**
 * Project access request approval workflow checker task
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class ProjectAccessRequestApprovalWorkFlowCheckerTask extends BaseWorkFlowCheckerTask {

	private final ProjectRequester projectRequester;

	public ProjectAccessRequestApprovalWorkFlowCheckerTask(WorkFlow projectAccessRequestApprovalEscalationWorkFlow,
			long sla, ProjectRequester projectRequester) {
		super(projectAccessRequestApprovalEscalationWorkFlow, sla);
		this.projectRequester = projectRequester;
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
			AccessStatusResponseDTO accessStatusResponseDTO = projectRequester.getAccessStatus(accessRequestId);
			switch (Objects.requireNonNull(accessStatusResponseDTO.getStatus())) {
				case REJECTED -> {
					log.info("Project access request {} is rejected!", accessStatusResponseDTO.getAccessRequestId());
					return new DefaultWorkReport(WorkStatus.REJECTED, workContext);
				}
				case APPROVED -> {
					log.info("Project access request {} is completed!", accessStatusResponseDTO.getAccessRequestId());
					return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
				}
				default -> log.info("Project access request {} awaits for approval",
						accessStatusResponseDTO.getAccessRequestId());
			}
		}
		catch (ApiException e) {
			log.error("There was an issue with the api call: {}", e.getMessage());
		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

}
