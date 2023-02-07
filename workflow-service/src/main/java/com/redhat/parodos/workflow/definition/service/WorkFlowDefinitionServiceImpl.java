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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowTaskDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinitionEntity;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinitionEntity;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
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
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
@Service
public class WorkFlowDefinitionServiceImpl implements WorkFlowDefinitionService {
    private final WorkFlowDefinitionRepository workFlowDefinitionRepository;
    private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    public WorkFlowDefinitionServiceImpl(WorkFlowDefinitionRepository workFlowDefinitionRepository,
                                         WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository,
                                         ModelMapper modelMapper,
                                         ObjectMapper objectMapper) {
        this.workFlowDefinitionRepository = workFlowDefinitionRepository;
        this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitions() {
        return modelMapper.map(workFlowDefinitionRepository.findAll(), new TypeToken<List<WorkFlowDefinitionResponseDTO>>() {}.getType());
    }

    @Override
    public List<WorkFlowTaskDefinitionResponseDTO> getWorkFlowTaskDefinitionById(UUID workFlowDefinitionId) {
        return modelMapper.map(workFlowTaskDefinitionRepository.findByWorkFlowDefinitionEntity(workFlowDefinitionRepository.findById(workFlowDefinitionId).get()), new TypeToken<List<WorkFlowTaskDefinitionResponseDTO>>() {}.getType());
    }

    @Override
    public WorkFlowDefinitionResponseDTO getWorkFlowDefinitionByName(String workFlowDefinitionName) {
        return modelMapper.map(workFlowDefinitionRepository.findByName(workFlowDefinitionName).get(0), WorkFlowDefinitionResponseDTO.class);
    }

    @Override
    public WorkFlowDefinitionResponseDTO save(String workFlowName, WorkFlowType workFlowType, Map<String, WorkFlowTask> hmWorkFlowTasks) {
        // prepare workflow entity
        WorkFlowDefinitionEntity workFlowDefinitionEntity = WorkFlowDefinitionEntity.builder()
                .name(workFlowName)
                .type(workFlowType.name())
                .createDate(new Date())
                .modifyDate(new Date())
                .build();
        // set workflow task entities
        workFlowDefinitionEntity.setWorkFlowTaskDefinitionEntities(hmWorkFlowTasks.entrySet().stream().map( entry -> WorkFlowTaskDefinitionEntity.builder()
                .name(entry.getKey())
                .parameters(writeValueAsString(entry.getValue().getParameters().stream()
                        .map(workFlowTaskParameter -> {
                            var hm = new HashMap<>();
                            hm.put("key", workFlowTaskParameter.getKey());
                            hm.put("description", workFlowTaskParameter.getDescription());
                            hm.put("type", workFlowTaskParameter.getType().name());
                            hm.put("optional", workFlowTaskParameter.isOptional());
                            return hm;
                        })
                        .collect(Collectors.toList())))
                .outputs(writeValueAsString(entry.getValue().getOutputs()))
                .workFlowDefinitionEntity(workFlowDefinitionEntity)
                .createDate(new Date())
                .modifyDate(new Date())
                .build()).collect(Collectors.toList()));
        return modelMapper.map(workFlowDefinitionRepository.save(workFlowDefinitionEntity), WorkFlowDefinitionResponseDTO.class);
    }

    private String writeValueAsString(Object objectValue) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(objectMapper.writeValueAsString(objectValue));
        } catch (JsonProcessingException e) {
            log.error("Error occurred in string conversion: {}", e.getMessage());
        }
        return sb.toString();
    }
}
