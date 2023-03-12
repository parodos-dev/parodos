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

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.engine.WorkFlowEngineBuilder;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Workflow execution service implementation
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
@Service
public class WorkFlowServiceImpl implements WorkFlowService {

	private final WorkFlowDelegate workFlowDelegate;

	private final WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private final WorkFlowRepository workFlowRepository;

	private final WorkFlowTaskRepository workFlowTaskRepository;

	private final WorkFlowWorkRepository workFlowWorkRepository;

	public WorkFlowServiceImpl(WorkFlowDelegate workFlowDelegate,
			WorkFlowDefinitionRepository workFlowDefinitionRepository, WorkFlowRepository workFlowRepository,
			WorkFlowTaskRepository workFlowTaskRepository, WorkFlowWorkRepository workFlowWorkRepository) {
		this.workFlowDelegate = workFlowDelegate;
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowRepository = workFlowRepository;
		this.workFlowTaskRepository = workFlowTaskRepository;
		this.workFlowWorkRepository = workFlowWorkRepository;
	}

	@Override
	public WorkReport execute(WorkFlowRequestDTO workFlowRequestDTO) {
		String workflowName = workFlowRequestDTO.getWorkFlowName();

		WorkFlow workFlow = workFlowDelegate.getWorkFlowExecutionByName(workflowName);
		String validationFailedMsg = validateWorkflow(workflowName, workFlow);
		if (validationFailedMsg != null) {
			return new DefaultWorkReport(WorkStatus.FAILED, new WorkContext(), new Throwable(validationFailedMsg));
		}

		WorkContext workContext = workFlowDelegate.initWorkFlowContext(workFlowRequestDTO);

		String projectId = workFlowRequestDTO.getProjectId();
		return execute(projectId, workflowName, workContext, null);
	}

	public WorkReport execute(String projectId, String workflowName, WorkContext workContext, UUID executionId) {
		WorkFlow workFlow = workFlowDelegate.getWorkFlowExecutionByName(workflowName);
		log.info("execute workFlow '{}': {}", workflowName, workFlow);
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.PROJECT, WorkContextDelegate.Resource.ID,
				projectId);
		if (executionId != null)
			WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
					WorkContextDelegate.Resource.ID, executionId.toString());
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
				WorkContextDelegate.Resource.NAME, workflowName);
		return WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(workFlow, workContext);
	}

	@Override
	public WorkFlowExecution getWorkFlowById(UUID workFlowExecutionId) {
		return this.workFlowRepository.findById(workFlowExecutionId).orElse(null);
	}

	@Override
	@Synchronized
	@Transactional
	public synchronized WorkFlowExecution saveWorkFlow(UUID projectId, UUID workFlowDefinitionId,
			WorkFlowStatus workFlowStatus, WorkFlowExecution masterWorkFlowExecution) {
		return workFlowRepository.saveAndFlush(WorkFlowExecution.builder().workFlowDefinitionId(workFlowDefinitionId)
				.projectId(projectId).status(workFlowStatus).startDate(new Date())
				.masterWorkFlowExecution(masterWorkFlowExecution).build());
	}

	@Override
	@Synchronized
	@Transactional
	public synchronized WorkFlowExecution updateWorkFlow(WorkFlowExecution workFlowExecution) {
		return workFlowRepository.saveAndFlush(workFlowExecution);
	}

	@Override
	public WorkFlowTaskExecution getWorkFlowTask(UUID workFlowExecutionId, UUID workFlowTaskDefinitionId) {
		List<WorkFlowTaskExecution> workFlowTaskExecutionList = workFlowTaskRepository
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(workFlowExecutionId, workFlowTaskDefinitionId);
		return (workFlowTaskExecutionList == null || workFlowTaskExecutionList.isEmpty()) ? null
				: workFlowTaskExecutionList.stream().findFirst().get();
	}

	@Override
	@Synchronized
	@Transactional
	public synchronized WorkFlowTaskExecution saveWorkFlowTask(String arguments, UUID workFlowTaskDefinitionId,
			UUID workFlowExecutionId, WorkFlowTaskStatus workFlowTaskStatus) {
		return workFlowTaskRepository.saveAndFlush(WorkFlowTaskExecution.builder()
				.workFlowExecutionId(workFlowExecutionId).workFlowTaskDefinitionId(workFlowTaskDefinitionId)
				.arguments(arguments).status(workFlowTaskStatus).startDate(new Date()).build());
	}

	@Override
	@Synchronized
	@Transactional
	public WorkFlowTaskExecution updateWorkFlowTask(WorkFlowTaskExecution workFlowTaskExecution) {
		return workFlowTaskRepository.saveAndFlush(workFlowTaskExecution);
	}

	private String validateWorkflow(String workflowName, WorkFlow workFlow) {
		// validate if workflow exists
		if (workFlow == null) {
			log.error("workflow '{}' is not found!", workflowName);
			return String.format("workflow '%s' cannot be found!", workflowName);
		}

		// validate if workflow is master
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.findFirstByName(workflowName);
		if (!workFlowWorkRepository.findByWorkDefinitionId(workFlowDefinition.getId()).isEmpty()) {
			log.error("workflow '{}' is not master workflow!", workflowName);
			return String.format("workflow '%s' is not master workflow!", workflowName);
		}

		// TODO: validate required parameters from definition
		return null;
	}

}
