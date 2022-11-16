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
package com.redhat.parodos.infrastructure;

import java.net.URI;
import java.net.URISyntaxException;
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
import com.redhat.parodos.workflow.execution.WorkFlowTransactionEntity;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.WorkFlowExecuteRequestDto;
import com.redhat.parodos.workflows.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.work.WorkReport;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Endpoint to get all the Task Workflows, kick off the execution of a Task Workflow and check the status of a Task
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@CrossOrigin(origins = "*", maxAge = 1800)
@RestController
@RequestMapping("/api/v1/workflows/infrastructures")
@Slf4j
public class InfrastructureWorkFlowController {
	
    private final InfrastructureWorkFlowService infrastructureWorkFlowService;
    
    public InfrastructureWorkFlowController(InfrastructureWorkFlowService infrastructureWorkFlowService) {
    	this.infrastructureWorkFlowService = infrastructureWorkFlowService;
    } 
    
    /**
     * Executes the WorkFlow
     * 
     * @param workFlowExecuteRequestDto
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<URI> executeWorkFlow(@RequestBody WorkFlowExecuteRequestDto workFlowExecuteRequestDto) {
    	WorkReport report = infrastructureWorkFlowService.execute(workFlowExecuteRequestDto);
    	if (report != null) {
    		try {
				return ResponseEntity.created(new URI("/api/v1/workflows/transactions/" + ((WorkFlowTransactionEntity)report.getWorkContext().get(WorkFlowConstants.WORKFLOW_EXECUTION_ENTITY_REFERENCE)).getId())).build();
			} catch (URISyntaxException e) {
				log.error("Workflow did not successfully execute. Please refer to the logs: {}", e.getMessage());
			}
    	}
    	return ResponseEntity.badRequest().build();
    }
    

    /**
	 * Gets All the IntrastructureTaskWorkFlows
	 * 
	 * @return Registered IntrastructureTaskWorkFlows
	 */
	@GetMapping("/")
	public ResponseEntity<Collection<String>> getInfraStructureTaskWorkFlows() {
		return ResponseEntity.ok(infrastructureWorkFlowService.getInfraStructureTaskWorkFlows(WorkFlowConstants.INFRASTRUCTURE_WORKFLOW));
	}
	
	
	/**
	 * Gets All the IntrastructureTaskWorkFlow WorkParameter
	 * 
	 * @return Registered IntrastructureTaskWorkFlow WorkParameter
	 */
	@GetMapping("/{id}/parameters")
	public ResponseEntity<List<WorkFlowTaskParameter>> getInfraStructureTaskWorkFlowDescription(@PathVariable String id) {
		return ResponseEntity.ok(infrastructureWorkFlowService.getWorkFlowParametersForWorkFlow(id));
	}
    
}
