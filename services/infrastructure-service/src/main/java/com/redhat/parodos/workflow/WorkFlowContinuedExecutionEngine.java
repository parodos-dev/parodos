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
package com.redhat.parodos.workflow;

import com.redhat.parodos.workflow.execution.Status;
import com.redhat.parodos.workflow.execution.transaction.WorkFlowTransactionRepository;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * When the application starts up it will run any workflows in Progress @see Status.IN_PROGRESS
 * 
 * @author Richard Wang (Github: richardw98)
 *
 */
@Component
@Slf4j
public class WorkFlowContinuedExecutionEngine {

    private final WorkFlowTransactionRepository workFlowTransactionRepository;

    private final WorkFlowEngine workFlowEngine;

    private final WorkFlowDelegate workFlowDelegate;

    public WorkFlowContinuedExecutionEngine(WorkFlowTransactionRepository workFlowTransactionRepository, WorkFlowEngine workFlowEngine, WorkFlowDelegate workFlowDelegate) {
        this.workFlowTransactionRepository = workFlowTransactionRepository;
        this.workFlowEngine = workFlowEngine;
        this.workFlowDelegate = workFlowDelegate;
    }

    /**
     * When the application starts up, get all workflows with Status.IN_PROGRESS and execute them
     */
    @EventListener(ApplicationReadyEvent.class)
    public void workFlowRunAfterStartup() {
        log.info("Looking up all IN PROGRESS workflows for excution");
        workFlowTransactionRepository.findAll()
                .stream()
                .filter(entity -> Status.IN_PROGRESS.name().equals(entity.getStatus()))
                .forEach(workFlowTransactionEntity -> {
	                // this logic is the same for all workflows
	                log.debug("Resuming: {}", workFlowTransactionEntity.getWorkFlowId());
                    WorkContext context = new WorkContext();
                    context.put(WorkFlowConstants.WORKFLOW_TYPE, workFlowTransactionEntity.getWorkFlowType());
                    WorkFlow workFlow = workFlowDelegate.getWorkFlowById(workFlowTransactionEntity.getWorkFlowId());
                    workFlowEngine.executeWorkFlows(context, workFlow);
                });
        log.info("All IN PROGRESS Workflows can be executed");
    }
}
