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
package com.redhat.parodos.project.controller;

import java.util.UUID;

import com.redhat.parodos.project.dto.request.AccessStatusRequestDTO;
import com.redhat.parodos.project.dto.response.AccessStatusResponseDTO;
import com.redhat.parodos.project.service.ProjectAccessServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project access controller
 *
 * @author Annel Ketcha (Github: anludke)
 */
@CrossOrigin(origins = "*", maxAge = 1800)
@RestController
@Validated
@RequestMapping("/api/v1/projects/access")
@Tag(name = "Project Access", description = "Operations about project access")
public class ProjectAccessController {

	private final ProjectAccessServiceImpl projectAccessService;

	public ProjectAccessController(ProjectAccessServiceImpl projectAccessService) {
		this.projectAccessService = projectAccessService;
	}

	@Operation(summary = "Returns status about a specified project access request")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = AccessStatusResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content) })
	@GetMapping("/{id}/status")
	public ResponseEntity<AccessStatusResponseDTO> getProjectAccessStatus(@PathVariable UUID id) {
		return ResponseEntity.ok(projectAccessService.getProjectAccessStatusById(id));
	}

	@Operation(summary = "Update status of a specified project access request")
	@ApiResponses(
			value = { @ApiResponse(responseCode = "204", description = "Updated successfully", content = @Content),
					@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
					@ApiResponse(responseCode = "404", description = "Not found", content = @Content) })
	@PostMapping("/{id}/status")
	public ResponseEntity<String> updateProjectAccessStatus(@PathVariable UUID id,
			@RequestBody AccessStatusRequestDTO accessStatusRequestDTO) {
		projectAccessService.updateProjectAccessStatusById(id, accessStatusRequestDTO);
		return ResponseEntity.noContent().build();
	}

}
