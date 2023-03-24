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
package com.redhat.parodos.tasks.tibco;

import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class TibcoWorkFlowTask extends BaseWorkFlowTask {

	private final TibcoMessageService service;

	public TibcoWorkFlowTask(TibcoMessageService service) {
		this.service = service;
	}

	@Override
	public @NonNull List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		LinkedList<WorkFlowTaskParameter> params = new LinkedList<>();
		params.add(WorkFlowTaskParameter.builder().key("topic").type(WorkFlowTaskParameterType.TEXT).optional(false)
				.description("Topic to send to").build());
		params.add(WorkFlowTaskParameter.builder().key("message").type(WorkFlowTaskParameterType.TEXT).optional(false)
				.description("Message to send to topic").build());
		return params;
	}

	@Override
	public @NonNull List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.OTHER);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		try {
			service.sendMessage(getRequiredParameterValue(workContext, "topic"),
					getRequiredParameterValue(workContext, "message"));
			service.closeConnection();
		}
		catch (Exception e) {
			log.error("TIBCO task failed", e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

}
