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
package com.redhat.parodos.infrastructure;

import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.redhat.parodos.workflows.WorkFlowTask;
import com.redhat.parodos.workflows.WorkContextDelegate;
import com.redhat.parodos.workflows.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * An example of a task that calls a Rest Endpoint with a BasicAuth Header
 *
 * @author Luke Shannon (Github: lshannon)
 */
@Slf4j
public class SecureAPIGetTest implements WorkFlowTask {

    static public final String SECURED_URL = "SECURED_URL";
    static public final String USERNAME = "USERNAME";
    static public final String PASSWORD = "PASSWORD";

    /**
     * Executed by the InfrastructureTask engine as part of the Workflow
     */
    public WorkReport execute(WorkContext workContext) {
        try {
            String urlString = WorkContextDelegate.getRequiredValueFromRequestParams(workContext, SECURED_URL);
            String username = WorkContextDelegate.getRequiredValueFromRequestParams(workContext, USERNAME);
            String password = WorkContextDelegate.getRequiredValueFromRequestParams(workContext, PASSWORD);
            log.info("Calling: urlString: {} username: {}", urlString, username);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> result = restTemplate.exchange(urlString, HttpMethod.GET, getRequestWithHeaders(username, password), String.class);
            if (result.getStatusCode().is2xxSuccessful()) {
                log.info("Rest call completed: {}", result.getBody());
                return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
            }
            log.error("Call to the API was not successful. Response: {}", result.getStatusCode());
        } catch (Exception e) {
            log.error("There was an issue with the REST call: {}", e.getMessage());

        }
        return new DefaultWorkReport(WorkStatus.FAILED, workContext);
    }
    
    HttpEntity<String> getRequestWithHeaders(String username, String password) {
    	String plainCreds = username+":"+ password;
    	byte[] plainCredsBytes = plainCreds.getBytes();
    	byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
    	String base64Creds = new String(base64CredsBytes);
    	HttpHeaders headers = new HttpHeaders();
    	headers.add("Authorization", "Basic " + base64Creds);
    	return new HttpEntity<String>(headers);
    }
    
    @Override
    public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
        return List.of(
				WorkFlowTaskParameter.builder()
						.key(SECURED_URL)
						.description("The URL of the Secured API you wish to call")
						.optional(false)
						.type(WorkFlowTaskParameterType.URL)
						.build(),
				WorkFlowTaskParameter.builder()
                        .key(USERNAME)
                        .description("Please enter your username authentication")
                        .optional(false)
                        .type(WorkFlowTaskParameterType.TEXT)
                        .build(),
				WorkFlowTaskParameter.builder()
                        .key(PASSWORD)
                        .description("Please enter your password for authentication (it will not be stored)")
                        .optional(false)
                        .type(WorkFlowTaskParameterType.PASSWORD)
                        .build());
    }
}