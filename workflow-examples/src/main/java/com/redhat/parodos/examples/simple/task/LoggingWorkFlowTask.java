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
package com.redhat.parodos.examples.simple.task;

import java.util.List;
import java.util.Map;

import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * logging task execution
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
@Component
public class LoggingWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Writing a message to the logs from: {}", getName());
		try {
			String userId = getRequiredParameterValue(workContext, "user-id");
			String apiServer = getRequiredParameterValue(workContext, "api-server");
			log.info("task parameter 'api-server' value in {} is {}", getName(), apiServer);
			log.info("workflow parameter 'user-id' value in {} is {}", getName(), userId);
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		catch (Exception e) {
			log.error("There was an issue with the task {}: {}", getName(), e.getMessage());
		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	@Override
	public List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key("api-server").description("The api server").type(WorkParameterType.URL)
						.optional(false).build(),
				WorkParameter.builder().key("user-id").description("The user id").type(WorkParameterType.TEXT)
						.optional(false).jsonSchemaOptions(Map.of("minLength", "1", "maxLength", "64")).build());
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.OTHER);
	}

}
