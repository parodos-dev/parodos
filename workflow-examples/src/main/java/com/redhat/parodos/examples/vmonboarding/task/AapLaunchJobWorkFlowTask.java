package com.redhat.parodos.examples.vmonboarding.task;

import java.util.Map;

import com.redhat.parodos.examples.vmonboarding.dto.AapGetJobResponseDto;
import com.redhat.parodos.examples.vmonboarding.dto.AapJobLaunchRequestDto;
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

	private final String jobTemplateId;

	private static final String JOB_LAUNCH_CONTEXT_PATH = "/api/v2/job_templates/%s/launch/";

	public AapLaunchJobWorkFlowTask(String aapUrl, String jobTemplateId, String username, String password) {
		this.aapUrl = aapUrl;
		this.jobTemplateId = jobTemplateId;
		this.username = username;
		this.password = password;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start ServiceNowTicketCreationWorkFlowTask...");
		try {
			String urlString = aapUrl + String.format(JOB_LAUNCH_CONTEXT_PATH, jobTemplateId);
			String vmIp = getRequiredParameterValue("IP");

			log.info("vm ip: {}", vmIp);

			AapJobLaunchRequestDto request = AapJobLaunchRequestDto.builder().limit(vmIp)
					.extraVars(Map.of("my_var", "my_answer")).build();

			ResponseEntity<AapGetJobResponseDto> response = RestUtils.executePost(urlString, request, username,
					password, AapGetJobResponseDto.class);

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				String jobId = response.getBody().getJobId();
				log.info("Rest call completed, job id: {}", jobId);
				addParameter("JOB_ID", jobId);
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
