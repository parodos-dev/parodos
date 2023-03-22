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

import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.definition.dto.WorkDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowCheckerMappingDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.parameter.WorkFlowParameter;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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

	private HashMap<String, Map<String, String>> convertWorkFlowParameters(List<WorkFlowParameter> workFlowParameters) {
		HashMap<String, Map<String, String>> result = new HashMap<>();
		for (WorkFlowParameter workFlowParameter : workFlowParameters) {
			if (workFlowParameter == null)
				continue;

			result.put(workFlowParameter.getKey(), workFlowParameter.getAsJsonSchema());
		}
		return result;
	}

	@Override
	public WorkFlowDefinitionResponseDTO save(String workFlowName, WorkFlowType workFlowType,
			List<WorkFlowParameter> workFlowParameters, List<Work> works,
			WorkFlowProcessingType workFlowProcessingType) {

		// set and save workflow definition
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.save(WorkFlowDefinition.builder()
				.name(workFlowName).type(workFlowType).createDate(new Date())
				.parameters(WorkFlowDTOUtil.writeObjectValueAsString(convertWorkFlowParameters(workFlowParameters)))
				.modifyDate(new Date()).numberOfWorks(works.size()).processingType(workFlowProcessingType.name())
				.build());
		// save workflow tasks and set works
		List<WorkFlowWorkDefinition> workFlowWorkDefinitions = new ArrayList<>();
		works.forEach(work -> {
			UUID workId;
			if (work instanceof WorkFlow) { // WorkFlow
				// A workflow in works should already been stored
				workId = workFlowDefinitionRepository.findFirstByName(work.getName()).getId();
				workFlowWorkDefinitions.add(
						WorkFlowWorkDefinition.builder().workDefinitionId(workId).workDefinitionType(WorkType.WORKFLOW)
								.workFlowDefinition(workFlowDefinition).createDate(new Date()).build());
			}
			else { // WorkFlowTask
				WorkFlowTask workFlowTask = (WorkFlowTask) work;
				workId = workFlowTaskDefinitionRepository.save(WorkFlowTaskDefinition.builder()
						.name(workFlowTask.getName())
						.parameters(WorkFlowDTOUtil.writeObjectValueAsString(workFlowTask.getAsJsonSchema()))
						.outputs(WorkFlowDTOUtil.writeObjectValueAsString(workFlowTask.getWorkFlowTaskOutputs()))
						.workFlowDefinition(workFlowDefinition).createDate(new Date()).modifyDate(new Date()).build())
						.getId();
				workFlowWorkDefinitions
						.add(WorkFlowWorkDefinition.builder().workDefinitionId(workId).workDefinitionType(WorkType.TASK)
								.workFlowDefinition(workFlowDefinition).createDate(new Date()).build());
			}
		});
		workFlowDefinition.setWorkFlowWorkDefinitions(workFlowWorkDefinitions);
		return modelMapper.map(workFlowDefinitionRepository.save(workFlowDefinition),
				WorkFlowDefinitionResponseDTO.class);
	}

	@Override
	public List<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitions() {
		List<WorkFlowDefinitionResponseDTO> workFlowDefinitionResponseDTOs = new ArrayList<>();
		workFlowDefinitionRepository.findByTypeIsNot(WorkFlowType.CHECKER)
				.forEach(
						workFlowDefinition -> workFlowDefinitionResponseDTOs.add(WorkFlowDefinitionResponseDTO.builder()
								.id(workFlowDefinition.getId()).name(workFlowDefinition.getName())
								.parameterFromString(workFlowDefinition.getParameters())
								.author(workFlowDefinition.getAuthor()).createDate(workFlowDefinition.getCreateDate())
								.modifyDate(workFlowDefinition.getModifyDate())
								.type(workFlowDefinition.getType().name())
								.processingType(workFlowDefinition.getProcessingType())
								.works(buildWorkFlowWorksDTOs(workFlowDefinition, workFlowWorkRepository
										.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workFlowDefinition.getId())))
								.build()));
		return workFlowDefinitionResponseDTOs;
	}

	@Override
	public WorkFlowDefinitionResponseDTO getWorkFlowDefinitionById(UUID id) {
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.findById(id)
				.orElseThrow(() -> new RuntimeException(String.format("Workflow definition id %s not found", id)));
		List<WorkFlowWorkDefinition> workFlowWorkDependencies = workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workFlowDefinition.getId()).stream()
				.sorted(Comparator.comparing(WorkFlowWorkDefinition::getCreateDate)).collect(Collectors.toList());
		return WorkFlowDefinitionResponseDTO.builder().id(workFlowDefinition.getId()).name(workFlowDefinition.getName())
				.parameterFromString(workFlowDefinition.getParameters()).author(workFlowDefinition.getAuthor())
				.createDate(workFlowDefinition.getCreateDate()).modifyDate(workFlowDefinition.getModifyDate())
				.type(workFlowDefinition.getType().toString()).processingType(workFlowDefinition.getProcessingType())
				.works(buildWorkFlowWorksDTOs(workFlowDefinition, workFlowWorkDependencies)).build();
	}

	@Override
	public WorkFlowDefinitionResponseDTO getWorkFlowDefinitionByName(String name) {
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.findFirstByName(name);
		if (null == workFlowDefinition) {
			throw new RuntimeException(String.format("Workflow definition name %s not found", name));
		}
		List<WorkFlowWorkDefinition> workFlowWorkDependencies = workFlowWorkRepository
				.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workFlowDefinition.getId()).stream()
				.sorted(Comparator.comparing(WorkFlowWorkDefinition::getCreateDate)).collect(Collectors.toList());
		return WorkFlowDefinitionResponseDTO.builder().id(workFlowDefinition.getId()).name(workFlowDefinition.getName())
				.parameterFromString(workFlowDefinition.getParameters()).author(workFlowDefinition.getAuthor())
				.createDate(workFlowDefinition.getCreateDate()).modifyDate(workFlowDefinition.getModifyDate())
				.type(workFlowDefinition.getType().name()).processingType(workFlowDefinition.getProcessingType())
				.works(buildWorkFlowWorksDTOs(workFlowDefinition, workFlowWorkDependencies)).build();
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

	private List<WorkDefinitionResponseDTO> buildWorkFlowWorksDTOs(WorkFlowDefinition workFlowDefinition,
			List<WorkFlowWorkDefinition> workFlowWorkDefinitions) {
		CopyOnWriteArrayList<WorkDefinitionResponseDTO> workDefinitionResponseDTOs = new CopyOnWriteArrayList<>();
		Map<String, Integer> workFlowWorksStartIndex = new HashMap<>();

		// add workflow
		workDefinitionResponseDTOs.add(WorkDefinitionResponseDTO.builder().id(workFlowDefinition.getId().toString())
				.workType(WorkType.WORKFLOW.name()).name(workFlowDefinition.getName())
				.parameterFromString(workFlowDefinition.getParameters())
				.processingType(workFlowDefinition.getProcessingType()).works(new ArrayList<>())
				.numberOfWorkUnits(workFlowWorkDefinitions.size()).build());
		workFlowWorksStartIndex.put(workFlowDefinition.getName(), 1);

		// add workflowWorkUnits
		workFlowWorkDefinitions.forEach(workFlowWorkDefinition -> {
			if (workFlowWorkDefinition.getWorkDefinitionType().equals(WorkType.TASK)) { // Task
				WorkFlowTaskDefinition wdt = workFlowTaskDefinitionRepository
						.findById(workFlowWorkDefinition.getWorkDefinitionId()).get();
				workDefinitionResponseDTOs.add(WorkDefinitionResponseDTO.builder().id(wdt.getId().toString())
						.workType(WorkType.TASK.name()).name(wdt.getName()).parameterFromString(wdt.getParameters())
						.outputs(WorkFlowDTOUtil.readStringAsObject(wdt.getOutputs(), new TypeReference<>() {
						}, List.of())).build());
			}
			else { // WorkFlow
				WorkFlowDefinition wd = workFlowDefinitionRepository
						.findById(workFlowWorkDefinition.getWorkDefinitionId()).get();
				List<WorkFlowWorkDefinition> wdWorkFlowWorkDependencies = workFlowWorkRepository
						.findByWorkFlowDefinitionIdOrderByCreateDateAsc(wd.getId());
				workDefinitionResponseDTOs.add(WorkDefinitionResponseDTO.builder().id(wd.getId().toString())
						.workType(WorkType.WORKFLOW.name()).name(wd.getName()).parameterFromString(wd.getParameters())
						.processingType(wd.getProcessingType()).works(new ArrayList<>())
						.numberOfWorkUnits(wdWorkFlowWorkDependencies.size()).build());
			}
		});

		// fill in subsequent workUnits
		for (int i = 1; i < workDefinitionResponseDTOs.size(); i++) {
			if (workDefinitionResponseDTOs.get(i).getWorkType().equalsIgnoreCase(WorkType.WORKFLOW.name())) {

				workFlowWorksStartIndex.put(workDefinitionResponseDTOs.get(i).getName(),
						workDefinitionResponseDTOs.size());

				List<WorkFlowWorkDefinition> workFlowWorkUnits1Definition = workFlowWorkRepository
						.findByWorkFlowDefinitionIdOrderByCreateDateAsc(
								UUID.fromString(workDefinitionResponseDTOs.get(i).getId()))
						.stream().sorted(Comparator.comparing(WorkFlowWorkDefinition::getCreateDate))
						.collect(Collectors.toList());

				workFlowWorkUnits1Definition.forEach(wwdt1 -> {
					if (wwdt1.getWorkDefinitionType().equals(WorkType.TASK)) { // Task
						WorkFlowTaskDefinition wdt1 = workFlowTaskDefinitionRepository
								.findById(wwdt1.getWorkDefinitionId()).get();
						workDefinitionResponseDTOs.add(WorkDefinitionResponseDTO.builder().id(wdt1.getId().toString())
								.workType(WorkType.TASK.name()).name(wdt1.getName())
								.parameterFromString(wdt1.getParameters())
								.outputs(WorkFlowDTOUtil.readStringAsObject(wdt1.getOutputs(), new TypeReference<>() {
								}, List.of())).build());
					}
					else { // WorkFlow
						WorkFlowDefinition wd1 = workFlowDefinitionRepository.findById(wwdt1.getWorkDefinitionId())
								.get();
						List<WorkFlowWorkDefinition> wd1WorkFlowWorkDefinitions = workFlowWorkRepository
								.findByWorkFlowDefinitionIdOrderByCreateDateAsc(wd1.getId()).stream()
								.sorted(Comparator.comparing(WorkFlowWorkDefinition::getCreateDate))
								.collect(Collectors.toList());
						workDefinitionResponseDTOs.add(WorkDefinitionResponseDTO.builder().id(wd1.getId().toString())
								.workType(WorkType.WORKFLOW.name()).name(wd1.getName()).works(new ArrayList<>())
								.processingType(wd1.getProcessingType())
								.numberOfWorkUnits(wd1WorkFlowWorkDefinitions.size()).build());
					}
				});
			}
		}

		for (int j = workDefinitionResponseDTOs.size() - 1; j >= 0; j--) {
			if (workDefinitionResponseDTOs.get(j).getWorkType().equalsIgnoreCase(WorkType.WORKFLOW.name())) {
				List<WorkDefinitionResponseDTO> tmpList = new ArrayList<>();
				for (int k = workFlowWorksStartIndex
						.get(workDefinitionResponseDTOs.get(j).getName()); k < workFlowWorksStartIndex
								.get(workDefinitionResponseDTOs.get(j).getName())
								+ workDefinitionResponseDTOs.get(j).getNumberOfWorkUnits(); k++) {
					tmpList.add(workDefinitionResponseDTOs.get(k));
				}
				workDefinitionResponseDTOs.get(j).setWorks(tmpList);
			}
		}
		return workDefinitionResponseDTOs.get(0).getWorks();
	}

}
