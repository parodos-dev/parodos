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
package com.redhat.parodos.project.dto.response;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * access response DTO
 *
 * @author Annel Ketcha (Github: anludke)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessResponseDTO {

	private UUID accessRequestId;

	private ProjectDTO project;

	private List<String> approvalSentTo;

	private String escalationSentTo;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ProjectDTO {

		private UUID id;

		private String name;

		private Date createdDate;

		private String createdBy;

	}

}
