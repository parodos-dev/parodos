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

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.redhat.parodos.workflow.option.WorkFlowOptions;
import com.redhat.parodos.workflows.work.WorkStatus;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WorkFlowResponseDTO {

	private UUID workFlowExecutionId;

	private UUID projectId;

	private String workFlowName;

	private WorkFlowOptions workFlowOptions;

	private WorkStatus workStatus;

	private String startDate;

	private String endDate;

	private String createUser;

}
