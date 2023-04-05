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

import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;
import com.redhat.parodos.examples.ocponboarding.dto.CreateJiraTicketRequestDto;
import com.redhat.parodos.examples.ocponboarding.dto.RequestFieldValues;
import com.redhat.parodos.examples.ocponboarding.dto.CreateJiraTicketResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

/**
 * An example of a task that calls a Jira Endpoint with a BasicAuth Header
 *
 * @author Richard Wang (Github: richardW98)
 */

@Slf4j
public class JiraTicketCreationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private static final String NAMESPACE = "NAMESPACE";

	private static final String ISSUE_KEY = "ISSUE_KEY";

	private static final String ISSUE_LINK = "ISSUE_LINK";

	private static final String WEB_LINK = "web";

	private final String url;

	private final String username;

	private final String password;

	private final String approverId;

	public JiraTicketCreationWorkFlowTask(String url, String username, String password, String approverId) {
		super();
		this.url = url;
		this.username = username;
		this.password = password;
		this.approverId = approverId;
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	public WorkReport execute(WorkContext workContext) {
		try {
			String urlString = url + "/rest/servicedeskapi/request";
			String serviceDeskId = "1";
			String requestTypeId = "35";
			String projectId = getProjectId(workContext);
			String namespace = getOptionalParameterValue(workContext, NAMESPACE, "demo");
			log.info("Calling: urlString: {} username: {}", urlString, username);

			CreateJiraTicketRequestDto request = CreateJiraTicketRequestDto.builder().serviceDeskId(serviceDeskId)
					.requestTypeId(requestTypeId)
					.requestFieldValues(RequestFieldValues.builder()
							.approvers(List.of(RequestFieldValues.JiraUser.builder().accountId(approverId).build()))
							.summary(String.format("Onboard %s in namespace %s on ocp", projectId, namespace))
							.projectName(projectId).namespace(namespace).build())
					.build();

			ResponseEntity<CreateJiraTicketResponseDto> response = RestUtils.executePost(urlString, request, username,
					password, CreateJiraTicketResponseDto.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				log.info("Rest call completed: {}", Objects.requireNonNull(response.getBody()).getIssueId());
				addParameter(workContext, ISSUE_KEY, Objects.requireNonNull(response.getBody()).getIssueKey());
				addParameter(workContext, ISSUE_LINK,
						Objects.requireNonNull(response.getBody()).getLinks().get(WEB_LINK));
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
			}
			log.error("Call to the API was not successful. Response: {}", response.getStatusCode());
		}
		catch (Exception e) {
			log.error("There was an issue with the REST call: {}", e.getMessage());

		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.HTTP2XX, WorkFlowTaskOutput.OTHER);
	}

}
