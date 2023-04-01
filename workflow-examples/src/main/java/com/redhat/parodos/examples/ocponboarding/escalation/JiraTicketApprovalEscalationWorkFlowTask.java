package com.redhat.parodos.examples.ocponboarding.escalation;

import com.redhat.parodos.examples.ocponboarding.task.dto.email.MessageRequestDTO;
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

import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
public class JiraTicketApprovalEscalationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	final String MAIL_SERVER_URL = "https://mail-handler-svc-ihtetft2da-uc.a.run.app/submit";

	final String SITE_NAME = "parodos-escaltion";

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start JiraTicketApprovalEscalationWorkFlowTask...");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = null;

		// requester name to extract securityContext or from workContext
		String requesterName = "John Doe";

		// requester email to extract securityContext or from workContext
		String requesterEmail = "jdoe@mail.com";

		// jira ticket url to extract from workContext
		String jiraTicketUrl = "https://parodos.atlassian.net/abc-xyz";

		// message request payload
		MessageRequestDTO messageRequestDTO = getMessageRequestDTO(requesterName, requesterEmail, SITE_NAME,
				getMessage(jiraTicketUrl));

		try {
			HttpEntity<MessageRequestDTO> request = new HttpEntity<>(messageRequestDTO);
			responseEntity = restTemplate.exchange(MAIL_SERVER_URL, HttpMethod.POST, request, String.class);
		}
		catch (Exception e) {
			log.error("Error occurred when submitting message: {}", e.getMessage());
		}

		if (!isNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful()
				&& !isNull(responseEntity.getBody()) && responseEntity.getBody().contains("Mail Sent")) {
			log.info("JiraTicketApprovalEscalationWorkFlowTask completed!");
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		log.info("JiraTicketApprovalEscalationWorkFlowTask failed!");
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
		return "Hi there," + "\n" + "Please review the jira ticket below and approve." + "\n"
				+ "Jira ticket url: <a href=\"" + jiraTicketUrl + "\">" + jiraTicketUrl + "</a>" + "\n" + "Thank you,"
				+ "\n" + "The Parodos Team";
	}

}
