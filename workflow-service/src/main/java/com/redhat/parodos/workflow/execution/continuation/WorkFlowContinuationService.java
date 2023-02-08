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
package com.redhat.parodos.workflow.execution.continuation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.WorkFlowStatus;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionEntity;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecutionEntity;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * When the application starts up it will run any workflows in Progress @see Status.IN_PROGRESS
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Service
@Slf4j
public class WorkFlowContinuationService {
    private final WorkFlowDefinitionRepository workFlowDefinitionRepository;
    private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;
    private final WorkFlowRepository workFlowRepository;
    private final WorkFlowTaskRepository workFlowTaskRepository;
    private final WorkFlowServiceImpl workFlowService;
    private final WorkFlowDelegate workFlowDelegate;
    private final ObjectMapper objectMapper;

    public WorkFlowContinuationService(WorkFlowDefinitionRepository workFlowDefinitionRepository,
                                       WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository,
                                       WorkFlowRepository workFlowRepository,
                                       WorkFlowTaskRepository workFlowTaskRepository,
                                       WorkFlowServiceImpl workFlowService,
                                       WorkFlowDelegate workFlowDelegate,
                                       ObjectMapper objectMapper) {
        this.workFlowDefinitionRepository = workFlowDefinitionRepository;
        this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
        this.workFlowRepository = workFlowRepository;
        this.workFlowTaskRepository = workFlowTaskRepository;
        this.workFlowService = workFlowService;
        this.workFlowDelegate = workFlowDelegate;
        this.objectMapper = objectMapper;
    }

    /**
     * When the application starts up, get all workflows with Status.IN_PROGRESS and execute them
     */
    @EventListener(ApplicationReadyEvent.class)
    public void workFlowRunAfterStartup() {
        log.info("Looking up all IN PROGRESS workflows for ");
        List<WorkFlowExecutionEntity> workFlowEntityList = workFlowRepository.findAll();
        log.info("Number of IN PROGRESS workflows for : {}", workFlowEntityList.size());
        workFlowEntityList.stream()
                .filter(workFlowEntity -> WorkFlowStatus.IN_PROGRESS == workFlowEntity.getStatus())
                .forEach(workFlowEntity -> {
                    WorkFlowDefinition workFlowDefinitionEntity = workFlowDefinitionRepository.findById(workFlowEntity.getWorkFlowDefinitionId()).get();
                    List<WorkFlowTaskExecutionEntity> workFlowTaskEntityList = workFlowTaskRepository.findByWorkFlowExecutionId(workFlowEntity.getId());
                    Map<String, Map<String, String>> workFlowTaskArguments = new HashMap<>();
                    workFlowTaskEntityList.forEach(workFlowTaskEntity -> {
                        try {
                            workFlowTaskArguments.put(workFlowTaskDefinitionRepository.findById(workFlowTaskEntity.getWorkFlowTaskDefinitionId()).get().getName(),
                                    objectMapper.readValue(workFlowTaskEntity.getArguments(), new TypeReference<>() {
                                    }));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    workFlowService.execute(workFlowDefinitionEntity.getName(), workFlowTaskArguments);
                });
    }
}
