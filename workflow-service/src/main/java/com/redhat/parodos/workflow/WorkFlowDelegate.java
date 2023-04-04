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

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.dto.WorkDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.registry.BeanWorkFlowRegistryImpl;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Provides functionality that is common to any WorkFlow composition in Parodos
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 * @author Richard Wang (Github: richardW98)
 */

@Slf4j
@Component
public class WorkFlowDelegate {

	private final BeanWorkFlowRegistryImpl beanWorkFlowRegistry;

	public WorkFlowDelegate(BeanWorkFlowRegistryImpl beanWorkFlowRegistry) {
		this.beanWorkFlowRegistry = beanWorkFlowRegistry;
	}

	public WorkContext initWorkFlowContext(WorkFlowRequestDTO workFlowRequestDTO,
			WorkFlowDefinitionResponseDTO masterWorkFlowDefinitionDto) {
		WorkContext workContext = new WorkContext();

		if (workFlowRequestDTO.getArguments() != null && !workFlowRequestDTO.getArguments().isEmpty()) {
			WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
					workFlowRequestDTO.getWorkFlowName(), WorkContextDelegate.Resource.ARGUMENTS,
					WorkFlowDTOUtil.convertArgumentListToMap(workFlowRequestDTO.getArguments()));
		}
		if (masterWorkFlowDefinitionDto.getWorks() != null && !masterWorkFlowDefinitionDto.getWorks().isEmpty())
			masterWorkFlowDefinitionDto.getWorks()
					.forEach(work -> initWorkContext(workContext,
							findWorkFromRequest(workFlowRequestDTO, work.getName()), work,
							masterWorkFlowDefinitionDto.getName()));
		return workContext;
	}

	private void initWorkContext(WorkContext workContext, WorkFlowRequestDTO.WorkRequestDTO workRequestDTO,
			WorkDefinitionResponseDTO workDefinitionResponseDTO, String parentWorkflowName) {
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				workDefinitionResponseDTO.getName(), WorkContextDelegate.Resource.PARENT_WORKFLOW, parentWorkflowName);

		if (workRequestDTO != null && workRequestDTO.getArguments() != null
				&& !workRequestDTO.getArguments().isEmpty()) {
			WorkContextDelegate.write(workContext,
					WorkType.WORKFLOW.name().equalsIgnoreCase(workRequestDTO.getType())
							? WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION
							: WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
					workRequestDTO.getWorkName(), WorkContextDelegate.Resource.ARGUMENTS,
					WorkFlowDTOUtil.convertArgumentListToMap(workRequestDTO.getArguments()));
		}
		if (workDefinitionResponseDTO.getWorks() != null)
			workDefinitionResponseDTO.getWorks().forEach(work -> initWorkContext(workContext,
					findSubWorkFromRequest(workRequestDTO, work.getName()), work, workDefinitionResponseDTO.getName()));
	}

	private WorkFlowRequestDTO.WorkRequestDTO findWorkFromRequest(WorkFlowRequestDTO workFlowRequestDTO, String name) {
		return Optional.ofNullable(workFlowRequestDTO)
				.flatMap(dto -> dto.getWorks().stream().filter(work -> name.equals(work.getWorkName())).findFirst())
				.orElse(null);
	}

	private WorkFlowRequestDTO.WorkRequestDTO findSubWorkFromRequest(WorkFlowRequestDTO.WorkRequestDTO workRequestDTO,
			String name) {
		return Optional.ofNullable(workRequestDTO)
				.flatMap(dto -> dto.getWorks().stream().filter(work -> name.equals(work.getWorkName())).findFirst())
				.orElse(null);
	}

	public WorkFlow getWorkFlowExecutionByName(String workFlowName) {
		return beanWorkFlowRegistry.getWorkFlowByName(workFlowName);
	}

}
