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
package com.redhat.parodos.infrastructure.workflowchecker;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.redhat.parodos.workflows.WorkFlowCheckResponseDto;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

/**
 * Endpoint to execute workflow checks
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@CrossOrigin(origins = "*", maxAge = 1800)
@RestController
@RequestMapping("/api/v1/workflowchecker/")
public class WorkFlowCheckerController {
	
	private final WorkFlowCheckerService workFlowCheckerService;
	
	public WorkFlowCheckerController(WorkFlowCheckerService workFlowCheckerService) {
		this.workFlowCheckerService = workFlowCheckerService;
	}

	/**
	 * WorkFlow checkers require a @see WorkFlowTransactionEntity Id to execute
	 * 
	 * @param id of the @see WorkFlowTransactionEntity. This value will contains all the information required to execute the @see WorkFlowChecker
	 * @return @see WorkFlowCheckResponseDto which contains the next @see WorkFlow to run, if it can be executed and any arguments to use that were obtained from the downstream process the @see WorkFlowChecker was calling
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/{id}")
    public ResponseEntity<WorkFlowCheckResponseDto> executeChecker(@PathVariable String id) {
		WorkReport report = workFlowCheckerService.execute(id);
		if (report.getStatus().equals(WorkStatus.COMPLETED)) {
			return ResponseEntity.ok(WorkFlowCheckResponseDto.builder()
					.nextWorkFlowToRun((String)report.getWorkContext().get(WorkFlowConstants.NEXT_WORKFLOW_ID))
					.readyToRun(true)
					.argumentsForNextWorkFlow((Map<String,String>)report.getWorkContext().get(WorkFlowConstants.NEXT_WORKFLOW_ARGUMENTS))
					.build());
		}
		return ResponseEntity.ok(WorkFlowCheckResponseDto.builder().nextWorkFlowToRun((String)report.getWorkContext().get(WorkFlowConstants.NEXT_WORKFLOW_ID)).readyToRun(false).build());
    }

}
