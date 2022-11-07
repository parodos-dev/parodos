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
package com.redhat.parodos.infrastructure.task;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.redhat.parodos.infrastructure.existing.ExistingInfrastructureEntity;
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
@RequestMapping("/v1/tasks")
@Slf4j
public class InfrastructureTaskController {
	
    private final InfrastructureTaskService infrastructureTaskService;
    
    public InfrastructureTaskController(InfrastructureTaskService infrastructureTaskService) {
    	this.infrastructureTaskService = infrastructureTaskService;
    } 
    
    @PostMapping("/")
    public ResponseEntity<URI> executeWorkFlow(@RequestBody InfrastructureTaskRequestDto infrastructureTaskRequestDto) {
    	ExistingInfrastructureEntity entity = infrastructureTaskService.executeInfrastructureTasks(infrastructureTaskRequestDto.getWorkFlowName(),infrastructureTaskRequestDto.getRequestDetails());
    	if (entity != null) {
    		try {
				return ResponseEntity.created(new URI("/v1/infrastructures/existing/" + entity.getId())).build();
			} catch (URISyntaxException e) {
				log.error("Workflow did not successfully execute. Please refer to the logs: {}", e.getMessage());
			}
    	}
    	return ResponseEntity.badRequest().build();
    }
    
    @GetMapping("/")
    public ResponseEntity<Collection<String>> getInfrastructureWorkFlows() {
    	return ResponseEntity.ok(infrastructureTaskService.getInfraStructureTaskWorkFlows());
    }
    
}
