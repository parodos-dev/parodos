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

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.execution.dto.WorkFlowCheckerTaskRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowContextResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflow.execution.service.WorkFlowService;
import com.redhat.parodos.workflow.execution.validation.PubliclyVisible;
import com.redhat.parodos.workflow.task.log.service.WorkFlowLogService;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
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
 * Workflow controller to execute workflow and get status
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@CrossOrigin(origins = "*", maxAge = 1800)
@RestController
@Validated
@RequestMapping("/api/v1/workflows")
@Tag(name = "Workflow", description = "Operations about workflow")
public class WorkFlowController {

	private final WorkFlowService workFlowService;

	private final WorkFlowLogService workFlowLogService;

	public WorkFlowController(WorkFlowService workFlowService, WorkFlowLogService workFlowLogService) {
		this.workFlowService = workFlowService;
		this.workFlowLogService = workFlowLogService;
	}

	@Operation(summary = "Executes a workflow")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Accepted",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = WorkFlowExecutionResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content) })
	@PostMapping
	public ResponseEntity<WorkFlowExecutionResponseDTO> execute(
			@RequestBody @Valid WorkFlowRequestDTO workFlowRequestDTO) {
		WorkReport workReport = workFlowService.execute(workFlowRequestDTO);
		return ResponseEntity.ok(WorkFlowExecutionResponseDTO.builder()
				.workFlowExecutionId(WorkContextUtils.getMainExecutionId(workReport.getWorkContext()))
				.workStatus(workReport.getStatus()).build());
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
	public void updateWorkFlowCheckerTaskStatus(@PathVariable UUID workFlowExecutionId,
			@PathVariable String workFlowCheckerTaskName,
			@Valid @RequestBody WorkFlowCheckerTaskRequestDTO workFlowCheckerTaskRequestDTO) {
		workFlowService.updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName,
				workFlowCheckerTaskRequestDTO.getStatus());
	}

	@Operation(summary = "Restart a workflow execution with same parameters")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Accepted",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = WorkFlowExecutionResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content) })
	@PostMapping("/{workFlowExecutionId}/restart")
	public ResponseEntity<WorkFlowExecutionResponseDTO> restartWorkFlow(@PathVariable UUID workFlowExecutionId) {
		WorkReport workReport = workFlowService.restart(workFlowExecutionId);
		return ResponseEntity.ok(WorkFlowExecutionResponseDTO.builder()
				.workFlowExecutionId(WorkContextUtils.getMainExecutionId(workReport.getWorkContext()))
				.workStatus(workReport.getStatus()).build());
	}

	@Operation(summary = "Returns a workflow status")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = WorkFlowStatusResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
			@ApiResponse(responseCode = "304", description = "Not Modified", content = @Content) })
	@GetMapping("/{workFlowExecutionId}/status")
	public ResponseEntity<WorkFlowStatusResponseDTO> getStatus(@PathVariable UUID workFlowExecutionId) {
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = workFlowService.getWorkFlowStatus(workFlowExecutionId);
		return ResponseEntity.ok().eTag(String.valueOf(workFlowStatusResponseDTO.hashCode()))
				.body(workFlowStatusResponseDTO);
	}

	@Operation(summary = "Returns workflows by project id")
	@ApiResponses(
			value = {
					@ApiResponse(responseCode = "200", description = "Succeeded",
							content = { @Content(mediaType = "application/json",
									array = @ArraySchema(
											schema = @Schema(implementation = WorkFlowResponseDTO.class))) }),
					@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
					@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content) })
	@GetMapping()
	public ResponseEntity<List<WorkFlowResponseDTO>> getStatusByProjectId(
			@RequestParam(value = "projectId", required = false) UUID projectId) {
		return ResponseEntity.ok(projectId != null ? workFlowService.getWorkFlowsByProjectId(projectId)
				: workFlowService.getWorkFlows());
	}

	@Operation(summary = "Returns workflow context parameters")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = WorkFlowContextResponseDTO.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content) })
	@GetMapping("/{workFlowExecutionId}/context")
	public ResponseEntity<WorkFlowContextResponseDTO> getWorkflowParameters(@PathVariable UUID workFlowExecutionId,
			@NotEmpty @RequestParam List<WorkContextDelegate.@PubliclyVisible Resource> param) {
		WorkFlowContextResponseDTO responseDTO = workFlowService.getWorkflowParameters(workFlowExecutionId, param);
		return ResponseEntity.ok(responseDTO);
	}

	@Operation(summary = "Returns workflow execution log")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Succeeded",
					content = { @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
							schema = @Schema(implementation = String.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
			@ApiResponse(responseCode = "304", description = "Not Modified", content = @Content) })
	@GetMapping("/{workFlowExecutionId}/log")
	public ResponseEntity<String> getLog(@PathVariable UUID workFlowExecutionId,
			@RequestParam(required = false) String taskName) {
		String log = workFlowLogService.getLog(workFlowExecutionId, taskName);
		return ResponseEntity.ok().eTag(String.valueOf(log.hashCode())).body(log);
	}

}
