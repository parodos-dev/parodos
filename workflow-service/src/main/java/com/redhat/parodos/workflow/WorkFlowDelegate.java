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
package com.redhat.parodos.workflow;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import org.springframework.stereotype.Component;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.registry.BeanWorkFlowRegistryImpl;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides functionality that is common to any WorkFlow composition in Parodos
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
@Component
public class WorkFlowDelegate {

	private final BeanWorkFlowRegistryImpl workFlowRegistry;

	private final WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	public WorkFlowDelegate(BeanWorkFlowRegistryImpl workFlowRegistry,
			WorkFlowDefinitionServiceImpl workFlowDefinitionService) {
		this.workFlowRegistry = workFlowRegistry;
		this.workFlowDefinitionService = workFlowDefinitionService;
	}

	public WorkContext getWorkFlowContext(WorkFlowDefinition workFlowDefinition,
			Map<String, Map<String, String>> workFlowTaskParameterValues, Map<String, String> argumentRequestDTOs) {
		WorkContext workContext = new WorkContext();
		workFlowDefinition.getWorkFlowTaskDefinitions().forEach(workFlowTaskDefinition -> {
			log.info("****** workflow task name: {}, parameter values: {}", workFlowTaskDefinition.getName(),
					workFlowTaskParameterValues.get(workFlowTaskDefinition.getName()));
			WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_DEFINITION,
					workFlowTaskDefinition.getName(), WorkContextDelegate.Resource.ID,
					getWorkFlowTaskDefinitionId(workFlowDefinition.getName(), workFlowTaskDefinition.getName()));
			WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
					workFlowTaskDefinition.getName(), WorkContextDelegate.Resource.ARGUMENTS,
					workFlowTaskParameterValues.get(workFlowTaskDefinition.getName()) == null ? Map.of()
							: workFlowTaskParameterValues.get(workFlowTaskDefinition.getName()));
		});
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ARGUMENTS, argumentRequestDTOs);
		return workContext;
	}

	public WorkFlow getWorkFlowExecutionByName(String workFlowName) {
		return workFlowRegistry.getWorkFlowByName(workFlowName);
	}

	public UUID getWorkFlowTaskDefinitionId(String workFlowName, String workFlowTaskName) {
		return UUID.fromString(workFlowDefinitionService.getWorkFlowDefinitionsByName(workFlowName).stream().findFirst()
				.get().getWorks().stream().filter(task -> task.getName().equalsIgnoreCase(workFlowTaskName)).findFirst()
				.get().getId());
	}

}
