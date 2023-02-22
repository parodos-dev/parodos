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
import com.redhat.parodos.workflow.WorkFlowStatus;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.task.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.engine.WorkFlowEngineBuilder;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

	public WorkFlowServiceImpl(WorkFlowDelegate workFlowDelegate,
			WorkFlowDefinitionRepository workFlowDefinitionRepository, WorkFlowRepository workFlowRepository,
			WorkFlowTaskRepository workFlowTaskRepository) {
		this.workFlowDelegate = workFlowDelegate;
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowRepository = workFlowRepository;
		this.workFlowTaskRepository = workFlowTaskRepository;
	}

	@Override
	public WorkReport execute(String projectId, String workFlowName,
			Map<String, Map<String, String>> workFlowTaskArguments) {
		WorkFlow workFlow = workFlowDelegate.getWorkFlowExecutionByName(workFlowName);
		if (workFlow == null) {
			log.error("workflow '{}' is not found!", workFlowName);
			return new DefaultWorkReport(WorkStatus.FAILED, new WorkContext(),
					new Throwable(String.format("workflow '{}' cannot be found!", workFlowName)));
		}

		log.info("execute workFlow '{}': {}", workFlowName, workFlow);
		WorkContext workContext = workFlowDelegate.getWorkFlowContext(
				workFlowDefinitionRepository.findByName(workFlowName).stream().findFirst().get(),
				workFlowTaskArguments);
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.PROJECT, WorkContextDelegate.Resource.ID,
				projectId);
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
				WorkContextDelegate.Resource.NAME, workFlowName);
		return WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(workFlow, workContext);
	}

	@Override

	public WorkFlowExecution getWorkFlowById(UUID workFlowExecutionId) {
		return this.workFlowRepository.findById(workFlowExecutionId).orElse(null);
	}

	@Override
	public WorkFlowExecution saveWorkFlow(UUID projectId, UUID workFlowDefinitionId, WorkFlowStatus workFlowStatus) {
		return workFlowRepository.save(WorkFlowExecution.builder().workFlowDefinitionId(workFlowDefinitionId)
				.projectId(projectId).status(workFlowStatus).startDate(new Date()).build());
	}

	@Override
	public WorkFlowExecution updateWorkFlow(WorkFlowExecution workFlowExecution) {
		return workFlowRepository.save(workFlowExecution);
	}

	@Override
	public WorkFlowTaskExecution getWorkFlowTask(UUID workFlowExecutionId, UUID workFlowTaskDefinitionId) {
		List<WorkFlowTaskExecution> workFlowTaskExecutionList = workFlowTaskRepository
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(workFlowExecutionId, workFlowTaskDefinitionId);
		return (workFlowTaskExecutionList == null || workFlowTaskExecutionList.isEmpty()) ? null
				: workFlowTaskExecutionList.stream().findFirst().get();
	}

	@Override
	public WorkFlowTaskExecution saveWorkFlowTask(String arguments, UUID workFlowTaskDefinitionId,
			UUID workFlowExecutionId, WorkFlowTaskStatus workFlowTaskStatus) {
		return workFlowTaskRepository.save(WorkFlowTaskExecution.builder().workFlowExecutionId(workFlowExecutionId)
				.workFlowTaskDefinitionId(workFlowTaskDefinitionId).arguments(arguments).status(workFlowTaskStatus)
				.startDate(new Date()).build());
	}

	@Override
	public WorkFlowTaskExecution updateWorkFlowTask(WorkFlowTaskExecution workFlowTaskExecution) {
		return workFlowTaskRepository.save(workFlowTaskExecution);
	}

}
