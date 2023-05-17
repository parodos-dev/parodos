package com.redhat.parodos.examples.vmonboarding.task;

import com.redhat.parodos.examples.vmonboarding.dto.ServiceNowRequestDTO;
import com.redhat.parodos.examples.vmonboarding.dto.ServiceNowResponseDTO;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;

/**
 * An example of a task that create a serviceNow ticket
 *
 * @author Annel Ketcha (Github: anlukde)
 */

@Slf4j
public class ServiceNowTicketCreationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private final String serviceNowUrl;

	private final String username;

	private final String password;

	private static final String CREATE_INCIDENT_CONTEXT_PATH = "/api/now/table/incident";

	public ServiceNowTicketCreationWorkFlowTask(String serviceNowUrl, String username, String password) {
		this.serviceNowUrl = serviceNowUrl;
		this.username = username;
		this.password = password;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start ServiceNowTicketCreationWorkFlowTask...");
		try {
			String urlString = serviceNowUrl + CREATE_INCIDENT_CONTEXT_PATH;
			String vmName = getOptionalParameterValue("hostname", "snowrhel");
			String vmType = getRequiredParameterValue("VM_TYPE");
			log.info("vm name: {}", vmName);

			ServiceNowRequestDTO request = ServiceNowRequestDTO.builder().callerId(username)
					.shortDescription(String.format("Azure %s Vm Onboarding", vmType)).build();

			ResponseEntity<ServiceNowResponseDTO> response = RestUtils.executePost(urlString, request, username,
					password, ServiceNowResponseDTO.class);

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				String sysId = response.getBody().getResult().getSysId();
				String number = response.getBody().getResult().getNumber();
				log.info("Rest call completed, sys id: {}, incident number: {}", sysId, number);
				addParameter("INCIDENT_ID", sysId);
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
			}
			log.error("Call to the API was not successful. Response: {}", response.getStatusCode());
		}
		catch (Exception e) {
			log.error("There was an issue with the REST call: {}", e.getMessage());
		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

}