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
package com.redhat.parodos.workflow.definition.dto;

import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * convert from workflow task eneity list to dto list
 *
 * @author Annel Ketcha (Github: anludke)
 */

public class WorkFlowTaskDefinitionListToTaskResponseDTOListConverter implements Converter<List<WorkFlowTaskDefinition>, List<WorkFlowDefinitionResponseDTO.TaskResponseDTO>> {
    @Override
    public List<WorkFlowDefinitionResponseDTO.TaskResponseDTO> convert(MappingContext<List<WorkFlowTaskDefinition>, List<WorkFlowDefinitionResponseDTO.TaskResponseDTO>> context) {
        List<WorkFlowTaskDefinition> source = context.getSource();
        return source.stream().map(workFlowTaskDefinition ->
                    WorkFlowDefinitionResponseDTO.TaskResponseDTO.builder()
                            .id(workFlowTaskDefinition.getId())
                            .name(workFlowTaskDefinition.getName())
                            .workFlowChecker(Optional.ofNullable(workFlowTaskDefinition.getWorkFlowCheckerDefinition()).map(checker -> checker.getCheckWorkFlow().getId()).orElse(null))
                            .nextWorkFlow(Optional.ofNullable(workFlowTaskDefinition.getWorkFlowCheckerDefinition()).map(checker -> checker.getNextWorkFlow().getId()).orElse(null))
                            .build())
                .collect(Collectors.toList());
    }
}