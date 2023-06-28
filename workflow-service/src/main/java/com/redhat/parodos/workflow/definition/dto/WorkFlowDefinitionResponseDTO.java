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

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Workflow definition response dto
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@EqualsAndHashCode
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlowDefinitionResponseDTO {

	private UUID id;

	private String name;

	private WorkFlowType type;

	private WorkFlowProcessingType processingType;

	private String author;

	private Date createDate;

	private Date modifyDate;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Map<String, Map<String, Object>> parameters;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private WorkFlowPropertiesDefinitionDTO properties;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Set<WorkDefinitionResponseDTO> works;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String fallbackWorkflow;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String cronExpression;

	public static class WorkFlowDefinitionResponseDTOBuilder {

		public WorkFlowDefinitionResponseDTOBuilder parameterFromString(String parameters) {
			if (parameters == null) {
				return this;
			}
			this.parameters(WorkFlowDTOUtil.readStringAsObject(parameters,
					new TypeReference<Map<String, Map<String, Object>>>() {
					}, Map.of()));
			return this;
		}

	}

	public static WorkFlowDefinitionResponseDTO fromEntity(WorkFlowDefinition workFlowDefinition,
			Set<WorkDefinitionResponseDTO> works) {
		WorkFlowDefinitionResponseDTOBuilder builder = WorkFlowDefinitionResponseDTO.builder()
				.id(workFlowDefinition.getId()).name(workFlowDefinition.getName())
				.properties(WorkFlowPropertiesDefinitionDTO.fromEntity(workFlowDefinition.getProperties()))
				.parameterFromString(workFlowDefinition.getParameters()).author(workFlowDefinition.getAuthor())
				.createDate(workFlowDefinition.getCreateDate()).modifyDate(workFlowDefinition.getModifyDate())
				.type(workFlowDefinition.getType()).processingType(workFlowDefinition.getProcessingType()).works(works)
				.fallbackWorkflow(Optional.ofNullable(workFlowDefinition.getFallbackWorkFlowDefinition())
						.map(WorkFlowDefinition::getName).orElse(null));

		if (workFlowDefinition.getCheckerWorkFlowDefinition() != null) {
			builder = builder.cronExpression(workFlowDefinition.getCheckerWorkFlowDefinition().getCronExpression());
		}
		return builder.build();
	}

}