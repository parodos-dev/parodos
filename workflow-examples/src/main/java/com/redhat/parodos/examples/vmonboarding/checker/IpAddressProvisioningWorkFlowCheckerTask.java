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
package com.redhat.parodos.examples.vmonboarding.checker;

import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.examples.vmonboarding.dto.AapGetJobResponseDto;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

/**
 * An example of a task that checks for IP address in a ticket
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class IpAddressProvisioningWorkFlowCheckerTask extends BaseWorkFlowCheckerTask {

	private final String aapUrl;

	private final String username;

	private final String password;

	public IpAddressProvisioningWorkFlowCheckerTask(WorkFlow serviceNowTicketFulfillmentEscalationWorkFlowTask,
			long sla, String aapUrl, String username, String password) {
		super(serviceNowTicketFulfillmentEscalationWorkFlowTask, sla);
		this.aapUrl = aapUrl;
		this.username = username;
		this.password = password;
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start IpAddressProvisioningWorkFlowCheckerTask...");
		try {
			String jobId = getRequiredParameterValue(workContext, "JOB_ID");
			log.info("job id: {}", jobId);
			String urlString = aapUrl + "/api/v2/jobs/" + jobId;

			ResponseEntity<AapGetJobResponseDto> result = RestUtils.restExchange(urlString, username, password,
					AapGetJobResponseDto.class);
			AapGetJobResponseDto responseDto = result.getBody();

			if (!result.getStatusCode().is2xxSuccessful() || responseDto == null) {
				log.error("Call to the API was not successful. Response: {}", result.getStatusCode());
			}
			else if (responseDto.getArtifacts().getAzureVmPublicIp() == null) {
				log.error("ip is not ready...");
			}
			else {
				String ip = responseDto.getArtifacts().getAzureVmPublicIp();
				log.info("vm IP is: {}", ip);
				addParameter(workContext, "IP", ip);
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
