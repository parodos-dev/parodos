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
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkUnit;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowCheckerDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkDependencyRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.parameter.WorkFlowParameter;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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

	private final WorkFlowCheckerDefinitionRepository workFlowCheckerDefinitionRepository;

	private final WorkFlowWorkDependencyRepository workFlowWorkDependencyRepository;

	private final ModelMapper modelMapper;

	public WorkFlowDefinitionServiceImpl(WorkFlowDefinitionRepository workFlowDefinitionRepository,
			WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository,
			WorkFlowCheckerDefinitionRepository workFlowCheckerDefinitionRepository,
			WorkFlowWorkDependencyRepository workFlowWorkDependencyRepository, ModelMapper modelMapper) {
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
		this.workFlowCheckerDefinitionRepository = workFlowCheckerDefinitionRepository;
		this.workFlowWorkDependencyRepository = workFlowWorkDependencyRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public WorkFlowDefinitionResponseDTO save(String workFlowName, String workFlowDescription,
			WorkFlowType workFlowType, List<WorkFlowParameter> workFlowParameters,
			Map<String, WorkFlowTask> workFlowTasks, List<Work> works, WorkFlowProcessingType workFlowProcessingType) {
		// prepare workFlowDefinition entity
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(workFlowName)
				.description(workFlowDescription).type(workFlowType.name()).createDate(new Date())
				.parameters(WorkFlowDTOUtil.writeObjectValueAsString(workFlowParameters)).modifyDate(new Date())
				.numberWorkUnits(works.size()).processingType(workFlowProcessingType.name()).build();
		// set workflowTasks to workFlowDefinition entity
		workFlowDefinition.setWorkFlowTaskDefinitions(workFlowTasks.entrySet().stream()
				.map(entry -> WorkFlowTaskDefinition.builder().name(entry.getKey())
						.parameters(WorkFlowDTOUtil.writeObjectValueAsString(
								entry.getValue().getWorkFlowTaskParameters().stream().map(workFlowTaskParameter -> {
									var hm = new HashMap<>();
									hm.put("key", workFlowTaskParameter.getKey());
									hm.put("description", workFlowTaskParameter.getDescription());
									hm.put("type", workFlowTaskParameter.getType().name());
									hm.put("optional", workFlowTaskParameter.isOptional());
									return hm;
								}).collect(Collectors.toList())))
						.outputs(WorkFlowDTOUtil.writeObjectValueAsString(entry.getValue().getWorkFlowTaskOutputs()))
						.workFlowDefinition(workFlowDefinition).createDate(new Date()).modifyDate(new Date()).build())
				.collect(Collectors.toList()));
		// save workFlowDefinition entity
		WorkFlowDefinition savedWorkFlowDefinition = workFlowDefinitionRepository.save(workFlowDefinition);
		// save workFlowDefinition's works (dependencies)
		saveWorkUnits(workFlowDefinition, works);
		return modelMapper.map(savedWorkFlowDefinition, WorkFlowDefinitionResponseDTO.class);
	}

	@Override
	public List<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitions() {
		List<WorkFlowDefinitionResponseDTO> workFlowDefinitionResponseDTOs = new ArrayList<>();
		List<WorkFlowDefinition> workFlowDefinitions = workFlowDefinitionRepository.findAll();
		workFlowDefinitions.stream().filter(
				workFlowDefinition -> workFlowDefinition.getType().equalsIgnoreCase(WorkFlowType.ASSESSMENT.name())
						|| workFlowDefinition.getType().equalsIgnoreCase(WorkFlowType.INFRASTRUCTURE.name()))
				.forEach(workFlowDefinition -> {
					List<WorkFlowWorkUnit> workFlowWorkDependencies = workFlowWorkDependencyRepository
							.findByWorkFlowDefinitionId(workFlowDefinition.getId()).stream()
							.sorted(Comparator.comparing(WorkFlowWorkUnit::getCreateDate)).collect(Collectors.toList());
					workFlowDefinitionResponseDTOs.add(WorkFlowDefinitionResponseDTO.builder()
							.id(workFlowDefinition.getId()).name(workFlowDefinition.getName())
							.parameters(WorkFlowDTOUtil.readStringAsObject(workFlowDefinition.getParameters(),
									new TypeReference<>() {
									}, List.of()))
							.author(workFlowDefinition.getAuthor()).createDate(workFlowDefinition.getCreateDate())
							.modifyDate(workFlowDefinition.getModifyDate()).type(workFlowDefinition.getType())
							.processingType(workFlowDefinition.getProcessingType())
							.works(buildWorkDefinitionResponseDTO(workFlowDefinition, workFlowWorkDependencies))
							.build());
				});
		return workFlowDefinitionResponseDTOs;
	}

	@Override
	public WorkFlowDefinitionResponseDTO getWorkFlowDefinitionById(UUID id) {
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.findById(id)
				.orElseThrow(() -> new RuntimeException(String.format("Workflow definition id %s not found", id)));
		List<WorkFlowWorkUnit> workFlowWorkDependencies = workFlowWorkDependencyRepository
				.findByWorkFlowDefinitionId(workFlowDefinition.getId()).stream()
				.sorted(Comparator.comparing(WorkFlowWorkUnit::getCreateDate)).collect(Collectors.toList());
		return WorkFlowDefinitionResponseDTO.builder().id(workFlowDefinition.getId()).name(workFlowDefinition.getName())
				.parameters(
						WorkFlowDTOUtil.readStringAsObject(workFlowDefinition.getParameters(), new TypeReference<>() {
						}, List.of()))
				.author(workFlowDefinition.getAuthor()).createDate(workFlowDefinition.getCreateDate())
				.modifyDate(workFlowDefinition.getModifyDate()).type(workFlowDefinition.getType())
				.processingType(workFlowDefinition.getProcessingType())
				.works(buildWorkDefinitionResponseDTO(workFlowDefinition, workFlowWorkDependencies)).build();
	}

	@Override
	public WorkFlowDefinitionResponseDTO getWorkFlowDefinitionByName(String name) {
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.findFirstByName(name);
		if (null == workFlowDefinition) {
			throw new RuntimeException(String.format("Workflow definition name %s not found", name));
		}
		List<WorkFlowWorkUnit> workFlowWorkDependencies = workFlowWorkDependencyRepository
				.findByWorkFlowDefinitionId(workFlowDefinition.getId()).stream()
				.sorted(Comparator.comparing(WorkFlowWorkUnit::getCreateDate)).collect(Collectors.toList());
		return WorkFlowDefinitionResponseDTO.builder().id(workFlowDefinition.getId()).name(workFlowDefinition.getName())
				.parameters(
						WorkFlowDTOUtil.readStringAsObject(workFlowDefinition.getParameters(), new TypeReference<>() {
						}, List.of()))
				.author(workFlowDefinition.getAuthor()).createDate(workFlowDefinition.getCreateDate())
				.modifyDate(workFlowDefinition.getModifyDate()).type(workFlowDefinition.getType())
				.processingType(workFlowDefinition.getProcessingType())
				.works(buildWorkDefinitionResponseDTO(workFlowDefinition, workFlowWorkDependencies)).build();
	}

	@Override
	public List<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionsByName(String name) {
		return modelMapper.map(workFlowDefinitionRepository.findByName(name),
				new TypeToken<List<WorkFlowDefinitionResponseDTO>>() {
				}.getType());
	}

	@Override
	public void saveWorkFlowChecker(String workFlowTaskName, String workFlowCheckerName,
			WorkFlowCheckerDTO workFlowCheckerDTO) {
		try {
			WorkFlowTaskDefinition workFlowTaskDefinitionEntity = workFlowTaskDefinitionRepository
					.findFirstByName(workFlowTaskName);
			WorkFlowDefinition checkerWorkFlowDefinitionEntity = workFlowDefinitionRepository
					.findByName(workFlowCheckerName).get(0);
			WorkFlowCheckerDefinition workFlowCheckerDefinition = Optional
					.ofNullable(workFlowCheckerDefinitionRepository
							.findFirstByCheckWorkFlow(checkerWorkFlowDefinitionEntity))
					.orElse(WorkFlowCheckerDefinition.builder().checkWorkFlow(checkerWorkFlowDefinitionEntity)
							.cronExpression(workFlowCheckerDTO.getCronExpression()).tasks(new ArrayList<>()).build());
			workFlowTaskDefinitionEntity.setWorkFlowCheckerDefinition(workFlowCheckerDefinition);
			workFlowTaskDefinitionRepository.save(workFlowTaskDefinitionEntity);
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	private void saveWorkUnits(WorkFlowDefinition workFlowDefinition, List<Work> works) {
		List<WorkFlowWorkUnit> workFlowWorkDependencies = works.stream().map(work -> {
			UUID workId;
			String workType;
			if (work instanceof WorkFlow) {
				workId = workFlowDefinitionRepository.findFirstByName(work.getName()).getId();
				workType = WorkType.WORKFLOW.name();
			}
			else { // WorkFlowTask
				workId = workFlowTaskDefinitionRepository.findFirstByName(work.getName()).getId();
				workType = WorkType.TASK.name();
			}
			return WorkFlowWorkUnit.builder().workDefinitionId(workId).workDefinitionType(workType)
					.workFlowDefinitionId(workFlowDefinition.getId()).createDate(new Date()).build();
		}).collect(Collectors.toList());
		workFlowWorkDependencyRepository.saveAll(workFlowWorkDependencies);
	}

	private List<WorkDefinitionResponseDTO> buildWorkDefinitionResponseDTO(WorkFlowDefinition workFlowDefinition,
			List<WorkFlowWorkUnit> workFlowWorkUnits) {
		CopyOnWriteArrayList<WorkDefinitionResponseDTO> workDefinitionResponseDTOs = new CopyOnWriteArrayList<>();
		Map<String, Integer> workFlowWorkUnitsStartIndex = new HashMap<>();

		// add workflow
		workDefinitionResponseDTOs.add(WorkDefinitionResponseDTO.builder().id(workFlowDefinition.getId().toString())
				.workType(WorkType.WORKFLOW.name()).name(workFlowDefinition.getName()).parameters(
						WorkFlowDTOUtil.readStringAsObject(workFlowDefinition.getParameters(), new TypeReference<>() {
						}, List.of()))
				.processingType(workFlowDefinition.getProcessingType()).works(new ArrayList<>())
				.numberOfWorkUnits(workFlowWorkUnits.size()).build());
		workFlowWorkUnitsStartIndex.put(workFlowDefinition.getName(), 1);

		// add workflowWorkUnits
		workFlowWorkUnits.forEach(workFlowWorkUnit -> {
			if (workFlowWorkUnit.getWorkDefinitionType().equalsIgnoreCase(WorkType.TASK.name())) { // Task
				WorkFlowTaskDefinition wdt = workFlowTaskDefinitionRepository
						.findById(workFlowWorkUnit.getWorkDefinitionId()).get();
				workDefinitionResponseDTOs.add(WorkDefinitionResponseDTO.builder().id(wdt.getId().toString())
						.workType(WorkType.TASK.name()).name(wdt.getName())
						.parameters(WorkFlowDTOUtil.readStringAsObject(wdt.getParameters(), new TypeReference<>() {
						}, List.of()))
						.outputs(WorkFlowDTOUtil.readStringAsObject(wdt.getOutputs(), new TypeReference<>() {
						}, List.of())).build());
			}
			else { // WorkFlow
				WorkFlowDefinition wd = workFlowDefinitionRepository.findById(workFlowWorkUnit.getWorkDefinitionId())
						.get();
				List<WorkFlowWorkUnit> wdWorkFlowWorkDependencies = workFlowWorkDependencyRepository
						.findByWorkFlowDefinitionId(wd.getId()).stream()
						.sorted(Comparator.comparing(WorkFlowWorkUnit::getCreateDate)).collect(Collectors.toList());
				workDefinitionResponseDTOs.add(WorkDefinitionResponseDTO.builder().id(wd.getId().toString())
						.workType(WorkType.WORKFLOW.name()).name(wd.getName())
						.parameters(WorkFlowDTOUtil.readStringAsObject(wd.getParameters(), new TypeReference<>() {
						}, List.of())).processingType(wd.getProcessingType()).works(new ArrayList<>())
						.numberOfWorkUnits(wdWorkFlowWorkDependencies.size()).build());
			}
		});

		// fill in subsequent workUnits
		for (int i = 1; i < workDefinitionResponseDTOs.size(); i++) {
			if (workDefinitionResponseDTOs.get(i).getWorkType().equalsIgnoreCase(WorkType.WORKFLOW.name())) {

				workFlowWorkUnitsStartIndex.put(workDefinitionResponseDTOs.get(i).getName(),
						workDefinitionResponseDTOs.size());

				List<WorkFlowWorkUnit> workFlowWorkDependencies1 = workFlowWorkDependencyRepository
						.findByWorkFlowDefinitionId(UUID.fromString(workDefinitionResponseDTOs.get(i).getId()))
						.stream().sorted(Comparator.comparing(WorkFlowWorkUnit::getCreateDate))
						.collect(Collectors.toList());

				workFlowWorkDependencies1.forEach(wwdt1 -> {
					if (wwdt1.getWorkDefinitionType().equalsIgnoreCase(WorkType.TASK.name())) { // Task
						WorkFlowTaskDefinition wdt1 = workFlowTaskDefinitionRepository
								.findById(wwdt1.getWorkDefinitionId()).get();
						workDefinitionResponseDTOs.add(WorkDefinitionResponseDTO.builder().id(wdt1.getId().toString())
								.workType(WorkType.TASK.name()).name(wdt1.getName()).parameters(
										WorkFlowDTOUtil.readStringAsObject(wdt1.getParameters(), new TypeReference<>() {
										}, List.of()))
								.outputs(WorkFlowDTOUtil.readStringAsObject(wdt1.getOutputs(), new TypeReference<>() {
								}, List.of())).build());
					}
					else { // WorkFlow
						WorkFlowDefinition wd1 = workFlowDefinitionRepository.findById(wwdt1.getWorkDefinitionId())
								.get();
						List<WorkFlowWorkUnit> wd1WorkFlowWorkDependencies = workFlowWorkDependencyRepository
								.findByWorkFlowDefinitionId(wd1.getId()).stream()
								.sorted(Comparator.comparing(WorkFlowWorkUnit::getCreateDate))
								.collect(Collectors.toList());
						workDefinitionResponseDTOs.add(WorkDefinitionResponseDTO.builder().id(wd1.getId().toString())
								.workType(WorkType.WORKFLOW.name()).name(wd1.getName()).parameters(
										WorkFlowDTOUtil.readStringAsObject(wd1.getParameters(), new TypeReference<>() {
										}, List.of()))
								.works(new ArrayList<>()).processingType(wd1.getProcessingType())
								.numberOfWorkUnits(wd1WorkFlowWorkDependencies.size()).build());
					}
				});
			}
		}

		for (int j = workDefinitionResponseDTOs.size() - 1; j >= 0; j--) {
			if (workDefinitionResponseDTOs.get(j).getWorkType().equalsIgnoreCase(WorkType.WORKFLOW.name())) {
				List<WorkDefinitionResponseDTO> tmp = new ArrayList<>();
				for (int k = workFlowWorkUnitsStartIndex
						.get(workDefinitionResponseDTOs.get(j).getName()); k < workFlowWorkUnitsStartIndex
								.get(workDefinitionResponseDTOs.get(j).getName())
								+ workDefinitionResponseDTOs.get(j).getNumberOfWorkUnits(); k++) {
					tmp.add(workDefinitionResponseDTOs.get(k));
				}
				workDefinitionResponseDTOs.get(j).setWorks(tmp);
			}
		}
		return workDefinitionResponseDTOs.get(0).getWorks();
	}
}
