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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkStatusResponseDTO;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflows.work.WorkStatus;

import org.springframework.stereotype.Service;

/**
 * Workflow execution service delegate
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Service
public class WorkFlowServiceDelegate {

	private final WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private final WorkFlowRepository workFlowRepository;

	private final WorkFlowTaskRepository workFlowTaskRepository;

	private final WorkFlowWorkRepository workFlowWorkRepository;

	public WorkFlowServiceDelegate(WorkFlowDefinitionRepository workFlowDefinitionRepository,
			WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository, WorkFlowRepository workFlowRepository,
			WorkFlowTaskRepository workFlowTaskRepository, WorkFlowWorkRepository workFlowWorkRepository) {
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
		this.workFlowRepository = workFlowRepository;
		this.workFlowTaskRepository = workFlowTaskRepository;
		this.workFlowWorkRepository = workFlowWorkRepository;
	}

	public List<WorkStatusResponseDTO> getWorkFlowAndWorksStatus(WorkFlowExecution workFlowExecution,
			WorkFlowDefinition workFlowDefinition) {
		CopyOnWriteArrayList<WorkStatusResponseDTO> workStatusResponseDTOs = new CopyOnWriteArrayList<>();
		Map<String, Integer> workFlowWorkStartIndexMap = new HashMap<>();

		// this is the main workflow so build its status dto and
		// its immediate works status dto then add into a common list
		buildWorkFlowStatusDTO(workFlowExecution, workFlowDefinition, workStatusResponseDTOs,
				workFlowWorkStartIndexMap);

		// continue building work status dto from the works in the common list
		for (int i = 1; i < workStatusResponseDTOs.size(); i++) {
			if (workStatusResponseDTOs.get(i).getType().equals(WorkType.WORKFLOW)
					&& workStatusResponseDTOs.get(i).getWorkExecution() != null) {
				buildWorkFlowWorksStatusDTO(workStatusResponseDTOs.get(i).getName(),
						workStatusResponseDTOs.get(i).getWorkExecution(), workFlowExecution, workStatusResponseDTOs,
						workFlowWorkStartIndexMap);
			}
		}

		// nest works status dto into the (main) workflow status dto
		return nestWorkFlowWorksStatusDTO(workStatusResponseDTOs, workFlowWorkStartIndexMap);
	}

	private void buildWorkFlowStatusDTO(WorkFlowExecution workFlowExecution, WorkFlowDefinition workFlowDefinition,
			CopyOnWriteArrayList<WorkStatusResponseDTO> workStatusResponseDTOList,
			Map<String, Integer> workFlowWorkStartIndexMap) {
		// build workflow status DTO
		workStatusResponseDTOList
				.add(WorkStatusResponseDTO.builder().name(workFlowDefinition.getName()).type(WorkType.WORKFLOW)
						.status(WorkStatus.IN_PROGRESS.equals(workFlowExecution.getStatus()) ? WorkStatus.PENDING
								: WorkStatus.valueOf(workFlowExecution.getStatus().name()))
						.message(workFlowExecution.getMessage()).workExecution(workFlowExecution)
						.numberOfWorks(workFlowDefinition.getNumberOfWorks()).works(new ArrayList<>()).build());

		// save the start index of the workflow's works
		workFlowWorkStartIndexMap.put(workFlowDefinition.getName(), workStatusResponseDTOList.size());

		// build workflow's works status DTO
		List<WorkFlowWorkDefinition> workFlowWorkDefinitions = workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workFlowDefinition.getId());
		workFlowWorkDefinitions.forEach(workFlowWorkDefinition -> {
			if (workFlowWorkDefinition.getWorkDefinitionType().equals(WorkType.WORKFLOW)) {
				workStatusResponseDTOList
						.add(getWorkStatusResponseDTOFromWorkFlow(workFlowWorkDefinition, workFlowExecution));
			}
			else {
				workStatusResponseDTOList
						.add(getWorkStatusResponseDTOFromWorkFlowTask(workFlowWorkDefinition, workFlowExecution));
			}
		});
	}

	private void buildWorkFlowWorksStatusDTO(String workFlowName, WorkFlowExecution workFlowExecution,
			WorkFlowExecution mainWorkFlowExecution,
			CopyOnWriteArrayList<WorkStatusResponseDTO> workStatusResponseDTOList,
			Map<String, Integer> workFlowWorkStartIndexMap) {
		// save the start index of the workflow's works
		workFlowWorkStartIndexMap.put(workFlowName, workStatusResponseDTOList.size());

		// get workflow's works definition to find each status
		List<WorkFlowWorkDefinition> workFlowWorksDefinitions = workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workFlowExecution.getWorkFlowDefinition().getId());

		workFlowWorksDefinitions.forEach(workFlowWorkDefinition -> {
			if (workFlowWorkDefinition.getWorkDefinitionType().equals(WorkType.WORKFLOW)) {
				workStatusResponseDTOList
						.add(getWorkStatusResponseDTOFromWorkFlow(workFlowWorkDefinition, mainWorkFlowExecution));
			}
			else {
				workStatusResponseDTOList
						.add(getWorkStatusResponseDTOFromWorkFlowTask(workFlowWorkDefinition, workFlowExecution));
			}
		});
	}

	private WorkStatusResponseDTO getWorkStatusResponseDTOFromWorkFlow(WorkFlowWorkDefinition workFlowWorkDefinition,
			WorkFlowExecution workFlowExecution) {
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository
				.findById(workFlowWorkDefinition.getWorkDefinitionId()).get();

		WorkFlowExecution workExecution = workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(
				workFlowWorkDefinition.getWorkDefinitionId(), workFlowExecution);

		/*
		 * the workflow execution might be null when there is pending checker before it
		 */
		WorkStatus workStatus;
		String message = null;
		if (workExecution == null) {
			workStatus = WorkStatus.PENDING;
		}
		else {
			workStatus = WorkStatus.valueOf(workExecution.getStatus().name());
			message = workExecution.getMessage();
		}

		return WorkStatusResponseDTO.builder().name(workFlowDefinition.getName()).type(WorkType.WORKFLOW)
				.status(workStatus).works(new ArrayList<>()).workExecution(workExecution)
				.numberOfWorks(workFlowDefinition.getNumberOfWorks()).message(message).build();
	}

	private WorkStatusResponseDTO getWorkStatusResponseDTOFromWorkFlowTask(
			WorkFlowWorkDefinition workFlowWorkDefinition, WorkFlowExecution workFlowExecution) {
		WorkFlowTaskDefinition workFlowTaskDefinition = workFlowTaskDefinitionRepository
				.findById(workFlowWorkDefinition.getWorkDefinitionId()).get();

		List<WorkFlowTaskExecution> workFlowTaskExecutions = workFlowTaskRepository
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(workFlowExecution.getId(),
						workFlowWorkDefinition.getWorkDefinitionId());

		Optional<WorkFlowTaskExecution> workFlowTaskExecutionOptional = workFlowTaskExecutions.stream()
				.max(Comparator.comparing(WorkFlowTaskExecution::getStartDate));

		WorkStatus workStatus = WorkStatus.PENDING;
		String message = null;
		String alertMessage = null;

		if (workFlowTaskExecutionOptional.isPresent()) {
			message = workFlowTaskExecutionOptional.get().getMessage();
			alertMessage = workFlowTaskExecutionOptional.get().getAlertMessage();
			workStatus = WorkStatus.valueOf(workFlowTaskExecutionOptional.get().getStatus().name());
			if (workFlowTaskDefinition.getWorkFlowCheckerMappingDefinition() != null) {
				workStatus = Optional
						.ofNullable(workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(
								workFlowTaskDefinition.getWorkFlowCheckerMappingDefinition().getCheckWorkFlow().getId(),
								Optional.ofNullable(workFlowExecution.getMainWorkFlowExecution())
										.orElse(workFlowExecution)))
						.map(WorkFlowExecution::getStatus)
						.map(checkerStatus -> WorkStatus.FAILED.equals(checkerStatus) ? WorkStatus.IN_PROGRESS
								: WorkStatus.valueOf(checkerStatus.name()))
						.orElse(WorkStatus.COMPLETED.equals(workStatus) ? WorkStatus.IN_PROGRESS : workStatus);
			}
		}

		return WorkStatusResponseDTO.builder().name(workFlowTaskDefinition.getName()).type(WorkType.TASK)
				.status(workStatus).message(message).alertMessage(alertMessage).build();
	}

	private List<WorkStatusResponseDTO> nestWorkFlowWorksStatusDTO(
			CopyOnWriteArrayList<WorkStatusResponseDTO> workStatusResponseDTOs,
			Map<String, Integer> workFlowWorksStartIndexMap) {
		for (int j = workStatusResponseDTOs.size() - 1; j >= 0; j--) {
			if (workStatusResponseDTOs.get(j).getType().equals(WorkType.WORKFLOW)
					&& workStatusResponseDTOs.get(j).getWorkExecution() != null) {
				List<WorkStatusResponseDTO> tmpList = new ArrayList<>();
				for (int k = workFlowWorksStartIndexMap
						.get(workStatusResponseDTOs.get(j).getName()); k < workFlowWorksStartIndexMap
								.get(workStatusResponseDTOs.get(j).getName())
								+ workStatusResponseDTOs.get(j).getNumberOfWorks(); k++) {
					tmpList.add(workStatusResponseDTOs.get(k));
				}
				workStatusResponseDTOs.get(j).setWorks(tmpList);
			}
		}
		return workStatusResponseDTOs.get(0).getWorks();
	}

}
