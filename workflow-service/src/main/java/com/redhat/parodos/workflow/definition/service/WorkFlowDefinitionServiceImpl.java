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

import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerDefinitionPK;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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

	private final ModelMapper modelMapper;

	public WorkFlowDefinitionServiceImpl(WorkFlowDefinitionRepository workFlowDefinitionRepository,
			WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository, ModelMapper modelMapper) {
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public WorkFlowDefinitionResponseDTO save(String workFlowName, String workFlowDescription,
			WorkFlowType workFlowType, Map<String, WorkFlowTask> hmWorkFlowTasks) {
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(workFlowName)
				.description(workFlowDescription).type(workFlowType.name()).createDate(new Date())
				.modifyDate(new Date()).build();

		workFlowDefinition.setWorkFlowTaskDefinitions(hmWorkFlowTasks.entrySet().stream()
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
		return modelMapper.map(workFlowDefinitionRepository.save(workFlowDefinition),
				WorkFlowDefinitionResponseDTO.class);
	}

	@Override
	public List<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitions() {
		return modelMapper.map(workFlowDefinitionRepository.findAll(),
				new TypeToken<List<WorkFlowDefinitionResponseDTO>>() {
				}.getType());
	}

	@Override
	public WorkFlowDefinitionResponseDTO getWorkFlowDefinitionById(UUID id) {
		WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository.findById(id)
				.orElseThrow(() -> new RuntimeException(String.format("Workflow definition id %s not found", id)));
		return modelMapper.map(workFlowDefinition, WorkFlowDefinitionResponseDTO.class);
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
			WorkFlowDefinition nextWorkFlowDefinitionEntity = workFlowDefinitionRepository
					.findByName(workFlowCheckerDTO.getNextWorkFlowName()).get(0);

			WorkFlowCheckerDefinition wfChecker = WorkFlowCheckerDefinition.builder()
					.id(WorkFlowCheckerDefinitionPK.builder().workFlowCheckerId(checkerWorkFlowDefinitionEntity.getId())
							.taskId(workFlowTaskDefinitionEntity.getId()).build())
					.task(workFlowTaskDefinitionEntity).checkWorkFlow(checkerWorkFlowDefinitionEntity)
					.nextWorkFlow(nextWorkFlowDefinitionEntity).cronExpression(workFlowCheckerDTO.getCronExpression())
					.build();
			workFlowTaskDefinitionEntity.setWorkFlowCheckerDefinition(wfChecker);
			workFlowTaskDefinitionRepository.save(workFlowTaskDefinitionEntity);
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}
