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
package com.redhat.parodos.assessment;

import java.util.Collection;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redhat.parodos.infrastructure.option.InfrastructureOptions;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.WorkFlowExecuteRequestDto;
import com.redhat.parodos.workflows.WorkFlowTaskParameter;

import lombok.extern.slf4j.Slf4j;

/**
 * Client facing entry point for submitting a request to assess a workload and determine potential InfrastructureOption(s)
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@CrossOrigin(origins = "*", maxAge = 1800)
@RestController
@RequestMapping("/api/v1/workflows/assessments")
@Slf4j
public class AssessmentWorkFlowController {
	
	private final AssessmentWorkFlowService assessmentWorkFlowService;
	
	public AssessmentWorkFlowController(AssessmentWorkFlowService assessmentWorkFlowService) {
		this.assessmentWorkFlowService = assessmentWorkFlowService;
	}

	/**
	 * Any request for Assessment will be posted to this endpoint
	 * 
	 * @param workFlowExecuteRequestDto contains all the information needed to perform the assessment
	 * @return InfrastructureOptions containing details on the current Infrastructure and any upgrade, migration or new infrastructure options
	 */
	@PostMapping("/")
	public ResponseEntity<InfrastructureOptions> assessApplication(@RequestBody WorkFlowExecuteRequestDto workFlowExecuteRequestDto) {
		log.debug("Running a Assessment using AssessmentRequest: {}", workFlowExecuteRequestDto.toString());
		return ResponseEntity.ok((InfrastructureOptions)assessmentWorkFlowService.execute(workFlowExecuteRequestDto).getWorkContext().get(WorkFlowConstants.RESULTING_INFRASTRUCTURE_OPTIONS));
	}
	
	/**
	 * Gets All the AssessmentWorkFlows
	 * 
	 * @return Registered AssessmentWorkFlows
	 */
	@GetMapping("/")
	public ResponseEntity<Collection<String>> getAssessmentWorkFlows() {
		return ResponseEntity.ok(assessmentWorkFlowService.getAssessmentWorkFlowIds());
	}
	
	
	/**
	 * Gets All the AssessmentWorkFlows
	 * 
	 * @return Registered AssessmentWorkFlows
	 */
	@GetMapping("/{id}/parameters")
	public ResponseEntity<List<WorkFlowTaskParameter>> getAssessmentWorkFlowDescription(@PathVariable String id) {
		return ResponseEntity.ok(assessmentWorkFlowService.getWorkFlowParametersForWorkFlow(id));
	}
}
