package com.redhat.parodos.examples.vmonboarding.checker;

import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.examples.vmonboarding.dto.ServiceNowResponseDto;
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
public class IncidentWorkFlowCheckerTask extends BaseWorkFlowCheckerTask {

	private final String serviceNowUrl;

	private final String username;

	private final String password;

	public IncidentWorkFlowCheckerTask(String serviceNowUrl, String username, String password) {
		this.serviceNowUrl = serviceNowUrl;
		this.username = username;
		this.password = password;
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start IncidentWorkFlowCheckerTask...");
		try {
			String incidentId = getRequiredParameterValue(workContext, "INCIDENT_ID");
			log.info("INCIDENT id: {}", incidentId);
			String urlString = serviceNowUrl + "/api/now/table/incident/" + incidentId;

			ResponseEntity<ServiceNowResponseDto> result = RestUtils.restExchange(urlString, username, password,
					ServiceNowResponseDto.class);
			ServiceNowResponseDto responseDto = result.getBody();

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
				String ip = responseDto.getResult().getIp();
				if (ip != null && !ip.isEmpty()) {
					addParameter(workContext, "IP", ip);
					return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
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

}
