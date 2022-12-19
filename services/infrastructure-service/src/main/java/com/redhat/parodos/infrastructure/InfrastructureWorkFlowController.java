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

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.WorkFlowExecuteRequestDto;
import com.redhat.parodos.workflows.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.workflow.ParallelFlowReport;

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
    @SuppressWarnings("unchecked")
	@PostMapping("/")
    public ResponseEntity<List<UUID>> executeWorkFlow(@RequestBody WorkFlowExecuteRequestDto workFlowExecuteRequestDto) {
    	WorkReport report = infrastructureWorkFlowService.execute(workFlowExecuteRequestDto);
    	if (report != null) {
    		if (report instanceof ParallelFlowReport) {
    			for (WorkReport innerReport : ((ParallelFlowReport)report).getReports() ) {
        			if (innerReport.getClass().equals(ParallelFlowReport.class)) {
        				return ResponseEntity.ok((List<UUID>)innerReport.getWorkContext().get(WorkFlowConstants.WORKFLOW_EXECUTION_ENTITY_REFERENCES));
        			}
        		}
    		}
    		return ResponseEntity.ok((List<UUID>)report.getWorkContext().get(WorkFlowConstants.WORKFLOW_EXECUTION_ENTITY_REFERENCES));
    	}
    	return ResponseEntity.badRequest().build();
    }
    
    /*
     * Recursively process each report to ensure all the sub reports of each ParallelFlowReport are processed
     */
//    private void processParallelReport(WorkReport report, List<UUID> createdURI) {
//    	//check if the report is a Parallel
//    	if (report.getClass().equals(ParallelFlowReport.class)) {
//    		for (WorkReport innerReport : ((ParallelFlowReport)report).getReports() ) {
//    			if (innerReport.getClass().equals(ParallelFlowReport.class)) {
//    				processParallelReport(innerReport, createdURI);
//    			}
// 
//				createdURI.add(((WorkFlowTransactionDTO)innerReport.getWorkContext().get(WorkFlowConstants.WORKFLOW_EXECUTION_ENTITY_REFERENCE)).getId());
//    		}
//    		
//    	}
//    	log.info("Got Transaction: {}", ((WorkFlowTransactionDTO)report.getWorkContext().get(WorkFlowConstants.WORKFLOW_EXECUTION_ENTITY_REFERENCE)).getId());
//    	createdURI.add(((WorkFlowTransactionDTO)report.getWorkContext().get(WorkFlowConstants.WORKFLOW_EXECUTION_ENTITY_REFERENCE)).getId());
//    }
//    

    /**
	 * Gets All the InfrastructureTaskWorkFlows
	 * 
	 * @return Registered InfrastructureTaskWorkFlows
	 */
	@GetMapping("/")
	public ResponseEntity<Collection<String>> getInfraStructureTaskWorkFlows() {
		return ResponseEntity.ok(infrastructureWorkFlowService.getInfrastructureTaskWorkFlows(WorkFlowConstants.INFRASTRUCTURE_WORKFLOW));
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
