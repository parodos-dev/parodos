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
package com.redhat.parodos.workflow.execution.controller;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.execution.dto.WorkFlowCheckerTaskRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflow.execution.service.WorkFlowService;
import com.redhat.parodos.workflow.option.WorkFlowOptions;
import com.redhat.parodos.workflows.work.WorkReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

/**
 * Workflow controller to execute workflow and get status
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@CrossOrigin(origins = "*", maxAge = 1800)
@RestController
@RequestMapping("/api/v1/workflows")
@Tag(name = "Workflow", description = "Operations about workflow")
public class WorkFlowController {

	private final WorkFlowService workFlowService;

	public WorkFlowController(WorkFlowService workFlowService) {
		this.workFlowService = workFlowService;
	}

	@Operation(summary = "Executes a workflow")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = WorkFlowResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content) })
	@PostMapping
	public ResponseEntity<WorkFlowResponseDTO> execute(@RequestBody @Valid WorkFlowRequestDTO workFlowRequestDTO) {
		WorkReport workReport = workFlowService.execute(workFlowRequestDTO);
		if (workReport == null) {
			return ResponseEntity.status(500).build();
		}

		return ResponseEntity.ok(WorkFlowResponseDTO.builder()
				.workFlowExecutionId(WorkContextDelegate.read(workReport.getWorkContext(),
						WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ID).toString())
				.workFlowOptions((WorkFlowOptions) WorkContextDelegate.read(workReport.getWorkContext(),
						WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
						WorkContextDelegate.Resource.WORKFLOW_OPTIONS))
				.build());

	}

	@Operation(summary = "Updates a workflow checker task status")
	@ApiResponses(
			value = {
					@ApiResponse(responseCode = "200", description = "Succeeded",
							content = { @Content(mediaType = "application/json",
									schema = @Schema(implementation = void.class)) }),
					@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
					@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
					@ApiResponse(responseCode = "404", description = "Not found", content = @Content) })
	@PostMapping("/{workFlowExecutionId}/checkers/{workFlowCheckerTaskName}")
	public void updateCheckerTaskStatus(@PathVariable String workFlowExecutionId,
			@PathVariable String workFlowCheckerTaskName,
			@Valid @RequestBody WorkFlowCheckerTaskRequestDTO workFlowCheckerTaskRequestDTO) {
		workFlowService.updateCheckerTaskStatus(UUID.fromString(workFlowExecutionId), workFlowCheckerTaskName,
				workFlowCheckerTaskRequestDTO.getStatus());
	}

	@Operation(summary = "Returns a workflow status")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = WorkFlowStatusResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content) })
	@GetMapping("/{id}/status")
	public ResponseEntity<WorkFlowStatusResponseDTO> getStatus(@PathVariable String id) {
		return null;
	}

}
