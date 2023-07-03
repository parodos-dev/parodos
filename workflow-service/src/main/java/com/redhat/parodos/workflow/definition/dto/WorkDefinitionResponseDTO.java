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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WorkDefinitionResponseDTO {

	private UUID id;

	private String name;

	private WorkType workType;

	private WorkFlowProcessingType processingType;

	private String author;

	private LinkedHashSet<WorkDefinitionResponseDTO> works;

	private Map<String, Map<String, Object>> parameters;

	private List<WorkFlowTaskOutput> outputs;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private UUID workFlowCheckerMappingDefinitionId;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String cronExpression;

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
		return WorkDefinitionResponseDTO.builder().id(wd.getId()).workType(WorkType.WORKFLOW).name(wd.getName())
				.parameterFromString(wd.getParameters()).processingType(wd.getProcessingType())
				.works(new LinkedHashSet<>()).numberOfWorkUnits(dependencies.size()).build();
	}

	public static WorkDefinitionResponseDTO fromWorkFlowTaskDefinition(WorkFlowTaskDefinition wdt) {
		WorkDefinitionResponseDTOBuilder builder = WorkDefinitionResponseDTO.builder().id(wdt.getId())
				.workType(WorkType.TASK).name(wdt.getName()).parameterFromString(wdt.getParameters())
				.outputs(WorkFlowDTOUtil.readStringAsObject(wdt.getOutputs(), new TypeReference<>() {
				}, List.of())).numberOfWorkUnits(0);

		if (wdt.getWorkFlowCheckerMappingDefinition() != null) {
			builder = builder.workFlowCheckerMappingDefinitionId(wdt.getWorkFlowCheckerMappingDefinition().getId());
		}

		return builder.build();
	}

	public static WorkDefinitionResponseDTO fromWorkFlowCheckerMappingDefinition(WorkFlowCheckerMappingDefinition wcd) {
		return WorkDefinitionResponseDTO.builder().id(wcd.getCheckWorkFlow().getId())
				.name(wcd.getCheckWorkFlow().getName()).parameterFromString(wcd.getCheckWorkFlow().getParameters())
				.processingType(wcd.getCheckWorkFlow().getProcessingType()).workType(WorkType.CHECKER)
				.numberOfWorkUnits(wcd.getCheckWorkFlow().getNumberOfWorks()).cronExpression(wcd.getCronExpression())
				.build();
	}

}
