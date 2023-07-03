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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

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

	private void getWorksFromWorkDefinition(WorkDefinitionResponseDTO workflow,
			List<WorkFlowWorkDefinition> workFlowWorkDefinitions, Queue<WorkDefinitionResponseDTO> responseDTOs) {
		if (workflow.getWorks() == null) {
			// LinkedHashSet to keep insertion order
			workflow.setWorks(new LinkedHashSet<>());
		}
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
					WorkDefinitionResponseDTO work = WorkDefinitionResponseDTO.fromWorkFlowTaskDefinition(wdt.get());
					workflow.getWorks().add(work);
					responseDTOs.add(work);
				}
				case CHECKER -> {
					Optional<WorkFlowCheckerMappingDefinition> wcd = workFlowCheckerMappingDefinitionRepository
							.findById(workFlowWorkDefinition.getWorkDefinitionId());
					if (wcd.isEmpty()) {
						log.error("Cannot find the checker definition with id "
								+ workFlowWorkDefinition.getWorkDefinitionId());
						return;
					}
					WorkDefinitionResponseDTO work = WorkDefinitionResponseDTO
							.fromWorkFlowCheckerMappingDefinition(wcd.get());
					workflow.getWorks().add(work);
					responseDTOs.add(work);
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
					WorkDefinitionResponseDTO work = WorkDefinitionResponseDTO.fromWorkFlowDefinitionEntity(wd.get(),
							wdWorkFlowWorkDependencies);
					workflow.getWorks().add(work);
					responseDTOs.add(work);
				}
				default -> {
				}
			}
		});

	}

	private Set<WorkDefinitionResponseDTO> buildWorkFlowWorksDTOs(WorkFlowDefinition workFlowDefinition,
			List<WorkFlowWorkDefinition> workFlowWorkDefinitions) {
		Queue<WorkDefinitionResponseDTO> workDefinitionResponseDTOs = new LinkedList<>();
		WorkDefinitionResponseDTO rootWorkFlow = buildRootWorkFlow(workFlowDefinition, workFlowWorkDefinitions,
				workDefinitionResponseDTOs);

		// fill in subsequent workUnits
		// workDefinitionResponseDTOs.size() will grow as long as there are new works
		populateWorkFlowWorksRecursive(workDefinitionResponseDTOs);

		return rootWorkFlow.getWorks();
	}

	private WorkDefinitionResponseDTO buildRootWorkFlow(WorkFlowDefinition workFlowDefinition,
			List<WorkFlowWorkDefinition> workFlowWorkDefinitions,
			Queue<WorkDefinitionResponseDTO> workDefinitionResponseDTOs) {
		WorkDefinitionResponseDTO rootWorkFlow = WorkDefinitionResponseDTO.builder().id(workFlowDefinition.getId())
				.workType(WorkType.WORKFLOW).name(workFlowDefinition.getName())
				.parameterFromString(workFlowDefinition.getParameters())
				.processingType(workFlowDefinition.getProcessingType()).works(new LinkedHashSet<>())
				.numberOfWorkUnits(workFlowWorkDefinitions.size()).build();
		workDefinitionResponseDTOs.add(rootWorkFlow);

		return rootWorkFlow;
	}

	private void populateWorkFlowWorksRecursive(Queue<WorkDefinitionResponseDTO> workDefinitionResponseDTOs) {
		if (workDefinitionResponseDTOs.isEmpty()) {
			return;
		}
		WorkDefinitionResponseDTO workflow = workDefinitionResponseDTOs.poll();
		List<WorkFlowWorkDefinition> workFlowWorkUnitsDefinition = workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workflow.getId()).stream()
				.sorted(Comparator.comparing(WorkFlowWorkDefinition::getCreateDate)).toList();

		this.getWorksFromWorkDefinition(workflow, workFlowWorkUnitsDefinition, workDefinitionResponseDTOs);
		this.getCheckerWorkFromWorkDefinition(workflow, workDefinitionResponseDTOs);
		populateWorkFlowWorksRecursive(workDefinitionResponseDTOs);
	}

	private void getCheckerWorkFromWorkDefinition(WorkDefinitionResponseDTO workflow,
			Queue<WorkDefinitionResponseDTO> workDefinitionResponseDTOs) {
		if (workflow.getWorkFlowCheckerMappingDefinitionId() != null) {
			Optional<WorkFlowCheckerMappingDefinition> checkerMappingDefinition = workFlowCheckerMappingDefinitionRepository
					.findById(workflow.getWorkFlowCheckerMappingDefinitionId());
			if (checkerMappingDefinition.isPresent()) {
				WorkDefinitionResponseDTO checkerWorkflowDefinitionDTO = WorkDefinitionResponseDTO
						.fromWorkFlowCheckerMappingDefinition(checkerMappingDefinition.get());
				workDefinitionResponseDTOs.add(checkerWorkflowDefinitionDTO);
				if (workflow.getWorks() == null) {
					workflow.setWorks(new LinkedHashSet<>());
				}
				workflow.getWorks().add(checkerWorkflowDefinitionDTO);
			}
			else {
				log.error("WorkFlowCheckerMappingDefinition %s associated with WorkDefinition %s not found"
						.formatted(workflow.getWorkFlowCheckerMappingDefinitionId(), workflow.getId()));
			}
		}
	}

	public void cleanAllDefinitionMappings() {
		workFlowCheckerMappingDefinitionRepository.deleteAll();
		workFlowWorkRepository.deleteAll();
		workFlowDefinitionRepository.deleteAllFromFallbackMapping();
	}

}
