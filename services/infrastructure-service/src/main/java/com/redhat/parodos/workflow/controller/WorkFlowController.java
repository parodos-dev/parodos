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
 */package com.redhat.parodos.workflow.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.definition.dto.EmbeddedTaskResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowTaskDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinitionEntity;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinitionEntity;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.definition.service.WorkFlowTaskDefinitionServiceImpl;
import com.redhat.parodos.workflow.execution.dto.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowTaskExecutionRequestDTO;
import com.redhat.parodos.workflow.execution.service.WorkFlowExecutionServiceImpl;
import com.redhat.parodos.workflows.common.context.WorkContextUtil;
import com.redhat.parodos.workflows.definition.WorkFlowDefinition;
import com.redhat.parodos.workflows.work.WorkReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * controller
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
@Slf4j
@CrossOrigin(origins = "*", maxAge = 1800)
@RestController
@RequestMapping("/api/v1/workflows")
public class WorkFlowController {
    private final WorkFlowDefinitionServiceImpl workFlowDefinitionService;
    private final WorkFlowTaskDefinitionServiceImpl workFlowTaskDefinitionService;
    private final WorkFlowExecutionServiceImpl workFlowExecutionService;
    private final WorkFlowDelegate workFlowDelegate;
    private ObjectMapper objectMapper;

    public WorkFlowController(WorkFlowDefinitionServiceImpl workFlowDefinitionService, WorkFlowTaskDefinitionServiceImpl workFlowTaskDefinitionService, WorkFlowExecutionServiceImpl workFlowExecutionService, WorkFlowDelegate workFlowDelegate, ObjectMapper objectMapper) {
        this.workFlowDefinitionService = workFlowDefinitionService;
        this.workFlowTaskDefinitionService = workFlowTaskDefinitionService;
        this.workFlowExecutionService = workFlowExecutionService;
        this.workFlowDelegate = workFlowDelegate;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public ResponseEntity<Collection<WorkFlowDefinitionResponseDTO>> getWorkFlowDefinitions() {
        List<WorkFlowDefinitionEntity> workFlowDefinitionEntityList = workFlowDefinitionService.getWorkFlowDefinitions();
        return ResponseEntity.ok(workFlowDefinitionEntityList.stream().map(wd -> WorkFlowDefinitionResponseDTO.builder()
                        .id(wd.getId().toString())
                        .name(wd.getName())
                        .description(wd.getDescription())
                        .type(wd.getType())
                        .author(wd.getAuthor())
                        .createdDate(wd.getCreateDate())
                        .modifiedDate(wd.getModifyDate())
                        .tasks(wd.getTasks().stream().map(wdt -> EmbeddedTaskResponseDTO.builder()
                                .id(wdt.getId().toString())
                                .name(wdt.getName())
                                .build()).collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList()));
    }


    @GetMapping("/{workFlowDefinitionId}/tasks")
    public ResponseEntity<Collection<WorkFlowTaskDefinitionResponseDTO>> getWorkFlowTaskDefinitions(@PathVariable String workFlowDefinitionId) {
        List<WorkFlowTaskDefinitionEntity> workFlowTaskDefinitionEntityList = workFlowDefinitionService.getWorkFlowTaskDefinitionById(UUID.fromString(workFlowDefinitionId));
        return ResponseEntity.ok(workFlowTaskDefinitionEntityList.stream().map(wdte -> {
                    try {
                        return WorkFlowTaskDefinitionResponseDTO.builder()
                                .id(wdte.getId().toString())
                                .name(wdte.getName())
                                .description(wdte.getDescription())
                                .parameters(objectMapper.readValue(wdte.getParameters(), new TypeReference<>() {
                                }))
                                .outputs(objectMapper.readValue(wdte.getOutputs(), new TypeReference<>() {
                                }))
                                .previousTask(getEmbeddedTask(wdte.getPreviousTask()))
                                .nextTask(getEmbeddedTask(wdte.getNextTask()))
                                .workFlowChecker(Optional.of(wdte)
                                        .map(WorkFlowTaskDefinitionEntity::getWorkFlowCheckerDefinitionEntity)
                                        .map(checker -> checker.getCheckWorkFlow().getId())
                                        .orElse(null))
                                .nextWorkFlow(Optional.of(wdte)
                                        .map(WorkFlowTaskDefinitionEntity::getWorkFlowCheckerDefinitionEntity)
                                        .map(checker -> checker.getNextWorkFlow().getId())
                                        .orElse(null))
                                .build();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList()));
    }

    @PostMapping("/{workFlowDefinitionId}/executions")
    public ResponseEntity<WorkFlowExecutionResponseDTO> executeWorkFlow(@PathVariable String workFlowDefinitionId,
                                                                        @RequestBody List<WorkFlowTaskExecutionRequestDTO> workFlowTaskExecutionRequestDTOList) {
        Map<String, Map<String, String>> workFlowTaskArguments = new HashMap<>();
        workFlowTaskExecutionRequestDTOList.forEach(arg -> {
            Map<String, String> tasksValuesMap = new HashMap<>();
            arg.getArguments().forEach(i -> tasksValuesMap.put(i.getKey(), i.getValue()));
            workFlowTaskArguments.put(arg.getTaskName(), tasksValuesMap);
        });
        WorkFlowDefinition workFlowDefinition = workFlowDelegate.getWorkFlowDefinitionById(UUID.fromString(workFlowDefinitionId));
        WorkReport workReport = workFlowExecutionService.execute(workFlowDefinition,
                workFlowDelegate.getWorkFlowExecutionByName(workFlowDefinition.getName()),
                workFlowTaskArguments);
        return ResponseEntity.ok(WorkFlowExecutionResponseDTO.builder()
                .workFlowExecutionId(WorkContextUtil.read(
                        workReport.getWorkContext(),
                        WorkContextUtil.ProcessType.WORKFLOW_EXECUTION,
                        WorkContextUtil.Resource.ID).toString())
                .output(WorkContextUtil.read(workReport.getWorkContext(),
                        WorkContextUtil.ProcessType.WORKFLOW_EXECUTION,
                        WorkContextUtil.Resource.INFRASTRUCTURE_OPTIONS))
                .build());
    }

    private EmbeddedTaskResponseDTO getEmbeddedTask(UUID workFlowTaskDefinitionId) {
        if (null == workFlowTaskDefinitionId)
            return null;
        WorkFlowTaskDefinitionEntity wdte = workFlowTaskDefinitionService.getWorkFlowTaskDefinitionById(workFlowTaskDefinitionId);
        if (null == wdte)
            return null;
        return EmbeddedTaskResponseDTO.builder()
                .id(wdte.getId().toString())
                .name(wdte.getName())
                .build();
    }
}
