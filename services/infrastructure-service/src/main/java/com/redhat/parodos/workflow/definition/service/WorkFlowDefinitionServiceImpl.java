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

import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerDefinitionEntity;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinitionEntity;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinitionEntity;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * workflow definition service implementation
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
@Service
public class WorkFlowDefinitionServiceImpl implements WorkFlowDefinitionService {
    private final WorkFlowDefinitionRepository workFlowDefinitionRepository;
    private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

    public WorkFlowDefinitionServiceImpl(WorkFlowDefinitionRepository workFlowDefinitionRepository,
                                         WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository) {
        this.workFlowDefinitionRepository = workFlowDefinitionRepository;
        this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
    }

    @Override
    public List<WorkFlowDefinitionEntity> getWorkFlowDefinitions() {
        return workFlowDefinitionRepository.findAll();
    }

    @Override
    public List<WorkFlowTaskDefinitionEntity> getWorkFlowTaskDefinitionById(UUID workFlowDefinitionId) {
        return workFlowTaskDefinitionRepository.findByWorkFlowDefinitionEntity(workFlowDefinitionRepository.findById(workFlowDefinitionId).get());
    }

    @Override
    public WorkFlowDefinitionEntity getWorkFlowDefinitionByName(String workFlowDefinitionName) {
        return workFlowDefinitionRepository.findByName(workFlowDefinitionName).get(0);
    }

    @Override
    public List<String> getWholeWorkFlow(String workFlowDefinitionName) {
        return getNextWorkFlow(workFlowDefinitionRepository.findByName(workFlowDefinitionName).get(0));
    }

    private List<String> getNextWorkFlow(WorkFlowDefinitionEntity entityRef) {
        List<String> workFlows = new ArrayList<>();
        workFlows.add(entityRef.getName());
        entityRef.getTasks().stream()
                .map(WorkFlowTaskDefinitionEntity::getWorkFlowCheckerDefinitionEntity)
                .filter(Objects::nonNull)
                .findAny()
                .map(WorkFlowCheckerDefinitionEntity::getNextWorkFlow)
                .ifPresent(nextWorkFlow ->
                        workFlows.addAll(this.getWholeWorkFlow(nextWorkFlow.getName()))
                );

        return workFlows;
    }
}

