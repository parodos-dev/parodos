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
package com.redhat.parodos.workflow.execution.continuation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * When the application starts up it will run any workflows in Progress @see
 * Status.IN_PROGRESS
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Service
@Slf4j
public class WorkFlowContinuationService {

	private final WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private final WorkFlowRepository workFlowRepository;

	private final WorkFlowTaskRepository workFlowTaskRepository;

	private final WorkFlowServiceImpl workFlowService;

	private final ObjectMapper objectMapper;

	public WorkFlowContinuationService(WorkFlowDefinitionRepository workFlowDefinitionRepository,
			WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository, WorkFlowRepository workFlowRepository,
			WorkFlowTaskRepository workFlowTaskRepository, WorkFlowServiceImpl workFlowService,
			ObjectMapper objectMapper) {
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
		this.workFlowRepository = workFlowRepository;
		this.workFlowTaskRepository = workFlowTaskRepository;
		this.workFlowService = workFlowService;
		this.objectMapper = objectMapper;
	}

	/**
	 * When the application starts up, get all workflows with Status.IN_PROGRESS and
	 * execute them
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void workFlowRunAfterStartup() {
		log.info("Looking up all IN PROGRESS workflows for ");
		List<WorkFlowExecution> workFlowExecutions = workFlowRepository.findAll();
		log.info("Number of IN PROGRESS workflows for : {}", workFlowExecutions.size());
		workFlowExecutions.stream()
				.filter(workFlowExecution -> WorkFlowStatus.IN_PROGRESS == workFlowExecution.getStatus())
				.forEach(workFlowExecution -> {
					WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository
							.findById(workFlowExecution.getWorkFlowDefinitionId()).get();
					List<WorkFlowTaskExecution> workFlowTaskExecutions = workFlowTaskRepository
							.findByWorkFlowExecutionId(workFlowExecution.getId());
					Map<String, Map<String, String>> workFlowTaskArguments = new HashMap<>();
					workFlowTaskExecutions.forEach(workFlowTaskExecution -> {
						try {
							workFlowTaskArguments.put(workFlowTaskDefinitionRepository
									.findById(workFlowTaskExecution.getWorkFlowTaskDefinitionId()).get().getName(),
									objectMapper.readValue(workFlowTaskExecution.getArguments(), new TypeReference<>() {
									}));
						}
						catch (JsonProcessingException e) {
							throw new RuntimeException(e);
						}
					});
					Map<String, String> workFlowArguments;
					workFlowArguments = WorkFlowDTOUtil.readStringAsObject(workFlowExecution.getArguments(),
							new TypeReference<>() {
							}, Map.of());
					workFlowService.execute(workFlowExecution.getProjectId().toString(), workFlowDefinition.getName(),
							workFlowTaskArguments, workFlowArguments);
				});
	}

}
