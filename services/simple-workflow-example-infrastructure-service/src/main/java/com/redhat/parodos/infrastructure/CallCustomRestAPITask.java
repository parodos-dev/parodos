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

import java.util.List;

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
 * An example of a task that calls a REST endpoint. It gets all its arguments passed in from the Service using the WorkContext
 * <p>
 * The InfrastructureTaskEngine will need to put the following arguments into the WorkContext for this task:
 * <p>
 * TOKEN_PASSED_IN_FROM_SERVICE: The rest API needs a token
 * PAYLOAD_PASSED_IN_FROM_SERVICE: The payload to send
 * URL_PASSED_IN_FROM_SERVICE: The URL of the service
 *
 * @author Luke Shannon (Github: lshannon)
 */
@Slf4j
public class CallCustomRestAPITask implements WorkFlowTask {

    static public final String PAYLOAD_PASSED_IN_FROM_SERVICE = "PAYLOAD_PASSED_IN_FROM_SERVICE";
    static public final String URL_PASSED_IN_FROM_SERVICE = "URL_PASSED_IN_FROM_SERVICE";

    /**
     * Executed by the InfrastructureTask engine as part of the Workflow
     */
    public WorkReport execute(WorkContext workContext) {
        try {
            String urlString = WorkContextDelegate.getRequiredValueFromRequestParams(workContext, URL_PASSED_IN_FROM_SERVICE);
            String payload = WorkContextDelegate.getRequiredValueFromRequestParams(workContext, PAYLOAD_PASSED_IN_FROM_SERVICE);
            log.info("Running Task REST API Call: urlString: {} payload: {} ", urlString, payload);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> result = restTemplate.postForEntity(urlString, payload, String.class);
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

    @Override
    public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
        return List.of(
				WorkFlowTaskParameter.builder()
						.key(URL_PASSED_IN_FROM_SERVICE)
						.description("The Url of the service (ie: https://httpbin.org/post")
						.optional(false)
						.type(WorkFlowTaskParameterType.URL)
						.build(),
				WorkFlowTaskParameter.builder()
						.key(PAYLOAD_PASSED_IN_FROM_SERVICE)
						.description("Json of what to provide for data. (ie: 'Hello!')")
						.optional(false)
						.type(WorkFlowTaskParameterType.PASSWORD)
						.build());
    }
}