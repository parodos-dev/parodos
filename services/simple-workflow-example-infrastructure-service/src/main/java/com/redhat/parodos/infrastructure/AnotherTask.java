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
 * An example of a task. This one only logs
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class AnotherTask implements WorkFlowTask {
	
	private static final String TICKET_SERVICE_NAME = "TICKET_SERVICE_NAME";
	
	/**
	 * This is a simple example and only writes a Log
	 */
	public WorkReport execute(WorkContext workContext) {
		String ticketServiceName = WorkContextDelegate.getOptionalValueFromRequestParams(workContext, TICKET_SERVICE_NAME, "defaultTicketService");
		log.info("Executing another Task. This one does nothing...in practise this could open a Ticket in Jira, inject into Github. Sky is the limit. Here is the optional parameter {} : {}", TICKET_SERVICE_NAME, ticketServiceName);
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	@Override
	public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkFlowTaskParameter.builder()
						.key(TICKET_SERVICE_NAME)
						.description("Name of the ticket service to integrate with")
						.type(WorkFlowTaskParameterType.TEXT)
						.optional(true)
						.build());
	}
}
