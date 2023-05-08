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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PreDestroy;

import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.service.ProjectService;
import com.redhat.parodos.security.SecurityUtils;
import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionService;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.exceptions.WorkflowPersistenceFailedException;
import com.redhat.parodos.workflow.execution.dto.WorkFlowContextResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowOptionsResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkStatusResponseDTO;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.engine.WorkFlowEngineBuilder;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Workflow execution service implementation
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
@Service
public class WorkFlowServiceImpl implements WorkFlowService {

	private final ProjectService projectService;

	private final WorkFlowDelegate workFlowDelegate;

	private final WorkFlowServiceDelegate workFlowServiceDelegate;

	private final WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private final WorkFlowRepository workFlowRepository;

	private final WorkFlowTaskRepository workFlowTaskRepository;

	private final WorkFlowWorkRepository workFlowWorkRepository;

	private final WorkFlowDefinitionService workFlowDefinitionService;

	private final MeterRegistry metricRegistry;

	public WorkFlowServiceImpl(WorkFlowDelegate workFlowDelegate, WorkFlowServiceDelegate workFlowServiceDelegate,
			WorkFlowDefinitionRepository workFlowDefinitionRepository,
			WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository, WorkFlowRepository workFlowRepository,
			WorkFlowTaskRepository workFlowTaskRepository, WorkFlowWorkRepository workFlowWorkRepository,
			WorkFlowDefinitionService workFlowDefinitionService, MeterRegistry metricRegistry,
			ProjectService projectService, ModelMapper modelMapper) {
		this.workFlowDelegate = workFlowDelegate;
		this.workFlowServiceDelegate = workFlowServiceDelegate;
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
		this.workFlowRepository = workFlowRepository;
		this.workFlowTaskRepository = workFlowTaskRepository;
		this.workFlowWorkRepository = workFlowWorkRepository;
		this.workFlowDefinitionService = workFlowDefinitionService;
		this.metricRegistry = metricRegistry;
		this.projectService = projectService;
	}

	private void statusCounterWithStatus(WorkStatus status) {
		if (status == null) {
			return;
		}
		Counter.builder("workflow.executions").tag("status", status.toString())
				.description("Workflow executions phases by status update").register(this.metricRegistry).increment();
	}

	@Override
	public WorkReport execute(WorkFlowRequestDTO workFlowRequestDTO) {
		String workflowName = workFlowRequestDTO.getWorkFlowName();

		WorkFlow workFlow = workFlowDelegate.getWorkFlowExecutionByName(workflowName);
		String validationFailedMsg = validateWorkflow(workflowName, workFlow);
		if (validationFailedMsg != null) {
			return new DefaultWorkReport(WorkStatus.FAILED, new WorkContext(), new Throwable(validationFailedMsg));
		}

		WorkContext workContext = workFlowDelegate.initWorkFlowContext(workFlowRequestDTO,
				workFlowDefinitionService.getWorkFlowDefinitionByName(workflowName));

		UUID projectId = workFlowRequestDTO.getProjectId();
		return execute(projectId, workflowName, workContext, null);
	}

	public WorkReport execute(UUID projectId, String workflowName, WorkContext workContext, UUID executionId) {
		WorkFlow workFlow = workFlowDelegate.getWorkFlowExecutionByName(workflowName);
		log.info("execute workFlow '{}': {}", workflowName, workFlow);
		WorkContextUtils.setProjectId(workContext, projectId);
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
	public WorkFlowExecution saveWorkFlow(UUID projectId, UUID workFlowDefinitionId, WorkStatus workStatus,
			WorkFlowExecution mainWorkFlowExecution, String arguments) {
		try {
			this.statusCounterWithStatus(workStatus);
			return workFlowRepository.save(WorkFlowExecution.builder().workFlowDefinitionId(workFlowDefinitionId)
					.projectId(projectId).status(workStatus).startDate(new Date()).arguments(arguments)
					.mainWorkFlowExecution(mainWorkFlowExecution).build());
		}
		catch (DataAccessException | IllegalArgumentException e) {
			log.error("failing persist workflow execution for: {} in main workflow execution: {}. error Message: {}",
					workFlowDefinitionId, mainWorkFlowExecution.getId(), e.getMessage());
			throw new WorkflowPersistenceFailedException(e.getMessage());
		}
	}

	@Override
	public synchronized WorkFlowExecution updateWorkFlow(WorkFlowExecution workFlowExecution) {
		this.statusCounterWithStatus(workFlowExecution.getStatus());
		return workFlowRepository.save(workFlowExecution);
	}

	@Override
	public List<WorkFlowResponseDTO> getWorkFlowsByProjectId(UUID projectId) {
		ProjectResponseDTO project = projectService.getProjectByIdAndUsername(projectId, SecurityUtils.getUsername());
		return workFlowRepository.findAllByProjectId(project.getId()).stream()
				.filter(workFlowExecution -> workFlowExecution.getMainWorkFlowExecution() == null)
				.map(this::buildWorkflowResponseDTO).toList();
	}

	@Override
	public List<WorkFlowResponseDTO> getWorkFlows() {
		List<ProjectResponseDTO> projects = projectService.findProjectsByUserName(SecurityUtils.getUsername());
		return projects.stream()
				.flatMap(project -> workFlowRepository.findAllByProjectId(project.getId()).stream()
						.filter(workFlowExecution -> workFlowExecution.getMainWorkFlowExecution() == null)
						.map(this::buildWorkflowResponseDTO))
				.toList();
	}

	@Override
	public WorkFlowStatusResponseDTO getWorkFlowStatus(UUID workFlowExecutionId) {
		WorkFlowExecution workFlowExecution = workFlowRepository.findById(workFlowExecutionId).orElseThrow(() -> {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("workflow execution id: %s not found!", workFlowExecutionId));
		});

		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository
				.findById(workFlowExecution.getWorkFlowDefinitionId()).orElseThrow(() -> {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND,
							String.format("workflow definition id: %s not found!", workFlowExecution.getId()));
				});

		// check if it is not an inner workflow
		if (workFlowExecution.getMainWorkFlowExecution() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("workflow id: %s from workflow name: %s is an inner workflow!",
							workFlowExecution.getId(), workFlowDefinition.getName()));
		}

		List<WorkStatusResponseDTO> workFlowWorksStatusResponseDTOs = workFlowServiceDelegate
				.getWorkFlowAndWorksStatus(workFlowExecution, workFlowDefinition);

		return WorkFlowStatusResponseDTO.builder().workFlowExecutionId(workFlowExecution.getId())
				.workFlowName(workFlowDefinition.getName()).status(workFlowExecution.getStatus())
				.works(workFlowWorksStatusResponseDTOs).build();
	}

	@Override
	public WorkFlowContextResponseDTO getWorkflowParameters(UUID workFlowExecutionId,
			List<WorkContextDelegate.Resource> params) {
		WorkFlowExecution workFlowExecution = workFlowRepository.findById(workFlowExecutionId).orElseThrow(() -> {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("workflow execution id: %s not found!", workFlowExecutionId));
		});
		Map options = Map.of();
		if (params.contains(WorkContextDelegate.Resource.WORKFLOW_OPTIONS)) {
			options = Optional.ofNullable(
					(Map) WorkContextDelegate.read(workFlowExecution.getWorkFlowExecutionContext().getWorkContext(),
							WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
							WorkContextDelegate.Resource.WORKFLOW_OPTIONS))
					.orElse(Map.of());
		}

		return WorkFlowContextResponseDTO.builder().workFlowExecutionId(workFlowExecution.getId())
				.workFlowOptions(WorkFlowOptionsResponseDTO.builder()
						.currentVersion((WorkFlowOption) options.get("currentVersion"))
						.continuationOptions(getFlowOptions(options, "continuationOptions"))
						.migrationOptions(getFlowOptions(options, "migrationOptions"))
						.otherOptions(getFlowOptions(options, "otherOptions"))
						.upgradeOptions(getFlowOptions(options, "upgradeOptions"))
						.newOptions(getFlowOptions(options, "newOptions")).build())
				.build();
	}

	@Nullable
	private static List<WorkFlowOption> getFlowOptions(Map options, String key) {
		return options.containsKey(key) ? (List<WorkFlowOption>) options.get(key) : null;
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
			UUID workFlowExecutionId, WorkStatus workFlowTaskStatus) {
		try {
			return workFlowTaskRepository.save(WorkFlowTaskExecution.builder().workFlowExecutionId(workFlowExecutionId)
					.workFlowTaskDefinitionId(workFlowTaskDefinitionId).arguments(arguments).status(workFlowTaskStatus)
					.startDate(new Date()).build());
		}
		catch (DataAccessException | IllegalArgumentException e) {
			log.error("failing persist task execution for: {} in main workflow execution: {}. error Message: {}",
					workFlowTaskDefinitionId, workFlowTaskDefinitionId, e.getMessage());
			throw new WorkflowPersistenceFailedException(e.getMessage());
		}
	}

	@Override
	public WorkFlowTaskExecution updateWorkFlowTask(WorkFlowTaskExecution workFlowTaskExecution) {
		try {
			return workFlowTaskRepository.save(workFlowTaskExecution);
		}
		catch (DataAccessException | IllegalArgumentException e) {
			log.error("failed updating task execution for: {} in execution: {}. error Message: {}",
					workFlowTaskExecution.getWorkFlowTaskDefinitionId(), workFlowTaskExecution.getId(), e.getMessage());
			throw new WorkflowPersistenceFailedException(e.getMessage());
		}
	}

	@Override
	public void updateWorkFlowCheckerTaskStatus(UUID workFlowExecutionId, String workFlowTaskName,
			WorkStatus workFlowTaskStatus) {
		// get main workflow associated to the execution id
		WorkFlowExecution mainWorkFlowExecution = workFlowRepository.findById(workFlowExecutionId).orElseThrow(() -> {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("workflow execution id: %s not found!", workFlowExecutionId));
		});
		// get workflow checker task definition
		WorkFlowTaskDefinition workFlowTaskDefinition = workFlowTaskDefinitionRepository
				.findFirstByNameAndWorkFlowDefinitionType(workFlowTaskName, WorkFlowType.CHECKER);
		if (Objects.isNull(workFlowTaskDefinition)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("workflow checker task name: %s not found!", workFlowTaskName));
		}
		// find workflow checker execution associated to the checker task
		List<WorkFlowExecution> workFlowExecutions = workFlowRepository
				.findByMainWorkFlowExecution(mainWorkFlowExecution);
		WorkFlowExecution workFlowCheckerExecution = workFlowExecutions.stream()
				.filter(workFlowExecution -> workFlowExecution.getWorkFlowDefinitionId()
						.equals(workFlowTaskDefinition.getWorkFlowDefinition().getId()))
				.max(Comparator.comparing(WorkFlowExecution::getStartDate)).orElseThrow(() -> {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String
							.format("workflow checker associated to task: %s has not started!", workFlowTaskName));
				});
		// get the workflow checker task execution
		WorkFlowTaskExecution workFlowTaskExecution = workFlowTaskRepository
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(workFlowCheckerExecution.getId(),
						workFlowTaskDefinition.getId())
				.stream().findFirst().orElseThrow(() -> {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
							String.format("workflow checker task name: %s has not been executed!", workFlowTaskName));
				});
		// update workflow checker task status
		workFlowTaskExecution.setStatus(workFlowTaskStatus);
		workFlowTaskRepository.save(workFlowTaskExecution);
	}

	public List<WorkFlowExecution> findRunningChecker(WorkFlowExecution mainWorkFlow) {
		return workFlowRepository.findRunningCheckersById(mainWorkFlow.getId());
	}

	private String validateWorkflow(String workflowName, WorkFlow workFlow) {
		// validate if workflow exists
		if (workFlow == null) {
			log.error("workflow '{}' is not found!", workflowName);
			return String.format("workflow '%s' cannot be found!", workflowName);
		}

		// validate if workflow is main
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.findFirstByName(workflowName);
		if (workFlowDefinition == null) {
			return String.format("workflow '%s' is not registered!", workflowName);
		}

		if (workFlowWorkRepository.findFirstByWorkDefinitionId(workFlowDefinition.getId()) != null) {
			log.error("workflow '{}' is not main workflow!", workflowName);
			return String.format("workflow '%s' is not main workflow!", workflowName);
		}

		// TODO: validate required parameters from definition
		return null;
	}

	@PreDestroy
	public void gracefulShutdown() {
		log.info(">> Shutting down the workflow service");
	}

	private WorkFlowResponseDTO buildWorkflowResponseDTO(WorkFlowExecution workflowExecution) {
		return WorkFlowResponseDTO.builder().workFlowExecutionId(workflowExecution.getId())
				.projectId(workflowExecution.getProjectId())
				.workFlowName(workFlowDefinitionService
						.getWorkFlowDefinitionById(workflowExecution.getWorkFlowDefinitionId()).getName())
				.workStatus(WorkStatus.valueOf(workflowExecution.getStatus().name()))
				.startDate(Optional.ofNullable(workflowExecution.getStartDate()).map(Date::toString).orElse(null))
				.endDate(Optional.ofNullable(workflowExecution.getEndDate()).map(Date::toString).orElse(null))
				.createUser(SecurityUtils.getUsername()).build();
	}

}
