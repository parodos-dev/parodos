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

import java.util.List;
import java.util.UUID;

import com.redhat.parodos.workflows.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.MissingParameterException;
import com.redhat.parodos.workflows.WorkContextDelegate;
import com.redhat.parodos.workflows.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple WorkFlowTask that logs the values of a parameter
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class LoggingMessageWorkFlowTask extends BaseInfrastructureWorkFlowTask {
	
	private String MESSAGE = "MESSAGE";

	public LoggingMessageWorkFlowTask(String name) {
		super(name);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		try {
			log.info("Running the process: {}", (String)WorkContextDelegate.getRequiredValueFromRequestParams(workContext, MESSAGE));
		} catch (MissingParameterException e) {
			log.error("MESSAGE must be supplied as a parameter");
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}
	
	
	  @Override
	    public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
	        return List.of(
					WorkFlowTaskParameter.builder()
							.key(MESSAGE)
							.description("The message to log")
							.optional(false)
							.type(WorkFlowTaskParameterType.TEXT)
							.build());
	    }

}
