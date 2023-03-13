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

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
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

	private String type;

	private String processingType;

	private String author;

	private Date createDate;

	private Date modifyDate;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<WorkFlowTaskParameter> parameters;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<WorkDefinitionResponseDTO> works;

}