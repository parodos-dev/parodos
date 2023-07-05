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
package com.redhat.parodos.workflow.definition.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.parodos.common.entity.AbstractEntity;
import com.redhat.parodos.common.exceptions.IDType;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.common.exceptions.ResourceType;
import com.redhat.parodos.workflow.definition.dto.WorkDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowPropertiesDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowCheckerMappingDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import com.redhat.parodos.workflows.workflow.WorkFlowPropertiesMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Service;

/**
 * workflow definition service implementation
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
@Service
public class WorkFlowDefinitionServiceImpl implements WorkFlowDefinitionService {

	private final WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private final WorkFlowCheckerMappingDefinitionRepository workFlowCheckerMappingDefinitionRepository;

	private final WorkFlowWorkRepository workFlowWorkRepository;

	private final ModelMapper modelMapper;

	public WorkFlowDefinitionServiceImpl(WorkFlowDefinitionRepository workFlowDefinitionRepository,
			WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository,
			WorkFlowCheckerMappingDefinitionRepository workFlowCheckerMappingDefinitionRepository,
			WorkFlowWorkRepository workFlowWorkRepository, ModelMapper modelMapper) {
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
		this.workFlowCheckerMappingDefinitionRepository = workFlowCheckerMappingDefinitionRepository;
		this.workFlowWorkRepository = workFlowWorkRepository;
		this.modelMapper = modelMapper;
	}

	private HashMap<String, Map<String, Object>> convertWorkParameters(List<WorkParameter> workParameters) {
		HashMap<String, Map<String, Object>> result = new HashMap<>();
		for (WorkParameter workParameter : workParameters) {
			if (workParameter == null) {
				continue;
			}

			result.put(workParameter.getKey(), workParameter.getAsJsonSchema());
		}
		return result;
	}

	@Override
	public WorkFlowDefinitionResponseDTO save(String workFlowName, WorkFlowType workFlowType,
			WorkFlowPropertiesMetadata properties, List<WorkParameter> workParameters, List<Work> works,
			WorkFlowProcessingType workFlowProcessingType, String fallbackWorkFlowName) {

		String stringifyParameters = WorkFlowDTOUtil.writeObjectValueAsString(convertWorkParameters(workParameters));

		// set and save workflow definition
		WorkFlowPropertiesDefinition propertiesDefinition = WorkFlowPropertiesDefinition.builder().build();
		if (properties != null) {
			propertiesDefinition.setVersion(properties.getVersion());
		}

		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.findFirstByName(workFlowName);
		if (workFlowDefinition == null) {
			// It's not created, so we created the minimal entity to update it
			workFlowDefinition = WorkFlowDefinition.builder().name(workFlowName).createDate(new Date()).build();
		}

		workFlowDefinition.setType(workFlowType);
		workFlowDefinition.setParameters(stringifyParameters);
		workFlowDefinition.setModifyDate(new Date());
		workFlowDefinition.setProperties(propertiesDefinition);
		workFlowDefinition.setProcessingType(workFlowProcessingType);
		workFlowDefinition.setNumberOfWorks(works.size());
		if (!StringUtils.isEmpty(fallbackWorkFlowName)) {
			workFlowDefinition
					.setFallbackWorkFlowDefinition(workFlowDefinitionRepository.findFirstByName(fallbackWorkFlowName));
		}

		workFlowDefinition = workFlowDefinitionRepository.save(workFlowDefinition);

		// save workflow tasks and set works
		List<WorkFlowWorkDefinition> workFlowWorkDefinitions = new ArrayList<>();
		WorkFlowDefinition finalWorkFlowDefinition = workFlowDefinition;
		works.forEach(work -> {
			UUID workId = null;
			if (work instanceof WorkFlow) { // WorkFlow
				// A workflow in works should already been stored
				workId = workFlowDefinitionRepository.findFirstByName(work.getName()).getId();
				workFlowWorkDefinitions.add(
						WorkFlowWorkDefinition.builder().workDefinitionId(workId).workDefinitionType(WorkType.WORKFLOW)
								.workFlowDefinition(finalWorkFlowDefinition).createDate(new Date()).build());
			}
			else { // WorkFlowTask
				WorkFlowTask workFlowTask = (WorkFlowTask) work;
				String taskParameters = WorkFlowDTOUtil.writeObjectValueAsString(workFlowTask.getAsJsonSchema());
				String taskOutputs = WorkFlowDTOUtil.writeObjectValueAsString(workFlowTask.getWorkFlowTaskOutputs());
				Optional<WorkFlowTaskDefinition> workFlowTaskDefinitionOptional = Optional
						.ofNullable(workFlowTaskDefinitionRepository.findFirstByName(workFlowTask.getName()));
				if (workFlowTaskDefinitionOptional.filter(workFlowTaskDefinitionItem -> finalWorkFlowDefinition
						.getName().equals(workFlowTaskDefinitionItem.getWorkFlowDefinition().getName())
						&& taskParameters.equals(workFlowTaskDefinitionItem.getParameters())
						&& taskOutputs.equals(workFlowTaskDefinitionItem.getOutputs())).isEmpty()) {
					workId = workFlowTaskDefinitionRepository
							.save(workFlowTaskDefinitionOptional.map(foundWorkflowTaskDefinition -> {
								foundWorkflowTaskDefinition.setParameters(taskParameters);
								foundWorkflowTaskDefinition.setModifyDate(new Date());
								foundWorkflowTaskDefinition.setOutputs(taskOutputs);
								foundWorkflowTaskDefinition.setWorkFlowDefinition(finalWorkFlowDefinition);
								return foundWorkflowTaskDefinition;
							}).orElse(WorkFlowTaskDefinition.builder().name(workFlowTask.getName())
									.parameters(
											WorkFlowDTOUtil.writeObjectValueAsString(workFlowTask.getAsJsonSchema()))
									.outputs(WorkFlowDTOUtil
											.writeObjectValueAsString(workFlowTask.getWorkFlowTaskOutputs()))
									.workFlowDefinition(finalWorkFlowDefinition).createDate(new Date())
									.modifyDate(new Date()).build()))
							.getId();
				}
				workFlowWorkDefinitions.add(WorkFlowWorkDefinition.builder()
						.workDefinitionId(workFlowTaskDefinitionOptional.map(AbstractEntity::getId).orElse(workId))
						.workDefinitionType(WorkType.TASK).workFlowDefinition(finalWorkFlowDefinition)
						.createDate(new Date()).build());
			}
		});

		workFlowDefinition.setWorkFlowWorkDefinitions(workFlowWorkDefinitions);
		return modelMapper.map(workFlowDefinitionRepository.save(workFlowDefinition),
				WorkFlowDefinitionResponseDTO.class);
	}

	@Override
	public List<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitions() {
		List<WorkFlowDefinitionResponseDTO> workFlowDefinitionResponseDTOs = new ArrayList<>();
		workFlowDefinitionRepository.findByTypeIsNot(WorkFlowType.CHECKER).forEach(workFlowDefinition -> {
			workFlowDefinitionResponseDTOs.add(WorkFlowDefinitionResponseDTO.fromEntity(workFlowDefinition,
					buildWorkFlowWorksDTOs(workFlowDefinition, workFlowWorkRepository
							.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workFlowDefinition.getId()))));
		});
		return workFlowDefinitionResponseDTOs;
	}

	@Override
	public WorkFlowDefinitionResponseDTO getWorkFlowDefinitionById(UUID id) {
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.WORKFLOW_DEFINITION, id));
		List<WorkFlowWorkDefinition> workFlowWorkDependencies = workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workFlowDefinition.getId()).stream()
				.sorted(Comparator.comparing(WorkFlowWorkDefinition::getCreateDate)).toList();
		return WorkFlowDefinitionResponseDTO.fromEntity(workFlowDefinition,
				buildWorkFlowWorksDTOs(workFlowDefinition, workFlowWorkDependencies));
	}

	@Override
	public WorkFlowDefinitionResponseDTO getWorkFlowDefinitionByName(String name) {
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.findFirstByName(name);
		if (null == workFlowDefinition) {
			throw new ResourceNotFoundException(ResourceType.WORKFLOW_DEFINITION, IDType.NAME, name);
		}
		List<WorkFlowWorkDefinition> workFlowWorkDependencies = workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workFlowDefinition.getId()).stream()
				.sorted(Comparator.comparing(WorkFlowWorkDefinition::getCreateDate)).toList();

		return WorkFlowDefinitionResponseDTO.fromEntity(workFlowDefinition,
				buildWorkFlowWorksDTOs(workFlowDefinition, workFlowWorkDependencies));
	}

	@Override
	public void saveWorkFlowChecker(String workFlowTaskName, String workFlowCheckerName,
			WorkFlowCheckerDTO workFlowCheckerDTO) {
		try {
			WorkFlowTaskDefinition workFlowTaskDefinitionEntity = workFlowTaskDefinitionRepository
					.findFirstByName(workFlowTaskName);
			WorkFlowDefinition checkerWorkFlowDefinitionEntity = workFlowDefinitionRepository
					.findFirstByName(workFlowCheckerName);
			WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition = Optional
					.ofNullable(workFlowCheckerMappingDefinitionRepository
							.findFirstByCheckWorkFlow(checkerWorkFlowDefinitionEntity))
					.orElse(WorkFlowCheckerMappingDefinition.builder().checkWorkFlow(checkerWorkFlowDefinitionEntity)
							.cronExpression(workFlowCheckerDTO.getCronExpression()).tasks(new ArrayList<>()).build());
			workFlowTaskDefinitionEntity.setWorkFlowCheckerMappingDefinition(workFlowCheckerMappingDefinition);
			workFlowTaskDefinitionRepository.save(workFlowTaskDefinitionEntity);
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	@Override
	public Map<String, Object> getWorkParametersByWorkName(String workName) {
		return Optional.ofNullable(workFlowTaskDefinitionRepository.findFirstByName(workName))
				.map(WorkFlowTaskDefinition::getParameters).map(stringifyParameters -> WorkFlowDTOUtil
						.readStringAsObject(stringifyParameters, new TypeReference<Map<String, Object>>() {
						}, null))
				.orElse(Optional.ofNullable(workFlowDefinitionRepository.findFirstByName(workName))
						.map(WorkFlowDefinition::getParameters).map(stringifyParameters -> WorkFlowDTOUtil
								.readStringAsObject(stringifyParameters, new TypeReference<Map<String, Object>>() {
								}, null))
						.orElse(null));
	}

	@Override
	public WorkFlowDefinition getParentWorkFlowByWorkName(String workName) {
		UUID workId = Optional.ofNullable(workFlowDefinitionRepository.findFirstByName(workName))
				.map(AbstractEntity::getId)
				.orElse(Optional.ofNullable(workFlowTaskDefinitionRepository.findFirstByName(workName))
						.map(AbstractEntity::getId).orElse(null));
		return workId == null ? null : Optional.ofNullable(workFlowWorkRepository.findFirstByWorkDefinitionId(workId))
				.map(WorkFlowWorkDefinition::getWorkFlowDefinition).orElse(null);
	}

	private void getWorksFromWorkDefinition(List<WorkFlowWorkDefinition> workFlowWorkDefinitions,
			CopyOnWriteArrayList<WorkDefinitionResponseDTO> responseDTOs) {
		workFlowWorkDefinitions.forEach(workFlowWorkDefinition -> {

			WorkType workType = workFlowWorkDefinition.getWorkDefinitionType();
			if (workType == null) {
				return;
			}
			switch (workType) {
				case TASK -> {
					Optional<WorkFlowTaskDefinition> wdt = workFlowTaskDefinitionRepository
							.findById(workFlowWorkDefinition.getWorkDefinitionId());
					if (wdt.isEmpty()) {
						log.error("Cannot find the task definition with id "
								+ workFlowWorkDefinition.getWorkDefinitionId());
						return;
					}
					responseDTOs.add(WorkDefinitionResponseDTO.fromWorkFlowTaskDefinition(wdt.get()));
				}
				case WORKFLOW -> {
					Optional<WorkFlowDefinition> wd = workFlowDefinitionRepository
							.findById(workFlowWorkDefinition.getWorkDefinitionId());
					if (wd.isEmpty()) {
						log.error("Cannot find work flow definition with id {}",
								workFlowWorkDefinition.getWorkDefinitionId());
						return;
					}
					List<WorkFlowWorkDefinition> wdWorkFlowWorkDependencies = workFlowWorkRepository
							.findByWorkFlowDefinitionIdOrderByCreateDateAsc(wd.get().getId());
					responseDTOs.add(WorkDefinitionResponseDTO.fromWorkFlowDefinitionEntity(wd.get(),
							wdWorkFlowWorkDependencies));
				}
				default -> {
				}
			}
		});

	}

	private List<WorkDefinitionResponseDTO> buildWorkFlowWorksDTOs(WorkFlowDefinition workFlowDefinition,
			List<WorkFlowWorkDefinition> workFlowWorkDefinitions) {
		CopyOnWriteArrayList<WorkDefinitionResponseDTO> workDefinitionResponseDTOs = new CopyOnWriteArrayList<>();
		Map<String, Integer> workFlowWorksStartIndex = new HashMap<>();

		// add workflow
		workDefinitionResponseDTOs
				.add(WorkDefinitionResponseDTO.builder().id(workFlowDefinition.getId()).workType(WorkType.WORKFLOW)
						.name(workFlowDefinition.getName()).parameterFromString(workFlowDefinition.getParameters())
						.processingType(workFlowDefinition.getProcessingType()).works(new ArrayList<>())
						.numberOfWorkUnits(workFlowWorkDefinitions.size()).build());
		workFlowWorksStartIndex.put(workFlowDefinition.getName(), 1);

		// add workflowWorkUnits
		this.getWorksFromWorkDefinition(workFlowWorkDefinitions, workDefinitionResponseDTOs);

		// fill in subsequent workUnits
		// this responseSize is like this because we modify the size of the
		// workDefinitionResponseDTO
		for (int i = 1; i < workDefinitionResponseDTOs.size(); i++) {
			if (workDefinitionResponseDTOs.get(i).getWorkType() == WorkType.WORKFLOW) {
				workFlowWorksStartIndex.put(workDefinitionResponseDTOs.get(i).getName(),
						workDefinitionResponseDTOs.size());

				List<WorkFlowWorkDefinition> workFlowWorkUnits1Definition = workFlowWorkRepository
						.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workDefinitionResponseDTOs.get(i).getId())
						.stream().sorted(Comparator.comparing(WorkFlowWorkDefinition::getCreateDate)).toList();
				this.getWorksFromWorkDefinition(workFlowWorkUnits1Definition, workDefinitionResponseDTOs);
			}
		}

		for (int j = workDefinitionResponseDTOs.size() - 1; j >= 0; j--) {
			if (workDefinitionResponseDTOs.get(j).getWorkType() == WorkType.WORKFLOW) {
				List<WorkDefinitionResponseDTO> tmpList = new ArrayList<>();
				for (int k = workFlowWorksStartIndex.get(workDefinitionResponseDTOs.get(j)
						.getName()); k < workFlowWorksStartIndex.get(workDefinitionResponseDTOs.get(j).getName())
								+ workDefinitionResponseDTOs.get(j).getNumberOfWorkUnits()
								&& k < workDefinitionResponseDTOs.size(); k++) {

					tmpList.add(workDefinitionResponseDTOs.get(k));
				}
				workDefinitionResponseDTOs.get(j).setWorks(tmpList);
			}
		}
		return workDefinitionResponseDTOs.get(0).getWorks();
	}

	public void cleanAllDefinitionMappings() {
		workFlowCheckerMappingDefinitionRepository.deleteAll();
		workFlowWorkRepository.deleteAll();
		workFlowDefinitionRepository.deleteAllFromFallbackMapping();
	}

}
