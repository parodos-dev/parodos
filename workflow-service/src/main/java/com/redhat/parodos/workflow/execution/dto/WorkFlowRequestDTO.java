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
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Workflow request dto
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlowRequestDTO {

	private String projectId;

	private String workFlowName;

	private List<WorkRequestDTO.ArgumentRequestDTO> arguments;

	private List<WorkRequestDTO> works;

	public WorkFlowRequestDTO.WorkRequestDTO findFirstWorkByName(String name) {
		return findWorks(works, name).stream().findFirst().orElse(null);
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class WorkRequestDTO {

		String workName;

		List<ArgumentRequestDTO> arguments;

		String type;

		// recursive works
		List<WorkRequestDTO> works;

		public WorkFlowRequestDTO.WorkRequestDTO findFirstWorkByName(String name) {
			return findWorks(works, name).stream().findFirst().orElse(null);
		}

		@Data
		@Builder
		@AllArgsConstructor
		@NoArgsConstructor
		public static class ArgumentRequestDTO {

			String key;

			String value;

		}

	}

	private static List<WorkRequestDTO> findWorks(List<WorkRequestDTO> works, String name) {
		return Optional.ofNullable(works)
				.map(ws -> ws.stream().filter(work -> name.equals(work.getWorkName())).collect(Collectors.toList()))
				.orElse(List.of());
	}

}
