package com.redhat.parodos.tasks.ansible;

import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;

@Slf4j
public class AapLaunchJobWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private final String aapUrl;

	private final String username;

	private final String password;

	protected String jobTemplateId;

	protected static final String JOB_LAUNCH_CONTEXT_PATH = "/api/v2/job_templates/%s/launch/";

	public AapLaunchJobWorkFlowTask(String aapUrl, String username, String password) {
		this.aapUrl = aapUrl;
		this.username = username;
		this.password = password;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		ResponseEntity<AapGetJobResponseDTO> response = executeAapJob(jobTemplateId);
		if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			responseAction(response.getBody());
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		log.error("Call to the API was not successful. Response: {}", response.getStatusCode());

		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	protected ResponseEntity<AapGetJobResponseDTO> executeAapJob(String jobTemplateId) {
		String urlString = aapUrl + String.format(JOB_LAUNCH_CONTEXT_PATH, jobTemplateId);
		try {
			return RestUtils.executePost(RestUtils.ignoreSSLVerifyRestTemplate(), urlString, "{}", username, password,
					AapGetJobResponseDTO.class);
		}
		catch (Exception e) {
			log.error("There was an issue with the REST call: {}", e.getMessage());
		}
		return null;
	}

	protected void responseAction(AapGetJobResponseDTO responseDTO) {
	}

}
