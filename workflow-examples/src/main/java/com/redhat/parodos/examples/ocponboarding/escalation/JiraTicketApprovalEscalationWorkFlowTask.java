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
package com.redhat.parodos.examples.ocponboarding.escalation;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import com.redhat.parodos.examples.ocponboarding.task.dto.email.MessageRequestDTO;
import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

/**
 * An example of a task that send an escalation email notification for a pending Jira
 * ticket review and approval
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class JiraTicketApprovalEscalationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private static final String ISSUE_LINK = "ISSUE_LINK";

	private final String mailServiceUrl;

	private final String mailServiceSiteName;

	public JiraTicketApprovalEscalationWorkFlowTask(String mailServiceUrl, String mailServiceSiteName) {
		super();
		this.mailServiceUrl = mailServiceUrl;
		this.mailServiceSiteName = mailServiceSiteName;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start jiraTicketApprovalEscalationWorkFlowTask...");

		// requester name to extract securityContext or from workContext
		String requesterName = "Test Test";

		// requester email to extract securityContext or from workContext
		String requesterEmail = "ttest@test.com";

		// jira ticket url to extract from workContext
		String jiraTicketUrl;
		try {
			jiraTicketUrl = getRequiredParameterValue(workContext, ISSUE_LINK);
			log.info("Jira ticket url is: {}", jiraTicketUrl);
		}
		catch (MissingParameterException e) {
			log.error("JiraTicketApprovalEscalationWorkFlowTask failed! Message: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}

		// message request payload
		MessageRequestDTO messageRequestDTO = MessageRequestDTO.builder().name(requesterName).email(requesterEmail)
				.siteName(mailServiceSiteName).message(getMessage(jiraTicketUrl)).build();

		ResponseEntity<String> responseEntity = null;
		try {
			HttpEntity<MessageRequestDTO> requestEntity = new HttpEntity<>(messageRequestDTO);
			LocalDateTime startDateTime = LocalDateTime.now();
			responseEntity = RestUtils.executePost(mailServiceUrl, requestEntity);
			log.info("Request duration: {} ms", ChronoUnit.MILLIS.between(startDateTime, LocalDateTime.now()));
		}
		catch (Exception e) {
			log.error("Error occurred when submitting message: {}", e.getMessage());
		}

		if (!Objects.isNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful()
				&& !Objects.isNull(responseEntity.getBody()) && responseEntity.getBody().contains("Mail Sent")) {
			log.info("JiraTicketApprovalEscalationWorkFlowTask completed!");
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		log.info("JiraTicketApprovalEscalationWorkFlowTask failed!");
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.EXCEPTION, WorkFlowTaskOutput.OTHER);
	}

	private String getMessage(String jiraTicketUrl) {
		return "Hi there," + "\n" + "The jira ticket below has been escalated due to pending review and approval."
				+ "\n" + "Jira ticket url: " + jiraTicketUrl + "\n" + "Thank you," + "\n" + "The Parodos Team";
	}

}
