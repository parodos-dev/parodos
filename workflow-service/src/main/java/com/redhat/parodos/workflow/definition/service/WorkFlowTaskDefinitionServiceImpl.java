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

import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * workflow task definition service implementation
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
@Service
public class WorkFlowTaskDefinitionServiceImpl implements WorkFlowTaskDefinitionService {
    private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

    public WorkFlowTaskDefinitionServiceImpl(WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository) {
        this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
    }

    @Override
    public WorkFlowTaskDefinition getWorkFlowTaskDefinitionById(UUID id) {
        return workFlowTaskDefinitionRepository.findById(id).get();
    }
}
