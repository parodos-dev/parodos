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
package com.redhat.parodos.workflow.definition.controller;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.Valid;

import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkParameterValueRequestDTO;
import com.redhat.parodos.workflow.definition.dto.WorkParameterValueResponseDTO;
import com.redhat.parodos.workflow.definition.parameter.WorkParameterService;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Workflow definition controller
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@CrossOrigin(origins = "*", maxAge = 1800)
@RestController
@Validated
@RequestMapping("/api/v1/workflowdefinitions")
@Tag(name = "Workflow Definition", description = "Operations about workflow definition")
public class WorkFlowDefinitionController {

	private final WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	private final WorkParameterService workParameterService;

	public WorkFlowDefinitionController(WorkFlowDefinitionServiceImpl workFlowDefinitionService,
			WorkParameterService workParameterService) {
		this.workFlowDefinitionService = workFlowDefinitionService;
		this.workParameterService = workParameterService;
	}

	@Operation(summary = "Returns a list of workflow definition")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							array = @ArraySchema(
									schema = @Schema(implementation = WorkFlowDefinitionResponseDTO.class))) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content) })
	@GetMapping
	public ResponseEntity<List<WorkFlowDefinitionResponseDTO>> getWorkFlowDefinitions(
			@RequestParam(required = false) String name) {
		if (Objects.isNull(name) || name.isEmpty()) {
			return ResponseEntity.ok(workFlowDefinitionService.getWorkFlowDefinitions());
		}
		return ResponseEntity.ok(List.of(workFlowDefinitionService.getWorkFlowDefinitionByName(name)));
	}

	@Operation(summary = "Returns information about a workflow definition by id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = WorkFlowDefinitionResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content) })
	@GetMapping("/{id}")
	public ResponseEntity<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionById(@PathVariable UUID id) {
		WorkFlowDefinitionResponseDTO response = workFlowDefinitionService.getWorkFlowDefinitionById(id);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Returns updated parameter value")
	@Parameters(value = {
			@Parameter(name = "workflowDefinitionName", description = "workflow Definition Name",
					example = "complexWorkFlow"),
			@Parameter(name = "valueProviderName",
					description = "valueProvider Name. It can be referenced to 'valueProviderName' in [GET /getWorkFlowDefinitions](#/Workflow%20Definition/getWorkFlowDefinitions)",
					example = "complexWorkFlowValueProvider") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							array = @ArraySchema(
									schema = @Schema(implementation = WorkParameterValueResponseDTO.class))) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content) })
	@PostMapping("/{workflowDefinitionName}/parameters/update/{valueProviderName}")
	public ResponseEntity<List<WorkParameterValueResponseDTO>> updateParameter(
			@PathVariable String workflowDefinitionName, @PathVariable String valueProviderName,
			@RequestBody @Valid List<WorkParameterValueRequestDTO> workParameterValueRequestDTOS) {
		return ResponseEntity.ok(workParameterService.getValues(workflowDefinitionName, valueProviderName,
				workParameterValueRequestDTOS));
	}

}
