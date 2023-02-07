///*
// * Copyright (c) 2022 Red Hat Developer
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.redhat.parodos.workflow.execution.continuation;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.redhat.parodos.workflow.WorkFlowDefinition;
//import com.redhat.parodos.workflow.WorkFlowDelegate;
//import com.redhat.parodos.workflow.WorkFlowStatus;
//import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinitionEntity;
//import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
//import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
//import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionEntity;
//import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecutionEntity;
//import com.redhat.parodos.workflow.execution.repository.WorkFlowExecutionRepository;
//import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskExecutionRepository;
//import com.redhat.parodos.workflow.execution.service.WorkFlowExecutionServiceImpl;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Service;
//
///**
// * When the application starts up it will run any workflows in Progress @see Status.IN_PROGRESS
// *
// * @author Richard Wang (Github: richardw98)
// * @author Annel Ketcha (Github: anludke)
// */
//
//@Service
//@Slf4j
//public class WorkFlowContinuationService {
//    private final WorkFlowDefinitionRepository workFlowDefinitionRepository;
//    private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;
//    private final WorkFlowExecutionRepository workFlowExecutionRepository;
//    private final WorkFlowTaskExecutionRepository workFlowTaskExecutionRepository;
//    private final WorkFlowExecutionServiceImpl workFlowExecutionService;
//    private final WorkFlowDelegate workFlowDelegate;
//    private final ObjectMapper objectMapper;
//
//    public WorkFlowContinuationService(WorkFlowDefinitionRepository workFlowDefinitionRepository,
//                                       WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository,
//                                       WorkFlowExecutionRepository workFlowExecutionRepository,
//                                       WorkFlowTaskExecutionRepository workFlowTaskExecutionRepository,
//                                       WorkFlowExecutionServiceImpl workFlowExecutionService,
//                                       WorkFlowDelegate workFlowDelegate,
//                                       ObjectMapper objectMapper) {
//        this.workFlowDefinitionRepository = workFlowDefinitionRepository;
//        this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
//        this.workFlowExecutionRepository = workFlowExecutionRepository;
//        this.workFlowTaskExecutionRepository = workFlowTaskExecutionRepository;
//        this.workFlowExecutionService = workFlowExecutionService;
//        this.workFlowDelegate = workFlowDelegate;
//        this.objectMapper = objectMapper;
//    }
//
//    /**
//     * When the application starts up, get all workflows with Status.IN_PROGRESS and execute them
//     */
//    @EventListener(ApplicationReadyEvent.class)
//    public void workFlowRunAfterStartup() {
//        log.info("Looking up all IN PROGRESS workflows for execution");
//        List<WorkFlowExecutionEntity> workFlowExecutionEntityList = workFlowExecutionRepository.findAll();
//        log.info("Number of IN PROGRESS workflows for execution: {}", workFlowExecutionEntityList.size());
//        workFlowExecutionEntityList.stream()
//                .filter(workFlowExecutionEntity -> WorkFlowStatus.IN_PROGRESS == workFlowExecutionEntity.getStatus())
//                .forEach(workFlowExecutionEntity -> {
//                    WorkFlowDefinitionEntity workFlowDefinitionEntity = workFlowDefinitionRepository.findById(workFlowExecutionEntity.getWorkFlowDefinitionId()).get();
//                    WorkFlowDefinition workFlowDefinition = workFlowDelegate.getWorkFlowDefinitionById(workFlowDefinitionEntity.getId());
//                    List<WorkFlowTaskExecutionEntity> workFlowTaskExecutionEntityList = workFlowTaskExecutionRepository.findByWorkFlowExecutionId(workFlowExecutionEntity.getId());
//                    Map<String, Map<String, String>> workFlowTaskArguments = new HashMap<>();
//                    workFlowTaskExecutionEntityList.forEach(workFlowTaskExecutionEntity -> {
//                        try {
//                            workFlowTaskArguments.put(workFlowTaskDefinitionRepository.findById(workFlowTaskExecutionEntity.getWorkFlowTaskDefinitionId()).get().getName(),
//                                    objectMapper.readValue(workFlowTaskExecutionEntity.getArguments(), new TypeReference<>() {
//                                    }));
//                        } catch (JsonProcessingException e) {
//                            throw new RuntimeException(e);
//                        }
//                    });
//                    workFlowExecutionService.execute(workFlowDefinition,
//                            workFlowDelegate.getWorkFlowExecutionByName(workFlowDefinition.getName()),
//                            workFlowTaskArguments);
//                });
//    }
//}
