package com.redhat.parodos.examples.vmonboarding.checker;

import com.redhat.parodos.examples.vmonboarding.dto.AapGetJobResponseDTO;
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
public class AnsibleCompletionWorkFlowCheckerTask extends BaseWorkFlowCheckerTask {

	private final String aapUrl;

	private final String username;

	private final String password;

	public AnsibleCompletionWorkFlowCheckerTask(String aapUrl, String username, String password) {
		this.aapUrl = aapUrl;
		this.username = username;
		this.password = password;
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start AnsibleCompletionWorkFlowCheckerTask...");
		try {
			String jobId = getRequiredParameterValue("JOB_ID");
			log.info("job id: {}", jobId);
			String urlString = aapUrl + "/api/v2/jobs/" + jobId;

			ResponseEntity<AapGetJobResponseDTO> result = RestUtils.restExchange(
					RestUtils.ignoreSSLVerifyRestTemplate(), urlString, username, password, AapGetJobResponseDTO.class);
			AapGetJobResponseDTO responseDto = result.getBody();

			if (!result.getStatusCode().is2xxSuccessful() || responseDto == null) {
				log.error("Call to the API was not successful. Response: {} ", result.getStatusCode());
			}
			else if ("pending".equalsIgnoreCase(responseDto.getStatus())
					|| "running".equalsIgnoreCase(responseDto.getStatus())) {
				log.error("job is not completed.  Status: {}", responseDto.getStatus());
			}
			else if ("successful".equalsIgnoreCase(responseDto.getStatus())) {
				log.info("Rest call completed: {}", responseDto.getStatus());
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
			}
			else {
				log.error("job is failed.  Status: {}", responseDto.getStatus());
				return new DefaultWorkReport(WorkStatus.REJECTED, workContext);
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
