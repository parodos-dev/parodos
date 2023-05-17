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

import java.util.Optional;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.dto.WorkDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.registry.BeanWorkFlowRegistryImpl;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Provides functionality that is common to any WorkFlow composition in Parodos
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 * @author Richard Wang (Github: richardW98)
 */

@Component
public class WorkFlowDelegate {

	private final BeanWorkFlowRegistryImpl beanWorkFlowRegistry;

	public WorkFlowDelegate(BeanWorkFlowRegistryImpl beanWorkFlowRegistry) {
		this.beanWorkFlowRegistry = beanWorkFlowRegistry;
	}

	public WorkContext initWorkFlowContext(WorkFlowRequestDTO workFlowRequestDTO,
			WorkFlowDefinitionResponseDTO mainWorkFlowDefinitionDto) {
		WorkContext workContext = new WorkContext();

		if (workFlowRequestDTO.getArguments() != null && !workFlowRequestDTO.getArguments().isEmpty()) {
			WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
					workFlowRequestDTO.getWorkFlowName(), WorkContextDelegate.Resource.ARGUMENTS,
					WorkFlowDTOUtil.convertArgumentListToMap(workFlowRequestDTO.getArguments()));
		}
		if (mainWorkFlowDefinitionDto.getWorks() != null && !mainWorkFlowDefinitionDto.getWorks().isEmpty()) {
			mainWorkFlowDefinitionDto.getWorks().forEach(work -> initWorkContext(workContext,
					workFlowRequestDTO.findFirstWorkByName(work.getName()), work, mainWorkFlowDefinitionDto.getName()));
		}
		return workContext;
	}

	private void initWorkContext(WorkContext workContext, WorkFlowRequestDTO.WorkRequestDTO workRequestDTO,
			WorkDefinitionResponseDTO workDefinitionResponseDTO, String parentWorkflowName) {
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				workDefinitionResponseDTO.getName(), WorkContextDelegate.Resource.PARENT_WORKFLOW, parentWorkflowName);

		Optional.ofNullable(workRequestDTO).filter(dto -> !CollectionUtils.isEmpty(dto.getArguments()))
				.ifPresent(dto -> WorkContextDelegate.write(workContext,
						(WorkType.WORKFLOW == dto.getType() ? WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION
								: WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION),
						workRequestDTO.getWorkName(), WorkContextDelegate.Resource.ARGUMENTS,
						WorkFlowDTOUtil.convertArgumentListToMap(workRequestDTO.getArguments())));

		if (workDefinitionResponseDTO.getWorks() != null) {
			workDefinitionResponseDTO.getWorks()
					.forEach(work -> initWorkContext(
							workContext, Optional.ofNullable(workRequestDTO)
									.map(dto -> dto.findFirstWorkByName(work.getName())).orElse(null),
							work, workDefinitionResponseDTO.getName()));
		}
	}

	public WorkFlow getWorkFlowByName(String workFlowName) {
		return beanWorkFlowRegistry.getWorkFlowByName(workFlowName);
	}

}
