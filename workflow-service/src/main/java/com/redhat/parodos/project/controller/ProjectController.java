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

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import com.redhat.parodos.project.dto.request.AccessRequestDTO;
import com.redhat.parodos.project.dto.request.ProjectRequestDTO;
import com.redhat.parodos.project.dto.request.UserRoleRequestDTO;
import com.redhat.parodos.project.dto.response.AccessResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectResponseDTO;
import com.redhat.parodos.project.dto.response.ProjectUserRoleResponseDTO;
import com.redhat.parodos.project.service.ProjectServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Project controller
 *
 * @author Annel Ketcha (Github: anludke)
 */

@CrossOrigin(origins = "*", maxAge = 1800)
@RestController
@Validated
@RequestMapping("/api/v1/projects")
@Tag(name = "Project", description = "Operations about project")
public class ProjectController {

	private final ProjectServiceImpl projectService;

	public ProjectController(ProjectServiceImpl projectService) {
		this.projectService = projectService;
	}

	@Operation(summary = "Creates a new project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Created",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = ProjectResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content) })
	@PostMapping
	public ResponseEntity<ProjectResponseDTO> createProject(@Valid @RequestBody ProjectRequestDTO projectRequestDTO) {
		ProjectResponseDTO projectResponseDTO = projectService.createProject(projectRequestDTO);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(projectResponseDTO.getId()).toUri();
		return ResponseEntity.created(location).body(projectResponseDTO);
	}

	@Operation(summary = "Returns a list of project")
	@ApiResponses(
			value = {
					@ApiResponse(responseCode = "200", description = "Succeeded",
							content = { @Content(mediaType = "application/json",
									array = @ArraySchema(
											schema = @Schema(implementation = ProjectResponseDTO.class))) }),
					@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
					@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
					@ApiResponse(responseCode = "304", description = "Not Modified", content = @Content) })
	@GetMapping
	public ResponseEntity<List<ProjectResponseDTO>> getProjects() {
		List<ProjectResponseDTO> projects = projectService.getProjects();
		return ResponseEntity.ok().eTag(String.valueOf(projects.hashCode())).body(projects);
	}

	@Operation(summary = "Returns information about a specified project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = ProjectResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content),
			@ApiResponse(responseCode = "304", description = "Not Modified", content = @Content) })
	@GetMapping("/{id}")
	public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable UUID id) {
		ProjectResponseDTO projectResponseDTO = projectService.getProjectById(id);
		return ResponseEntity.ok().eTag(String.valueOf(projectResponseDTO.hashCode())).body(projectResponseDTO);
	}

	@Operation(summary = "Update user roles in project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = ProjectUserRoleResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content) })
	@PostMapping("/{id}/users")
	public ResponseEntity<ProjectUserRoleResponseDTO> updateUserRolesToProject(@PathVariable UUID id,
			@RequestBody List<UserRoleRequestDTO> userRoleRequestDTOs) {
		ProjectUserRoleResponseDTO projectResponseDTO = projectService.updateUserRolesToProject(id,
				userRoleRequestDTOs);
		return ResponseEntity.ok(projectResponseDTO);
	}

	@Operation(summary = "Remove users from project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = ProjectUserRoleResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content) })
	@DeleteMapping("/{id}/users")
	public ResponseEntity<ProjectUserRoleResponseDTO> removeUsersFromProject(@PathVariable UUID id,
			@RequestBody @NotEmpty List<String> usernames) {
		ProjectUserRoleResponseDTO projectUserRoleResponseDTO = projectService.removeUsersFromProject(id, usernames);
		return ResponseEntity.ok(projectUserRoleResponseDTO);
	}

	@Operation(summary = "Request user access to project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = AccessResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content) })
	@PostMapping("/{id}/access")
	public ResponseEntity<AccessResponseDTO> createAccessRequestToProject(@PathVariable UUID id,
			@Valid @RequestBody AccessRequestDTO accessRequestDTO) {
		AccessResponseDTO accessResponseDTO = projectService.createAccessRequestToProject(id, accessRequestDTO);
		return ResponseEntity.ok(accessResponseDTO);
	}

}
