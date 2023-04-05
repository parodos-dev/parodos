package com.redhat.parodos.examples.ocponboarding.task;

import com.redhat.parodos.examples.ocponboarding.task.dto.email.MessageRequestDTO;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import static java.util.Objects.isNull;

@Slf4j
public class JiraTicketEmailNotificationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private static final String MAIL_SERVER_URL = "https://mail-handler-svc-ihtetft2da-uc.a.run.app/submit";

	private static final String MAIL_SITE_NAME = "parodos-jira";

	private static final String ISSUE_LINK = "ISSUE_LINK";

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start JiraTicketEmailNotificationWorkFlowTask...");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = null;

		// requester name to extract securityContext or from workContext
		String requesterName = "John Doe";

		// requester email to extract securityContext or from workContext
		String requesterEmail = "jdoe@mail.com";

		// jira ticket url to extract from workContext
		String jiraTicketUrl;
		try {
			jiraTicketUrl = getRequiredParameterValue(workContext, ISSUE_LINK);
			log.info("Jira ticket url is: {}", jiraTicketUrl);
		}
		catch (MissingParameterException e) {
			log.error("JiraTicketEmailNotificationWorkFlowTask failed! Message: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}

		// message request payload
		MessageRequestDTO messageRequestDTO = getMessageRequestDTO(requesterName, requesterEmail, MAIL_SITE_NAME,
				getMessage(jiraTicketUrl));

		try {
			HttpEntity<MessageRequestDTO> request = new HttpEntity<>(messageRequestDTO);
			LocalDateTime startDateTime = LocalDateTime.now();
			responseEntity = restTemplate.exchange(MAIL_SERVER_URL, HttpMethod.POST, request, String.class);
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
	public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		return Collections.emptyList();
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.OTHER, WorkFlowTaskOutput.EXCEPTION);
	}

	private MessageRequestDTO getMessageRequestDTO(String requesterName, String requesterEmail, String siteName,
			String message) {
		MessageRequestDTO messageRequestDTO = new MessageRequestDTO();
		messageRequestDTO.setName(requesterName);
		messageRequestDTO.setEmail(requesterEmail);
		messageRequestDTO.setSiteName(siteName);
		messageRequestDTO.setMessage(message);
		return messageRequestDTO;
	}

	private String getMessage(String jiraTicketUrl) {
		return "Hi there," + "\n" + "Please review the jira ticket below and approve." + "\n" + "Jira ticket url: "
				+ jiraTicketUrl + "\n" + "Thank you," + "\n" + "The Parodos Team";
	}

}