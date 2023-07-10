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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PreDestroy;

import com.redhat.parodos.common.exceptions.IllegalWorkFlowStateException;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.common.exceptions.ResourceType;
import com.redhat.parodos.project.dto.response.ProjectResponseDTO;
import com.redhat.parodos.project.service.ProjectService;
import com.redhat.parodos.security.SecurityUtils;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.service.UserService;
import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.context.WorkContextDelegate.ProcessType;
import com.redhat.parodos.workflow.context.WorkContextDelegate.Resource;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
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
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionContext;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import org.springframework.dao.DataAccessException;
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

	private final ProjectService projectService;

	private final WorkFlowDelegate workFlowDelegate;

	private final WorkFlowServiceDelegate workFlowServiceDelegate;

	private final WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private final WorkFlowRepository workFlowRepository;

	private final WorkFlowTaskRepository workFlowTaskRepository;

	private final WorkFlowWorkRepository workFlowWorkRepository;

	private final WorkFlowDefinitionService workFlowDefinitionService;

	private final UserService userService;

	private final MeterRegistry metricRegistry;

	private final WorkFlowExecutor workFlowExecutor;

	public WorkFlowServiceImpl(ProjectService projectService, UserService userService,
			WorkFlowDefinitionService workFlowDefinitionService, WorkFlowDelegate workFlowDelegate,
			WorkFlowServiceDelegate workFlowServiceDelegate, WorkFlowDefinitionRepository workFlowDefinitionRepository,
			WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository, WorkFlowRepository workFlowRepository,
			WorkFlowTaskRepository workFlowTaskRepository, WorkFlowWorkRepository workFlowWorkRepository,
			MeterRegistry metricRegistry, WorkFlowExecutor workFlowExecutor) {
		this.projectService = projectService;
		this.userService = userService;
		this.workFlowDefinitionService = workFlowDefinitionService;
		this.workFlowDelegate = workFlowDelegate;
		this.workFlowServiceDelegate = workFlowServiceDelegate;
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
		this.workFlowRepository = workFlowRepository;
		this.workFlowTaskRepository = workFlowTaskRepository;
		this.workFlowWorkRepository = workFlowWorkRepository;
		this.metricRegistry = metricRegistry;
		this.workFlowExecutor = workFlowExecutor;
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
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		String workflowName = workFlowRequestDTO.getWorkFlowName();
		WorkFlow workFlow = workFlowDelegate.getWorkFlowByName(workflowName);
		String validationFailedMsg = validateWorkflow(workflowName, workFlow);
		if (validationFailedMsg != null) {
			return new DefaultWorkReport(WorkStatus.FAILED, new WorkContext(), new Throwable(validationFailedMsg));
		}

		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = workFlowDefinitionService
				.getWorkFlowDefinitionByName(workflowName);
		WorkContext workContext = workFlowDelegate.initWorkFlowContext(workFlowRequestDTO,
				workFlowDefinitionResponseDTO);

		if (workFlowRequestDTO.getInvokingExecutionId() != null) {
			mergeContextArgumentsFromExecution(workFlowRequestDTO.getInvokingExecutionId(), workContext);
		}

		// save workflow execution
		String arguments = WorkFlowDTOUtil.writeObjectValueAsString(WorkContextDelegate.read(workContext,
				ProcessType.WORKFLOW_EXECUTION, workFlowDefinitionResponseDTO.getName(), Resource.ARGUMENTS));
		UUID projectId = workFlowRequestDTO.getProjectId();
		WorkFlowExecution workFlowExecution = saveWorkFlow(projectId, user.getId(),
				workFlowDefinitionRepository.findFirstByName(workflowName), WorkStatus.IN_PROGRESS, null, arguments);
		WorkContextUtils.setMainExecutionId(workContext, workFlowExecution.getId());
		workFlowExecution.setWorkFlowExecutionContext(WorkFlowExecutionContext.builder().workContext(workContext)
				.mainWorkFlowExecution(workFlowExecution).build());
		workFlowExecution = this.workFlowRepository.save(workFlowExecution);
		workFlowExecutor.execute(WorkFlowExecutor.ExecutionContext.builder().projectId(projectId).userId(user.getId())
				.workFlowName(workflowName).workContext(workContext).executionId(workFlowExecution.getId())
				.fallbackWorkFlowName(workFlowDefinitionResponseDTO.getFallbackWorkflow()).build());
		return new DefaultWorkReport(WorkStatus.IN_PROGRESS, workContext);
	}

	@Override
	public WorkReport restart(UUID workFlowExecutionId) {
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		WorkFlowExecution workFlowExecution = workFlowRepository.findById(workFlowExecutionId).orElseThrow(() -> {
			throw new ResourceNotFoundException(ResourceType.WORKFLOW_EXECUTION, workFlowExecutionId);
		});
		WorkFlowDefinition workFlowDefinition = Optional.ofNullable(workFlowExecution.getWorkFlowDefinition())
				.orElseThrow(() -> {
					throw new ResourceNotFoundException(ResourceType.WORKFLOW_DEFINITION, workFlowExecution.getId());
				});
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = workFlowDefinitionService
				.getWorkFlowDefinitionByName(workFlowDefinition.getName());
		// check if it is not an inner workflow
		if (workFlowExecution.getMainWorkFlowExecution() != null) {
			throw new IllegalWorkFlowStateException(
					String.format("workflow id: %s from workflow name: %s is an inner workflow!",
							workFlowExecution.getId(), workFlowDefinition.getName()));
		}
		if (workFlowExecution.getWorkFlowExecutionContext() == null) {
			throw new IllegalWorkFlowStateException(String.format(
					"workflow id: %s from workflow name: %s has not Workflow Execution Context saved in the database, cannot restart it!",
					workFlowExecution.getId(), workFlowDefinition.getName()));
		}

		WorkFlowExecution restartedWorkFlowExecution = saveRestartedWorkFlow(workFlowExecution.getProjectId(),
				user.getId(), workFlowDefinition, WorkStatus.IN_PROGRESS, null, workFlowExecution.getArguments(),
				workFlowExecution);
		WorkContext context = rebuildRestartedWorkContext(
				workFlowExecution.getWorkFlowExecutionContext().getWorkContext(), restartedWorkFlowExecution,
				workFlowDefinition);

		restartedWorkFlowExecution.setWorkFlowExecutionContext(workFlowExecution.getWorkFlowExecutionContext());
		restartedWorkFlowExecution = this.workFlowRepository.save(restartedWorkFlowExecution);
		workFlowExecutor.execute(
				WorkFlowExecutor.ExecutionContext.builder().projectId(restartedWorkFlowExecution.getProjectId())
						.userId(user.getId()).workFlowName(workFlowDefinition.getName()).workContext(context)
						.executionId(restartedWorkFlowExecution.getId())
						.fallbackWorkFlowName(workFlowDefinitionResponseDTO.getFallbackWorkflow()).build());

		return new DefaultWorkReport(WorkStatus.IN_PROGRESS, context);
	}

	private static WorkContext rebuildRestartedWorkContext(WorkContext context, WorkFlowExecution workFlowExecution,
			WorkFlowDefinition workFlowDefinition) {
		if (context == null) {
			log.warn(
					"workflow id: {} from workflow name: {} has null WorkContext from WorkflowExecutionContext, using default empty",
					workFlowExecution.getId(), workFlowDefinition.getName());
			context = new WorkContext();
		}
		context.getContext().entrySet().removeIf(entry -> {
			String key = entry.getKey();
			return !(key.endsWith(WorkContextDelegate.Resource.ARGUMENTS.name())
					|| key.endsWith(WorkContextDelegate.Resource.PARENT_WORKFLOW.name()));
		});
		WorkContextUtils.setMainExecutionId(context, workFlowExecution.getId());
		return context;
	}

	/**
	 * Merge the context of the previous execution into the top level of this current
	 * context. Don't override in case of collisions.
	 * @param executionId the execution context with the source context arguments
	 * @param target target context to put arguments on
	 */
	private void mergeContextArgumentsFromExecution(UUID executionId, WorkContext target) {
		Optional<WorkFlowExecution> invokedBy = workFlowRepository.findById(executionId);
		if (invokedBy.isEmpty()) {
			throw new ResourceNotFoundException(ResourceType.WORKFLOW_EXECUTION, executionId);
		}

		ContextArgumentsMerger.mergeArguments(invokedBy.get(), target);
	}

	@Override
	public WorkFlowExecution getWorkFlowById(UUID workFlowExecutionId) {
		return this.workFlowRepository.findById(workFlowExecutionId).orElse(null);
	}

	@Override
	public WorkFlowExecution saveWorkFlow(UUID projectId, UUID userId, WorkFlowDefinition workFlowDefinition,
			WorkStatus workStatus, WorkFlowExecution mainWorkFlowExecution, String arguments) {
		return saveRestartedWorkFlow(projectId, userId, workFlowDefinition, workStatus, mainWorkFlowExecution,
				arguments, null);
	}

	@Override
	public WorkFlowExecution saveRestartedWorkFlow(UUID projectId, UUID userId, WorkFlowDefinition workFlowDefinition,
			WorkStatus workStatus, WorkFlowExecution mainWorkFlowExecution, String arguments,
			WorkFlowExecution originalWorkflowExecution) {
		User user = userService.getUserEntityById(userId);
		try {
			this.statusCounterWithStatus(workStatus);
			WorkFlowExecution.WorkFlowExecutionBuilder builder = WorkFlowExecution.builder()
					.workFlowDefinition(workFlowDefinition).projectId(projectId).user(user).status(workStatus)
					.startDate(new Date()).arguments(arguments).mainWorkFlowExecution(mainWorkFlowExecution);
			if (originalWorkflowExecution != null) {
				builder = builder.originalWorkFlowExecution(originalWorkflowExecution);
			}
			return workFlowRepository.save(builder.build());
		}
		catch (DataAccessException e) {
			log.error("failing persist workflow execution for: {} in main workflow execution: {}. error Message: {}",
					workFlowDefinition.getId(), mainWorkFlowExecution.getId(), e.getMessage());
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
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		List<ProjectResponseDTO> projects = projectService.getProjectByIdAndUserId(projectId, user.getId());
		List<WorkFlowResponseDTO> workFlowResponseDTOs = new ArrayList<>();
		projects.forEach(project -> workFlowResponseDTOs.addAll(workFlowRepository.findAllByProjectId(project.getId())
				.stream().filter(workFlowExecution -> workFlowExecution.getMainWorkFlowExecution() == null)
				.map(this::buildWorkflowResponseDTO).toList()));
		return workFlowResponseDTOs;
	}

	@Override
	public List<WorkFlowResponseDTO> getWorkFlows() {
		User user = userService.getUserEntityByUsername(SecurityUtils.getUsername());
		List<ProjectResponseDTO> projects = projectService.getProjectsByUserId(user.getId());
		return projects.stream()
				.flatMap(project -> workFlowRepository.findAllByProjectId(project.getId()).stream()
						.filter(workFlowExecution -> workFlowExecution.getMainWorkFlowExecution() == null)
						.map(this::buildWorkflowResponseDTO))
				.toList();
	}

	@Override
	public WorkFlowStatusResponseDTO getWorkFlowStatus(UUID workFlowExecutionId) {
		WorkFlowExecution workFlowExecution = workFlowRepository.findById(workFlowExecutionId).orElseThrow(() -> {
			throw new ResourceNotFoundException(ResourceType.WORKFLOW_EXECUTION, workFlowExecutionId);
		});

		WorkFlowDefinition workFlowDefinition = Optional.ofNullable(workFlowExecution.getWorkFlowDefinition())
				.orElseThrow(() -> {
					throw new ResourceNotFoundException(ResourceType.WORKFLOW_DEFINITION, workFlowExecution.getId());
				});

		// check if it is not an inner workflow
		if (workFlowExecution.getMainWorkFlowExecution() != null) {
			throw new IllegalWorkFlowStateException(
					String.format("workflow id: %s from workflow name: %s is an inner workflow!",
							workFlowExecution.getId(), workFlowDefinition.getName()));
		}

		List<WorkStatusResponseDTO> workFlowWorksStatusResponseDTOs = workFlowServiceDelegate
				.getWorkFlowAndWorksStatus(workFlowExecution, workFlowDefinition);
		return WorkFlowStatusResponseDTO.builder().workFlowExecutionId(workFlowExecution.getId())
				.workFlowName(workFlowDefinition.getName()).status(workFlowExecution.getStatus())
				.message(workFlowExecution.getMessage()).works(workFlowWorksStatusResponseDTOs)
				.restartedCount(workFlowRepository.countRestartedWorkflow(workFlowExecution.getId()))
				.originalExecutionId(Optional.ofNullable(workFlowExecution.getOriginalWorkFlowExecution())
						.orElse(new WorkFlowExecution()).getId())
				.build();
	}

	@Override
	public WorkFlowContextResponseDTO getWorkflowParameters(UUID workFlowExecutionId, List<Resource> params) {
		WorkFlowExecution workFlowExecution = workFlowRepository.findById(workFlowExecutionId).orElseThrow(() -> {
			throw new ResourceNotFoundException(ResourceType.WORKFLOW_EXECUTION, workFlowExecutionId);
		});
		Map options = Map.of();
		if (params.contains(Resource.WORKFLOW_OPTIONS)) {
			options = Optional.ofNullable(
					(Map) WorkContextDelegate.read(workFlowExecution.getWorkFlowExecutionContext().getWorkContext(),
							ProcessType.WORKFLOW_EXECUTION, Resource.WORKFLOW_OPTIONS))
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
		catch (DataAccessException e) {
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
			throw new ResourceNotFoundException(ResourceType.WORKFLOW_EXECUTION, workFlowExecutionId);
		});
		// get workflow checker task definition
		WorkFlowTaskDefinition workFlowTaskDefinition = workFlowTaskDefinitionRepository
				.findFirstByNameAndWorkFlowDefinitionType(workFlowTaskName, WorkFlowType.CHECKER);
		if (Objects.isNull(workFlowTaskDefinition)) {
			throw new ResourceNotFoundException(
					String.format("workflow checker task name: %s not found!", workFlowTaskName));
		}
		// find workflow checker execution associated to the checker task
		List<WorkFlowExecution> workFlowExecutions = workFlowRepository
				.findByMainWorkFlowExecution(mainWorkFlowExecution);
		WorkFlowExecution workFlowCheckerExecution = workFlowExecutions.stream()
				.filter(workFlowExecution -> workFlowExecution.getWorkFlowDefinition().getId()
						.equals(workFlowTaskDefinition.getWorkFlowDefinition().getId()))
				.max(Comparator.comparing(WorkFlowExecution::getStartDate)).orElseThrow(() -> {
					throw new IllegalWorkFlowStateException(String
							.format("workflow checker associated to task: %s has not started!", workFlowTaskName));
				});
		// get the workflow checker task execution
		WorkFlowTaskExecution workFlowTaskExecution = workFlowTaskRepository
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(workFlowCheckerExecution.getId(),
						workFlowTaskDefinition.getId())
				.stream().findFirst().orElseThrow(() -> {
					throw new IllegalWorkFlowStateException(
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
				.workFlowName(workflowExecution.getWorkFlowDefinition().getName())
				.workStatus(WorkStatus.valueOf(workflowExecution.getStatus().name()))
				.startDate(Optional.ofNullable(workflowExecution.getStartDate()).map(Date::toString).orElse(null))
				.endDate(Optional.ofNullable(workflowExecution.getEndDate()).map(Date::toString).orElse(null))
				.executeBy(workflowExecution.getUser().getUsername())
				.additionalInfos(Optional.ofNullable(workflowExecution.getWorkFlowExecutionContext())
						.flatMap(workFlowExecutionContext -> Optional
								.ofNullable(
										WorkContextUtils.getAdditionalInfo(workFlowExecutionContext.getWorkContext()))
								.map(additionalInfoMap -> additionalInfoMap.entrySet().stream()
										.sorted(Map.Entry.comparingByKey())
										.map(additionalInfo -> new WorkFlowResponseDTO.AdditionalInfo(
												additionalInfo.getKey(), additionalInfo.getValue()))
										.toList()))
						.orElse(null))
				.workFlowType(workflowExecution.getWorkFlowDefinition().getType()).build();
	}

}
