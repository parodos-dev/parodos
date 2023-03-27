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

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.ParodosWorkStatus;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkStatusResponseDTO;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Workflow execution service delegate
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
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

		// this is the master workflow so build its status dto and
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

		// nest works status dto into the (master) workflow status dto
		return nestWorkFlowWorksStatusDTO(workStatusResponseDTOs, workFlowWorkStartIndexMap);
	}

	private void buildWorkFlowStatusDTO(WorkFlowExecution workFlowExecution, WorkFlowDefinition workFlowDefinition,
			CopyOnWriteArrayList<WorkStatusResponseDTO> workStatusResponseDTOList,
			Map<String, Integer> workFlowWorkStartIndexMap) {
		// build workflow status DTO
		workStatusResponseDTOList.add(WorkStatusResponseDTO.builder().name(workFlowDefinition.getName())
				.type(WorkType.WORKFLOW)
				.status(WorkFlowStatus.IN_PROGRESS.equals(workFlowExecution.getStatus()) ? ParodosWorkStatus.PENDING
						: ParodosWorkStatus.valueOf(workFlowExecution.getStatus().name()))
				.workExecution(workFlowExecution).numberOfWorks(workFlowDefinition.getNumberOfWorks())
				.works(new ArrayList<>()).build());

		// save the start index of the workflow's works
		workFlowWorkStartIndexMap.put(workFlowDefinition.getName(), workStatusResponseDTOList.size());

		// build workflow's works status DTO
		List<WorkFlowWorkDefinition> workFlowWorkDefinitions = workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workFlowDefinition.getId());
		workFlowWorkDefinitions.forEach(workFlowWorkDefinition -> {
			if (workFlowWorkDefinition.getWorkDefinitionType().equals(WorkType.WORKFLOW))
				workStatusResponseDTOList
						.add(getWorkStatusResponseDTOFromWorkFlow(workFlowWorkDefinition, workFlowExecution));
			else
				workStatusResponseDTOList
						.add(getWorkStatusResponseDTOFromWorkFlowTask(workFlowWorkDefinition, workFlowExecution));
		});
	}

	private void buildWorkFlowWorksStatusDTO(String workFlowName, WorkFlowExecution workFlowExecution,
			WorkFlowExecution masterWorkFlowExecution,
			CopyOnWriteArrayList<WorkStatusResponseDTO> workStatusResponseDTOList,
			Map<String, Integer> workFlowWorkStartIndexMap) {
		// save the start index of the workflow's works
		workFlowWorkStartIndexMap.put(workFlowName, workStatusResponseDTOList.size());

		// get workflow's works definition to find each status
		List<WorkFlowWorkDefinition> workFlowWorksDefinitions = workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workFlowExecution.getWorkFlowDefinitionId());

		workFlowWorksDefinitions.forEach(workFlowWorkDefinition -> {
			if (workFlowWorkDefinition.getWorkDefinitionType().equals(WorkType.WORKFLOW))
				workStatusResponseDTOList
						.add(getWorkStatusResponseDTOFromWorkFlow(workFlowWorkDefinition, masterWorkFlowExecution));
			else
				workStatusResponseDTOList
						.add(getWorkStatusResponseDTOFromWorkFlowTask(workFlowWorkDefinition, workFlowExecution));
		});
	}

	private WorkStatusResponseDTO getWorkStatusResponseDTOFromWorkFlow(WorkFlowWorkDefinition workFlowWorkDefinition,
			WorkFlowExecution workFlowExecution) {
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository
				.findById(workFlowWorkDefinition.getWorkDefinitionId()).get();

		WorkFlowExecution workExecution = workFlowRepository.findFirstByMasterWorkFlowExecutionAndWorkFlowDefinitionId(
				workFlowExecution, workFlowWorkDefinition.getWorkDefinitionId());
		/*
		 * the workflow execution might be null when there is pending checker before it
		 */
		ParodosWorkStatus workStatus = workExecution == null
				|| WorkFlowStatus.IN_PROGRESS.equals(workExecution.getStatus()) ? ParodosWorkStatus.PENDING
						: ParodosWorkStatus.valueOf(workExecution.getStatus().name());

		return WorkStatusResponseDTO.builder().name(workFlowDefinition.getName()).type(WorkType.WORKFLOW)
				.status(workStatus).works(new ArrayList<>()).workExecution(workExecution)
				.numberOfWorks(workFlowDefinition.getNumberOfWorks()).build();
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

		ParodosWorkStatus workStatus = ParodosWorkStatus.PENDING;

		if (workFlowTaskExecutionOptional.isPresent()) {
			workStatus = WorkFlowTaskStatus.IN_PROGRESS.equals(workFlowTaskExecutionOptional.get().getStatus())
					? ParodosWorkStatus.PENDING
					: ParodosWorkStatus.valueOf(workFlowTaskExecutionOptional.get().getStatus().name());
		}

		return WorkStatusResponseDTO.builder().name(workFlowTaskDefinition.getName()).type(WorkType.TASK)
				.status(workStatus).build();
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
