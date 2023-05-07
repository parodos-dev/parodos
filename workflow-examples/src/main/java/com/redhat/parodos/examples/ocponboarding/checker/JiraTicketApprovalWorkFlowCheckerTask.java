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
package com.redhat.parodos.examples.ocponboarding.checker;

import java.util.List;

import com.redhat.parodos.examples.ocponboarding.task.dto.jira.GetJiraTicketResponseDto;
import com.redhat.parodos.examples.ocponboarding.task.dto.jira.GetJiraTicketResponseValue;
import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

/**
 * An example of a task that calls a Jira Endpoint with a BasicAuth Header
 *
 * @author Richard Wang (Github: richardW98)
 */

@Slf4j
public class JiraTicketApprovalWorkFlowCheckerTask extends BaseWorkFlowCheckerTask {

	private static final String ISSUE_KEY = "ISSUE_KEY";

	private static final String CLUSTER_TOKEN = "CLUSTER_TOKEN";

	private static final String CLUSTER_TOKEN_CUSTOM_FIELD_ID = "customfield_10064";

	private static final String DONE = "DONE";

	private static final String DECLINED = "DECLINED";

	private final String jiraServiceBaseUrl;

	private final String jiraUsername;

	private final String jiraPassword;

	public JiraTicketApprovalWorkFlowCheckerTask(WorkFlow jiraTicketApprovalEscalationWorkFlow, long sla,
			String jiraServiceBaseUrl, String jiraUsername, String jiraPassword) {
		super(jiraTicketApprovalEscalationWorkFlow, sla);
		this.jiraServiceBaseUrl = jiraServiceBaseUrl;
		this.jiraUsername = jiraUsername;
		this.jiraPassword = jiraPassword;
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start jiraTicketApprovalWorkFlowCheckerTask...");
		try {
			String urlString = jiraServiceBaseUrl + "/rest/servicedeskapi/request/";
			String issueKey = getRequiredParameterValue(workContext, ISSUE_KEY);
			log.info("Calling: urlString: {} username: {}", urlString, jiraUsername);

			ResponseEntity<GetJiraTicketResponseDto> result = RestUtils.restExchange(urlString + issueKey, jiraUsername,
					jiraPassword, GetJiraTicketResponseDto.class);
			GetJiraTicketResponseDto responseDto = result.getBody();

			if (!result.getStatusCode().is2xxSuccessful() || responseDto == null) {
				log.error("Call to the API was not successful. Response: {}", result.getStatusCode());
			}
			else {
				log.info("Rest call completed: {}", responseDto.getIssueKey());
				switch (responseDto.getCurrentStatus().getStatus().toUpperCase()) {
					case DONE:
						log.info("request {} is approved", responseDto.getIssueKey());
						String clusterToken = responseDto.getRequestFieldValues().stream()
								.filter(requestFieldValue -> requestFieldValue.getFieldId()
										.equals(CLUSTER_TOKEN_CUSTOM_FIELD_ID))
								.findFirst().map(GetJiraTicketResponseValue::getValue)
								.orElseThrow(() -> new MissingParameterException(
										"cluster token is not provided by approver!"))
								.toString();
						addParameter(workContext, CLUSTER_TOKEN, clusterToken);
						return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
					case DECLINED:
						log.info("request {} is rejected", responseDto.getIssueKey());
						return new DefaultWorkReport(WorkStatus.REJECTED, workContext);
					default:
						log.info("request {} is waiting for approval", responseDto.getIssueKey());
						break;
				}
			}
		}
		catch (RestClientException e) {
			log.error("There was an issue with the REST call: {}", e.getMessage());
		}
		catch (MissingParameterException e) {
			log.error("There was an error getting parameter(s): {}", e.getMessage());
		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.HTTP2XX, WorkFlowTaskOutput.OTHER);
	}

}
