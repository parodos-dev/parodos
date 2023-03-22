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
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkStatusResponseDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Objects.isNull;

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

	private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private final WorkFlowRepository workFlowRepository;

	private final WorkFlowTaskRepository workFlowTaskRepository;

	private final WorkFlowWorkRepository workFlowWorkRepository;

	public WorkFlowServiceImpl(WorkFlowDelegate workFlowDelegate,
			WorkFlowDefinitionRepository workFlowDefinitionRepository,
			WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository, WorkFlowRepository workFlowRepository,
			WorkFlowTaskRepository workFlowTaskRepository, WorkFlowWorkRepository workFlowWorkRepository) {
		this.workFlowDelegate = workFlowDelegate;
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
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
	public synchronized WorkFlowExecution saveWorkFlow(UUID projectId, UUID workFlowDefinitionId,
			WorkFlowStatus workFlowStatus, WorkFlowExecution masterWorkFlowExecution) {
		return workFlowRepository.save(WorkFlowExecution.builder().workFlowDefinitionId(workFlowDefinitionId)
				.projectId(projectId).status(workFlowStatus).startDate(new Date())
				.masterWorkFlowExecution(masterWorkFlowExecution).build());
	}

	@Override
	public synchronized WorkFlowExecution updateWorkFlow(WorkFlowExecution workFlowExecution) {
		return workFlowRepository.save(workFlowExecution);
	}

	@Override
	public WorkFlowStatusResponseDTO getWorkFlowStatus(UUID workFlowExecutionId) {
		WorkFlowExecution masterWorkFlowExecution = workFlowRepository.findById(workFlowExecutionId).orElseThrow(() -> {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("workflow execution id: %s not found!", workFlowExecutionId));
		});

		WorkFlowDefinition masterWorkFlowDefinition = workFlowDefinitionRepository
				.findById(masterWorkFlowExecution.getWorkFlowDefinitionId()).orElseThrow(() -> {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND,
							String.format("workflow definition id: %s not found!", workFlowExecutionId));
				});

		CopyOnWriteArrayList<WorkStatusResponseDTO> workStatusResponseDTOs = new CopyOnWriteArrayList<>();
		Map<String, Integer> workFlowWorksStartIndex = new HashMap<>();

		workStatusResponseDTOs
				.add(WorkStatusResponseDTO.builder().workDefinitionId(masterWorkFlowExecution.getWorkFlowDefinitionId())
						.name(masterWorkFlowDefinition.getName()).type(WorkType.WORKFLOW)
						.status(WorkFlowStatus.IN_PROGRESS.equals(masterWorkFlowExecution.getStatus())
								? com.redhat.parodos.workflow.enums.WorkStatus.PENDING
								: com.redhat.parodos.workflow.enums.WorkStatus
										.valueOf(masterWorkFlowExecution.getStatus().name()))
						.numberOfWorks(masterWorkFlowDefinition.getNumberOfWorks()).works(new ArrayList<>()).build());
		workFlowWorksStartIndex.put(masterWorkFlowDefinition.getName(), 1);

		List<WorkFlowWorkDefinition> masterWorkFlowWorkDefinitions = workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(masterWorkFlowDefinition.getId());
		masterWorkFlowWorkDefinitions.forEach(workFlowWorkDefinition -> {
			if (workFlowWorkDefinition.getWorkDefinitionType().equals(WorkType.WORKFLOW)) {
				WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository
						.findById(workFlowWorkDefinition.getWorkDefinitionId()).get();
				WorkFlowExecution workExecution = workFlowRepository
						.findFirstByMasterWorkFlowExecutionAndWorkFlowDefinitionId(masterWorkFlowExecution,
								workFlowWorkDefinition.getWorkDefinitionId());
				/*
				 * the workflow execution might be null when there is pending checker
				 * before it
				 */
				com.redhat.parodos.workflow.enums.WorkStatus workStatus = workExecution == null
						|| WorkFlowStatus.IN_PROGRESS.equals(workExecution.getStatus())
								? com.redhat.parodos.workflow.enums.WorkStatus.PENDING
								: com.redhat.parodos.workflow.enums.WorkStatus
										.valueOf(workExecution.getStatus().name());
				workStatusResponseDTOs.add(
						WorkStatusResponseDTO.builder().workDefinitionId(workFlowWorkDefinition.getWorkDefinitionId())
								.name(workFlowDefinition.getName()).type(WorkType.WORKFLOW).status(workStatus)
								.numberOfWorks(workFlowDefinition.getNumberOfWorks()).works(new ArrayList<>()).build());
			}
			else {
				WorkFlowTaskDefinition workFlowTaskDefinition = workFlowTaskDefinitionRepository
						.findById(workFlowWorkDefinition.getWorkDefinitionId()).get();
				List<WorkFlowTaskExecution> workFlowTaskExecutions = workFlowTaskRepository
						.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(masterWorkFlowExecution.getId(),
								workFlowWorkDefinition.getWorkDefinitionId());
				Optional<WorkFlowTaskExecution> workFlowTaskExecutionOptional = workFlowTaskExecutions.stream()
						.max(Comparator.comparing(WorkFlowTaskExecution::getStartDate));
				com.redhat.parodos.workflow.enums.WorkStatus workStatus = com.redhat.parodos.workflow.enums.WorkStatus.PENDING;
				if (workFlowTaskExecutionOptional.isPresent()) {
					workStatus = WorkFlowTaskStatus.IN_PROGRESS.equals(workFlowTaskExecutionOptional.get().getStatus())
							? com.redhat.parodos.workflow.enums.WorkStatus.PENDING
							: com.redhat.parodos.workflow.enums.WorkStatus
									.valueOf(workFlowTaskExecutionOptional.get().getStatus().name());
				}
				workStatusResponseDTOs.add(
						WorkStatusResponseDTO.builder().workDefinitionId(workFlowWorkDefinition.getWorkDefinitionId())
								.name(workFlowTaskDefinition.getName()).type(WorkType.TASK).status(workStatus).build());
			}
		});

		for (int i = 1; i < workStatusResponseDTOs.size(); i++) {
			if (workStatusResponseDTOs.get(i).getType().equals(WorkType.WORKFLOW)) {
				List<WorkFlowWorkDefinition> tmpWorkFlowWorkDefinitions = workFlowWorkRepository
						.findByWorkFlowDefinitionIdOrderByCreateDateAsc(
								workStatusResponseDTOs.get(i).getWorkDefinitionId());
				workFlowWorksStartIndex.put(workStatusResponseDTOs.get(i).getName(), workStatusResponseDTOs.size());

				tmpWorkFlowWorkDefinitions.forEach(tmpWorkFlowWorkDefinition -> {
					if (tmpWorkFlowWorkDefinition.getWorkDefinitionType().equals(WorkType.WORKFLOW)) {
						WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository
								.findById(tmpWorkFlowWorkDefinition.getWorkDefinitionId()).get();
						WorkFlowExecution workExecution = workFlowRepository
								.findFirstByMasterWorkFlowExecutionAndWorkFlowDefinitionId(masterWorkFlowExecution,
										tmpWorkFlowWorkDefinition.getWorkDefinitionId());
						workStatusResponseDTOs.add(WorkStatusResponseDTO.builder().name(workFlowDefinition.getName())
								.workDefinitionId(tmpWorkFlowWorkDefinition.getWorkDefinitionId())
								.type(WorkType.WORKFLOW)
								.status(WorkFlowStatus.IN_PROGRESS.equals(workExecution.getStatus())
										? com.redhat.parodos.workflow.enums.WorkStatus.PENDING
										: com.redhat.parodos.workflow.enums.WorkStatus
												.valueOf(workExecution.getStatus().name()))
								.numberOfWorks(workFlowDefinition.getNumberOfWorks()).works(new ArrayList<>()).build());
					}
					else {
						WorkFlowTaskDefinition workFlowTaskDefinition = workFlowTaskDefinitionRepository
								.findById(tmpWorkFlowWorkDefinition.getWorkDefinitionId()).get();
						WorkFlowExecution workFlowExecution = workFlowRepository
								.findFirstByMasterWorkFlowExecutionAndWorkFlowDefinitionId(masterWorkFlowExecution,
										tmpWorkFlowWorkDefinition.getWorkFlowDefinition().getId());
						List<WorkFlowTaskExecution> workFlowTaskExecutions = workFlowExecution == null ? List.of()
								: workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(
										workFlowExecution.getId(), tmpWorkFlowWorkDefinition.getWorkDefinitionId());
						Optional<WorkFlowTaskExecution> workFlowTaskExecutionOptional = workFlowTaskExecutions.stream()
								.max(Comparator.comparing(WorkFlowTaskExecution::getStartDate));
						com.redhat.parodos.workflow.enums.WorkStatus workStatus = com.redhat.parodos.workflow.enums.WorkStatus.PENDING;
						if (workFlowTaskExecutionOptional.isPresent()) {
							workStatus = WorkFlowTaskStatus.IN_PROGRESS
									.equals(workFlowTaskExecutionOptional.get().getStatus())
											? com.redhat.parodos.workflow.enums.WorkStatus.PENDING
											: com.redhat.parodos.workflow.enums.WorkStatus
													.valueOf(workFlowTaskExecutionOptional.get().getStatus().name());
						}
						workStatusResponseDTOs
								.add(WorkStatusResponseDTO.builder().name(workFlowTaskDefinition.getName())
										.workDefinitionId(tmpWorkFlowWorkDefinition.getWorkDefinitionId())
										.type(WorkType.TASK).status(workStatus).build());
					}
				});
			}
		}

		for (int j = workStatusResponseDTOs.size() - 1; j >= 0; j--) {
			if (workStatusResponseDTOs.get(j).getType().equals(WorkType.WORKFLOW)) {
				List<WorkStatusResponseDTO> tmpList = new ArrayList<>();
				for (int k = workFlowWorksStartIndex
						.get(workStatusResponseDTOs.get(j).getName()); k < workFlowWorksStartIndex
								.get(workStatusResponseDTOs.get(j).getName())
								+ workStatusResponseDTOs.get(j).getNumberOfWorks(); k++) {
					tmpList.add(workStatusResponseDTOs.get(k));
				}
				workStatusResponseDTOs.get(j).setWorks(tmpList);
			}
		}

		return WorkFlowStatusResponseDTO.builder().workFlowExecutionId(masterWorkFlowExecution.getId().toString())
				.workFlowName(masterWorkFlowDefinition.getName()).status(masterWorkFlowExecution.getStatus().name())
				.works(workStatusResponseDTOs.get(0).getWorks()).build();
	}

	@Override
	public WorkFlowTaskExecution getWorkFlowTask(UUID workFlowExecutionId, UUID workFlowTaskDefinitionId) {
		List<WorkFlowTaskExecution> workFlowTaskExecutionList = workFlowTaskRepository
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(workFlowExecutionId, workFlowTaskDefinitionId);
		return (workFlowTaskExecutionList == null || workFlowTaskExecutionList.isEmpty()) ? null
				: workFlowTaskExecutionList.stream().findFirst().get();
	}

	@Override
	public synchronized WorkFlowTaskExecution saveWorkFlowTask(String arguments, UUID workFlowTaskDefinitionId,
			UUID workFlowExecutionId, WorkFlowTaskStatus workFlowTaskStatus) {
		return workFlowTaskRepository.save(WorkFlowTaskExecution.builder().workFlowExecutionId(workFlowExecutionId)
				.workFlowTaskDefinitionId(workFlowTaskDefinitionId).arguments(arguments).status(workFlowTaskStatus)
				.startDate(new Date()).build());
	}

	@Override
	public WorkFlowTaskExecution updateWorkFlowTask(WorkFlowTaskExecution workFlowTaskExecution) {
		return workFlowTaskRepository.save(workFlowTaskExecution);
	}

	@Override
	public void updateWorkFlowCheckerTaskStatus(UUID workFlowExecutionId, String workFlowTaskName,
			WorkFlowTaskStatus workFlowTaskStatus) {
		// get master workflow associated to the execution id
		WorkFlowExecution masterWorkFlowExecution = workFlowRepository.findById(workFlowExecutionId).orElseThrow(() -> {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("workflow execution id: %s not found!", workFlowExecutionId));
		});
		// get workflow checker task definition
		WorkFlowTaskDefinition workFlowTaskDefinition = workFlowTaskDefinitionRepository
				.findFirstByNameAndWorkFlowDefinitionType(workFlowTaskName, WorkFlowType.CHECKER);
		if (isNull(workFlowTaskDefinition)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("workflow checker task name: %s not found!", workFlowTaskName));
		}
		// find workflow checker execution associated to the checker task
		List<WorkFlowExecution> workFlowExecutions = workFlowRepository
				.findByMasterWorkFlowExecution(masterWorkFlowExecution);
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

	private String validateWorkflow(String workflowName, WorkFlow workFlow) {
		// validate if workflow exists
		if (workFlow == null) {
			log.error("workflow '{}' is not found!", workflowName);
			return String.format("workflow '%s' cannot be found!", workflowName);
		}

		// validate if workflow is master
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.findFirstByName(workflowName);
		if (workFlowDefinition == null) {
			return String.format("workflow '%s' is not registered!", workflowName);
		}

		if (!workFlowWorkRepository.findByWorkDefinitionId(workFlowDefinition.getId()).isEmpty()) {
			log.error("workflow '{}' is not master workflow!", workflowName);
			return String.format("workflow '%s' is not master workflow!", workflowName);
		}

		// TODO: validate required parameters from definition
		return null;
	}

}
