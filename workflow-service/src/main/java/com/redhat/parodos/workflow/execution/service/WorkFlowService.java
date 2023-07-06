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
package com.redhat.parodos.workflow.execution.service;

import java.util.List;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.execution.dto.WorkFlowContextResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

/**
 * Workflow execution service
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
public interface WorkFlowService {

	WorkReport execute(WorkFlowRequestDTO workFlowRequestDTO);

	WorkReport restart(UUID workFlowExecutionId);

	WorkReport executeFallbackWorkFlow(String fallbackWorkFlowName, UUID originalWorkFlowExecutionId);

	WorkFlowExecution getWorkFlowById(UUID workFlowExecutionId);

	WorkFlowExecution saveWorkFlow(UUID projectId, UUID userId, WorkFlowDefinition workFlowDefinition,
			WorkStatus workStatus, WorkFlowExecution mainWorkFlowExecution, String arguments);

	WorkFlowExecution savedWorkFlowWithOriginalWorkFlow(UUID projectId, UUID userId,
			WorkFlowDefinition workFlowDefinition, WorkStatus workStatus, WorkFlowExecution mainWorkFlowExecution,
			String arguments, WorkFlowExecution originalWorkflowExecution);

	WorkFlowExecution updateWorkFlow(WorkFlowExecution workFlowExecution);

	List<WorkFlowResponseDTO> getWorkFlowsByProjectId(UUID projectId);

	List<WorkFlowResponseDTO> getWorkFlows();

	WorkFlowStatusResponseDTO getWorkFlowStatus(UUID workFlowExecutionId);

	WorkFlowContextResponseDTO getWorkflowParameters(UUID workFlowExecutionId,
			List<WorkContextDelegate.Resource> params);

	WorkFlowTaskExecution getWorkFlowTask(UUID workFlowExecutionId, UUID workFlowTaskDefinitionId);

	WorkFlowTaskExecution saveWorkFlowTask(String arguments, UUID workFlowTaskDefinitionId, UUID workFlowExecutionId,
			WorkStatus workFlowTaskStatus);

	WorkFlowTaskExecution updateWorkFlowTask(WorkFlowTaskExecution workFlowTaskExecution);

	void updateWorkFlowCheckerTaskStatus(UUID workFlowExecutionId, String workFlowTaskName,
			WorkStatus workFlowTaskStatus);

}