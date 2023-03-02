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
package com.redhat.parodos.examples.simple;

import java.util.Base64;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.redhat.parodos.workflow.task.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * An example of a task that calls a Rest Endpoint with a BasicAuth Header
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
@Component
public class SecureAPIGetTestTask extends BaseInfrastructureWorkFlowTask {

	public static final String SECURED_URL = "SECURED_URL";

	public static final String USERNAME = "USERNAME";

	public static final String PASSWORD = "PASSWORD";

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	public WorkReport execute(WorkContext workContext) {
		try {
			String urlString = getParameterValue(workContext, SECURED_URL);
			String username = getParameterValue(workContext, USERNAME);
			String password = getParameterValue(workContext, PASSWORD);
			log.info("Calling: urlString: {} username: {}", urlString, username);
			ResponseEntity<String> result = restExchange(urlString, username, password);
			if (result.getStatusCode().is2xxSuccessful()) {
				log.info("Rest call completed: {}", result.getBody());
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
			}
			log.error("Call to the API was not successful. Response: {}", result.getStatusCode());
		}
		catch (Exception e) {
			log.error("There was an issue with the REST call: {}", e.getMessage());

		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	ResponseEntity<String> restExchange(String urlString, String username, String password) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result = restTemplate.exchange(urlString, HttpMethod.GET,
				getRequestWithHeaders(username, password), String.class);
		return result;
	}

	HttpEntity<String> getRequestWithHeaders(String username, String password) {
		String base64Creds = getBase64Creds(username, password);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		return new HttpEntity<String>(headers);
	}

	String getBase64Creds(String username, String password) {
		String plainCreds = username + ":" + password;
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);
		return base64Creds;
	}

	@Override
	public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkFlowTaskParameter.builder().key(SECURED_URL)
						.description("The URL of the Secured API you wish to call").optional(false)
						.type(WorkFlowTaskParameterType.URL).build(),
				WorkFlowTaskParameter.builder().key(USERNAME).description("Please enter your username authentication")
						.optional(false).type(WorkFlowTaskParameterType.TEXT).build(),
				WorkFlowTaskParameter.builder().key(PASSWORD)
						.description("Please enter your password for authentication (it will not be stored)")
						.optional(false).type(WorkFlowTaskParameterType.PASSWORD).build());
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.HTTP2XX, WorkFlowTaskOutput.OTHER);
	}

}
