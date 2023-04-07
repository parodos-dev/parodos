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

import static java.util.Objects.isNull;

import com.redhat.parodos.examples.ocponboarding.task.dto.email.MessageRequestDTO;
import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

/**
 * An example of a task that send a Jira ticket email notification
 *
 * @author Annel Ketch (Github: anludke)
 */

@Slf4j
public class JiraTicketEmailNotificationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private static final String MAIL_RECIPIENT_SITE_NAME = "parodos-jira";

	private static final String ISSUE_LINK_PARAMETER_NAME = "ISSUE_LINK";

	private final String mailServiceUrl;

	public JiraTicketEmailNotificationWorkFlowTask(String mailServiceUrl) {
		super();
		this.mailServiceUrl = mailServiceUrl;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start jiraTicketEmailNotificationWorkFlowTask...");

		// requester name to extract securityContext or from workContext
		String requesterName = "Test Test";

		// requester email to extract securityContext or from workContext
		String requesterEmail = "ttest@test.com";

		// jira ticket url to extract from workContext
		String jiraTicketUrl;
		try {
			jiraTicketUrl = getRequiredParameterValue(workContext, ISSUE_LINK_PARAMETER_NAME);
			log.info("Jira ticket url is: {}", jiraTicketUrl);
		}
		catch (MissingParameterException e) {
			log.error("JiraTicketEmailNotificationWorkFlowTask failed! Message: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}

		// message request payload
		MessageRequestDTO messageRequestDTO = new MessageRequestDTO(requesterName, requesterEmail,
				MAIL_RECIPIENT_SITE_NAME, getMessage(jiraTicketUrl));

		ResponseEntity<String> responseEntity = null;
		try {
			HttpEntity<MessageRequestDTO> requestEntity = new HttpEntity<>(messageRequestDTO);
			LocalDateTime startDateTime = LocalDateTime.now();
			responseEntity = RestUtils.executePost(mailServiceUrl, requestEntity);
			log.info("Request duration: {} ms", ChronoUnit.MILLIS.between(startDateTime, LocalDateTime.now()));
		}
		catch (Exception e) {
			e.printStackTrace();
			log.error("Error occurred when submitting message: {}", e.getMessage());
		}

		if (!isNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful()
				&& !isNull(responseEntity.getBody()) && responseEntity.getBody().contains("Mail Sent")) {
			log.info("JiraTicketEmailNotificationWorkFlowTask completed!");
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}

		log.info("JiraTicketEmailNotificationWorkFlowTask failed!");
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.EXCEPTION, WorkFlowTaskOutput.OTHER);
	}

	private String getMessage(String jiraTicketUrl) {
		return "Hi there," + "\n" + "Please review the jira ticket below and approve." + "\n" + "Jira ticket url: "
				+ jiraTicketUrl + "\n" + "Thank you," + "\n" + "The Parodos Team";
	}

}