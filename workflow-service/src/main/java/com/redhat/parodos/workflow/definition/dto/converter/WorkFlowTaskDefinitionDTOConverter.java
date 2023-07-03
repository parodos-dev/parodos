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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.definition.dto.WorkDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.exceptions.WorkflowDefinitionException;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

/**
 * Workflow task definition converter
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

public class WorkFlowTaskDefinitionDTOConverter
		implements Converter<List<WorkFlowTaskDefinition>, Set<WorkDefinitionResponseDTO>> {

	@Override
	public Set<WorkDefinitionResponseDTO> convert(
			MappingContext<List<WorkFlowTaskDefinition>, Set<WorkDefinitionResponseDTO>> context) {
		ObjectMapper objectMapper = new ObjectMapper();
		List<WorkFlowTaskDefinition> source = context.getSource();
		return source.stream().map(workFlowTaskDefinition -> {
			try {
				return WorkDefinitionResponseDTO.builder().id(workFlowTaskDefinition.getId())
						.name(workFlowTaskDefinition.getName())
						.outputs(objectMapper.readValue(workFlowTaskDefinition.getOutputs(), new TypeReference<>() {
						})).parameterFromString(workFlowTaskDefinition.getParameters()).build();
			}
			catch (JsonProcessingException e) {
				throw new WorkflowDefinitionException(e);
			}
		}).collect(Collectors.toSet());
	}

}
