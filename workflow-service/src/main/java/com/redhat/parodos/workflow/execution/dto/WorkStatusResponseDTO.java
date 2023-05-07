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
package com.redhat.parodos.workflow.execution.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.redhat.parodos.workflow.enums.ParodosWorkStatus;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Workflow response dto
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkStatusResponseDTO {

	private String name;

	private WorkType type;

	private ParodosWorkStatus status;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<WorkStatusResponseDTO> works;

	@JsonIgnore
	private WorkFlowExecution workExecution;

	@JsonIgnore
	private Integer numberOfWorks;

}
