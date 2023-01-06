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

import com.redhat.parodos.security.SecurityUtils;
import com.redhat.parodos.workflow.execution.Status;
import com.redhat.parodos.workflow.execution.transaction.WorkFlowTransactionDTO;
import com.redhat.parodos.workflow.execution.transaction.WorkFlowTransactionEntity;
import com.redhat.parodos.workflow.execution.transaction.WorkFlowTransactionRepository;
import com.redhat.parodos.workflow.execution.transaction.WorkTransactionService;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.engine.WorkFlowEngineBuilder;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Executes a @see WorkFlow
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 */
@Component
@Slf4j
public class WorkFlowEngine {

    private final WorkTransactionService workTransactionService;
    private final WorkFlowTransactionRepository workFlowTransactionRepository;
    private final SecurityUtils securityUtils;

    public WorkFlowEngine(WorkTransactionService workTransactionService,
                          WorkFlowTransactionRepository workFlowTransactionRepository,
                          SecurityUtils securityUtils) {
        this.workTransactionService = workTransactionService;
        this.workFlowTransactionRepository = workFlowTransactionRepository;
        this.securityUtils = securityUtils;
    }

    /**
     * Runs all the All Tasks that have be packaged into a WorkFlow with the provided Context. At the end of execution a WorkFlowTransaction reference is created.
     * This is intended to keep track of the @see WorFlowTask execution for audit purposes, but to provide a link to a @see WorkFlowChecker.
     * <p>
     * When a @see WorkFlowChecker is linked to a WorkFlowTransaction it implies the WorkFlowTask that just executed has ended in waiting state (i.e:
     *
     * @param workContext common objects passed across Work units. Each task stores the result
     * @param workFlow    the list of steps that need to be done to create the InfrastructureOption
     * @return workReport indicating if the WorkFlow was successful it also contains the updated WorkContext
     * @author Luke Shannon (Github: lshannon)
     */
    public WorkReport executeWorkFlows(WorkContext workContext, WorkFlow workFlow) {
        log.info("Running the WorkFlow: {} with Context: {}", workFlow.getName(), workContext.toString());

        // Create the entity
        WorkFlowTransactionEntity workFlowTransactionEntity = workTransactionService.createWorkFlowTransactionEntity(
                WorkFlowTransactionDTO.builder()
                        .status(Status.IN_PROGRESS.name())
                        .projectName((String) workContext.get(WorkFlowConstants.PROJECT_NAME))
                        .workFlowId(workFlow.getName())
                        .executedBy(getUserName())
                        .workFlowType((String) workContext.get(WorkFlowConstants.WORKFLOW_TYPE))
                        .createdAt(OffsetDateTime.now())
                        .build());

        workContext.put(WorkFlowConstants.WORKFLOW_EXECUTION_ENTITY_REFERENCES, workFlowTransactionEntity.getId());

        //get the report - might be a Parallel report (meaning the report has a collection of reports within)
        WorkReport report = WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(workFlow, workContext);

        //process each report
        processReport(report, workFlowTransactionEntity);
        return report;
    }

    /*
     * Create the WorkFlowTransactionDTO and puts it in the Context of the Report
     */
    private void processReport(WorkReport report, WorkFlowTransactionEntity workFlowTransactionEntity) {
        // Check if there is a workflow checker
        workFlowTransactionEntity.setWorkFlowCheckerId(obtainWorkFlowCheckerId(report));

        // Check if there were any arguments for the WorkFlowChecker
        workFlowTransactionEntity.setWorkFlowCheckerArguments(obtainWorkFlowCheckerArguments(report));

        // Check if there is a next workflow to run - this value would be set by a WorkFlowChecker
        workFlowTransactionEntity.setNextWorkFlowId(obtainerNextWorkFlowId(report));

        // Check if there are runtime arguments captured for the next workflow by the WorkFlowChecker
        workFlowTransactionEntity.setNextWorkFlowArguments(obtainNextWorkFlowArguments(report));

        // Check workflow status
        workFlowTransactionEntity.setStatus(report.getStatus().toString());

        // Update workflow transaction entity
        WorkFlowTransactionEntity entity = workFlowTransactionRepository.saveAndFlush(workFlowTransactionEntity);

        log.info("Generating transaction entity ID: {}", entity.getId());

    }

    /*
     * Gets the arguments for the NEXT WORKFLOW from the WorkContext
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> obtainNextWorkFlowArguments(WorkReport report) {
        if (report.getWorkContext().get(WorkFlowConstants.NEXT_WORKFLOW_ARGUMENTS) != null) {
            return (Map<String, String>) report.getWorkContext().get(WorkFlowConstants.NEXT_WORKFLOW_ARGUMENTS);
        }
        return new HashMap<>();
    }

    /*
     * Gets the NEXT WORKFLOW ID from the WorkContext
     */
    private String obtainerNextWorkFlowId(WorkReport report) {
        if (report.getWorkContext().get(WorkFlowConstants.NEXT_WORKFLOW_ID) != null) {
            return (String) report.getWorkContext().get(WorkFlowConstants.NEXT_WORKFLOW_ID);
        }
        return null;
    }

    /*
     * Gets the WORKFLOW CHECKER Arguments from the WorkContext
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> obtainWorkFlowCheckerArguments(WorkReport report) {
        if (report.getWorkContext().get(WorkFlowConstants.WORKFLOW_CHECKER_ARGUMENTS) != null) {
            return (Map<String, String>) report.getWorkContext().get(WorkFlowConstants.WORKFLOW_CHECKER_ARGUMENTS);
        }
        return new HashMap<>();
    }

    /*
     * Gets the WORKFLOW CHECKER ID from the WorkContext
     */
    private String obtainWorkFlowCheckerId(WorkReport report) {
        if (report.getWorkContext().get(WorkFlowConstants.WORKFLOW_CHECKER_ID) != null) {
            return (String) report.getWorkContext().get(WorkFlowConstants.WORKFLOW_CHECKER_ID);
        }
        return null;
    }

    private String getUserName() {
        String userName = securityUtils.getUsername();
        if (userName == null) {
            log.info("There is no username. This can happen when running with the 'local' profile. Setting the username to a default");
            userName = "N/A";
        }
        return userName;
    }
}
