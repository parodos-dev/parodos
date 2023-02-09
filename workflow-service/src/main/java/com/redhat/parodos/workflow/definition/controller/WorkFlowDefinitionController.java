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

import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Workflow definition controller
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@CrossOrigin(origins = "*", maxAge = 1800)
@RestController
@RequestMapping("/api/v1/workflowdefinitions")
public class WorkFlowDefinitionController {

	private final WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	public WorkFlowDefinitionController(WorkFlowDefinitionServiceImpl workFlowDefinitionService) {
		this.workFlowDefinitionService = workFlowDefinitionService;
	}

	@GetMapping
	public ResponseEntity<List<WorkFlowDefinitionResponseDTO>> getWorkFlowDefinitions() {
		return ResponseEntity.ok(workFlowDefinitionService.getWorkFlowDefinitions());
	}

	@GetMapping("/{id}")
	public ResponseEntity<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionById(@PathVariable String id) {
		return ResponseEntity.ok(workFlowDefinitionService.getWorkFlowDefinitionById(UUID.fromString(id)));
	}

}
