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
package com.redhat.parodos.workflow.definition.dto.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.definition.dto.WorkFlowTaskDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterScope;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Workflow task definition converter
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

public class WorkFlowTaskDefinitionDTOConverter
		implements Converter<List<WorkFlowTaskDefinition>, List<WorkFlowTaskDefinitionResponseDTO>> {

	@Override
	public List<WorkFlowTaskDefinitionResponseDTO> convert(
			MappingContext<List<WorkFlowTaskDefinition>, List<WorkFlowTaskDefinitionResponseDTO>> context) {
		ObjectMapper objectMapper = new ObjectMapper();
		List<WorkFlowTaskDefinition> source = context.getSource();
		return source.stream().map(workFlowTaskDefinition -> {
			try {
				return WorkFlowTaskDefinitionResponseDTO.builder().id(workFlowTaskDefinition.getId().toString())
						.name(workFlowTaskDefinition.getName())
						.outputs(objectMapper.readValue(workFlowTaskDefinition.getOutputs(), new TypeReference<>() {
						})).parameters(objectMapper.readValue(workFlowTaskDefinition.getParameters(),
								new TypeReference<List<WorkFlowTaskParameter>>() {
								}).stream()
								.filter(workFlowTaskParameter -> WorkFlowTaskParameterScope.TASK
										.equals(workFlowTaskParameter.getScope()))
								.collect(Collectors.toList()))
						.workFlowChecker(Optional.ofNullable(workFlowTaskDefinition.getWorkFlowCheckerDefinition())
								.map(checker -> checker.getCheckWorkFlow().getId()).orElse(null))
						.nextWorkFlow(Optional.ofNullable(workFlowTaskDefinition.getWorkFlowCheckerDefinition())
								.map(checker -> checker.getNextWorkFlow().getId()).orElse(null))
						.build();
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
	}

}
