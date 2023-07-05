package com.redhat.parodos.examples.vmonboarding.checker;

import com.redhat.parodos.examples.vmonboarding.dto.ServiceNowResponseDTO;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

/**
 * An example of a task that check for ansible completion
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class ServiceNowTicketApprovalWorkFlowCheckerTask extends BaseWorkFlowCheckerTask {

	private final String serviceNowUrl;

	private final String username;

	private final String password;

	public ServiceNowTicketApprovalWorkFlowCheckerTask(String serviceNowUrl, String username, String password) {
		this.serviceNowUrl = serviceNowUrl;
		this.username = username;
		this.password = password;
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start ServiceNowTicketApprovalWorkFlowCheckerTask...");
		try {
			String incidentId = getRequiredParameterValue("INCIDENT_ID");
			log.info("INCIDENT id: {}", incidentId);
			String urlString = serviceNowUrl + "/api/now/table/incident/" + incidentId;

			ResponseEntity<ServiceNowResponseDTO> result = RestUtils.restExchange(urlString, username, password,
					ServiceNowResponseDTO.class);
			ServiceNowResponseDTO responseDto = result.getBody();

			if (!result.getStatusCode().is2xxSuccessful() || responseDto == null) {
				log.error("Call to the API was not successful. Response: {} ", result.getStatusCode());
			}
			else if ("1".equalsIgnoreCase(responseDto.getResult().getState())) {
				log.error("incident is not approved.  Status: {}", responseDto.getResult().getState());
			}
			else if ("8".equalsIgnoreCase(responseDto.getResult().getState())) {
				log.error("incident is failed.  Status: {}", responseDto.getResult().getState());
				return new DefaultWorkReport(WorkStatus.REJECTED, workContext);
			}
			else {
				log.info("incident completed: {}", responseDto.getResult().getState());
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
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

}
