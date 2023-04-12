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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Workflow task definition response dto
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class WorkDefinitionResponseDTO {

	private String id;

	private String name;

	private String workType;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private WorkFlowProcessingType processingType;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String author;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<WorkDefinitionResponseDTO> works;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Map<String, Map<String, Object>> parameters;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<WorkFlowTaskOutput> outputs;

	@JsonIgnore
	private Integer numberOfWorkUnits;

	public static class WorkDefinitionResponseDTOBuilder {

		public WorkDefinitionResponseDTOBuilder parameterFromString(String parameters) {
			if (parameters == null) {
				return this;
			}
			this.parameters(WorkFlowDTOUtil.readStringAsObject(parameters,
					new TypeReference<Map<String, Map<String, Object>>>() {
					}, Map.of()));
			return this;
		}

	}

	public static WorkDefinitionResponseDTO fromWorkFlowDefinitionEntity(WorkFlowDefinition wd,
			List<WorkFlowWorkDefinition> dependencies) {
		return WorkDefinitionResponseDTO.builder().id(wd.getId().toString()).workType(WorkType.WORKFLOW.name())
				.name(wd.getName()).parameterFromString(wd.getParameters()).processingType(wd.getProcessingType())
				.works(new ArrayList<>()).numberOfWorkUnits(dependencies.size()).build();
	}

	public static WorkDefinitionResponseDTO fromWorkFlowTaskDefinition(WorkFlowTaskDefinition wdt) {
		return WorkDefinitionResponseDTO.builder().id(wdt.getId().toString()).workType(WorkType.TASK.name())
				.name(wdt.getName()).parameterFromString(wdt.getParameters())
				.outputs(WorkFlowDTOUtil.readStringAsObject(wdt.getOutputs(), new TypeReference<>() {
				}, List.of())).build();
	}

}
