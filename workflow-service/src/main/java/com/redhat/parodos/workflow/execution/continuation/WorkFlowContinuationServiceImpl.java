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

import java.util.List;
import java.util.UUID;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflows.work.WorkContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * When the application starts up it will run any workflows in Progress @see
 * Status.IN_PROGRESS
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
@Service
@Slf4j
public class WorkFlowContinuationServiceImpl implements WorkFlowContinuationService {

	private final WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private final WorkFlowRepository workFlowRepository;

	private final AsyncWorkFlowContinuerImpl asyncWorkFlowContinuerImpl;

	public WorkFlowContinuationServiceImpl(WorkFlowDefinitionRepository workFlowDefinitionRepository,
			WorkFlowRepository workFlowRepository, AsyncWorkFlowContinuerImpl asyncWorkFlowContinuerImpl) {
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowRepository = workFlowRepository;
		this.asyncWorkFlowContinuerImpl = asyncWorkFlowContinuerImpl;
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
				.filter(workFlowExecution -> WorkFlowStatus.IN_PROGRESS == workFlowExecution.getStatus()
						&& workFlowExecution.getMasterWorkFlowExecution() == null)
				.forEach(workFlowExecution -> {
					WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepository
							.findById(workFlowExecution.getWorkFlowDefinitionId()).get();

					// continue with the same execution id
					continueWorkFlow(workFlowExecution.getProjectId().toString(), workFlowDefinition.getName(),
							workFlowExecution.getWorkFlowExecutionContext().getWorkContext(),
							workFlowExecution.getId());
				});
	}

	public void continueWorkFlow(String projectId, String workflowName, WorkContext workContext, UUID executionId) {
		asyncWorkFlowContinuerImpl.executeAsync(projectId, workflowName, workContext, executionId);
	}

}
