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
package com.redhat.parodos.workflow.execution;

import com.redhat.parodos.workflow.execution.transaction.TaskTransactionEntity;
import com.redhat.parodos.workflow.execution.transaction.WorkFlowTransactionEntity;
import com.redhat.parodos.workflow.execution.transaction.WorkTransactionService;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.WorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Aspect pointcut to perform state management for WorkFlowTask executions
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Luke Shannon (Github: lshannon)
 */
@Aspect
@Component
@Slf4j
public class WorkFlowTaskExecutionAspect {
	
    private final WorkTransactionService workTransactionService;

    public WorkFlowTaskExecutionAspect(WorkTransactionService workTransactionService) {
        this.workTransactionService = workTransactionService;
    }

    /**
     * the "execute()" method of all subclasses of WorkFlowTask are targeted
     */
    @Pointcut("execution(* com.redhat.parodos.workflows.WorkFlowTask+.execute(..))")
    public void pointcutScope() {
    }

    /**
     * Main entry point. Determines if a WorkFlowTask should be continued to execute, also persists/updates execution state in the DB
     *  
     * @param proceedingJoinPoint - JoinPoint supplied by framework
     * @param workContext - @see WorkFlowContext reference being used for the execution
     * @return WorkReport with the results of the Workflow execution
     */
    @Around(
            "pointcutScope() && args(workContext)"
    )
    public WorkReport executeAroundAdvice(ProceedingJoinPoint proceedingJoinPoint, WorkContext workContext) {
    	//A WorkFlowTransactionEntity might have multiple WorkFlowTasks of the same type - need to get the name of the specific one that needs to be processed
        String targetedTaskName = ((WorkFlowTask)proceedingJoinPoint.getTarget()).getName();
        WorkFlowTransactionEntity workFlowTransactionEntity = workTransactionService.getWorkFlowTransactionEntity(String.valueOf(workContext.get(WorkFlowConstants.WORKFLOW_EXECUTION_ENTITY_REFERENCES)));
        log.info("Before invoking execute() on task: {}", targetedTaskName);
        Status status = getTaskStatus(workFlowTransactionEntity, targetedTaskName);
        if (status == null) {
            workFlowTransactionEntity = updateTaskPersistence(workFlowTransactionEntity, targetedTaskName, workContext, Status.IN_PROGRESS, true, false);
        } else if (!Status.IN_PROGRESS.equals(status)) {
            log.info("Task status found: {}", targetedTaskName);
            return new DefaultWorkReport(status.equals(Status.SUCCESS) ? WorkStatus.COMPLETED : WorkStatus.FAILED, workContext);
        }
        WorkReport report = null;
        try {
            report = (WorkReport) proceedingJoinPoint.proceed();
        } catch (Throwable e) {
            log.error("Task {} is failed!", targetedTaskName);
            updateTaskPersistence(workFlowTransactionEntity, targetedTaskName, workContext, Status.FAILED, false, true);
        }
        log.info("Task {} is successful!", targetedTaskName);
        updateTaskPersistence(workFlowTransactionEntity, targetedTaskName, report.getWorkContext(),
                report.getStatus().equals(WorkStatus.COMPLETED) ? Status.SUCCESS : Status.FAILED,
                false, false);
        return report;

    }

    /**
     * Updates the state of the WorkFlowTask execution in the DB. This method is thread safe.
     *
     * @param workFlowTransactionEntity entity obtained from the DB containing all required state to execute a WorkFlowTask
     * @param workFlowTaskName - the specific task that is targeted for execution
     * @param workContext - context of the current execution
     * @param status - status of the execution (only applies when persisting for WorkFlowTasks that have executed)
     * @param isBeforeExecute - persistence is done for a WorkFlowTask before it executed in the event that the application crashes during the execution (this task will be executed again on start up)
     * @param isExceptionThrown - indicating if there was an execution during execution
     * @return WorkFlowTransactionEntity entity generated based on the arguments
     */
    @Synchronized
    private WorkFlowTransactionEntity updateTaskPersistence(WorkFlowTransactionEntity workFlowTransactionEntity, String workFlowTaskName, WorkContext workContext, Status status, boolean isBeforeExecute, boolean isExceptionThrown) {
        if (isBeforeExecute) {
            // Create the task entity
            log.info("adding task: {}", workFlowTaskName);
            workFlowTransactionEntity.getTaskTransactions().add(
                    TaskTransactionEntity.builder()
                            .workFlowTransactionEntity(workFlowTransactionEntity)
                            .taskStatus(Status.IN_PROGRESS)
                            .taskName(workFlowTaskName)
                            .createdAt(OffsetDateTime.now())
                            .build()
            );
        }
        else {
            workFlowTransactionEntity
                    .getTaskTransactions()
                    .stream()
                    .filter(taskTransactionEntity -> workFlowTaskName.equals(taskTransactionEntity.getTaskName()))
                    .forEach(entity -> {
                        entity.setTaskStatus(status);
                        entity.setEndAt(OffsetDateTime.now());
                    });
            if (isExceptionThrown)
                workFlowTransactionEntity.setStatus(Status.FAILED.name());
        }
        return workTransactionService.updateWorkFlowTransactionEntity(workFlowTransactionEntity);
    }

    /*
     * Gets the status of a specific WorkFlowTask in a transaction entity
     */
    private Status getTaskStatus(WorkFlowTransactionEntity workFlowTransactionEntity, String taskName) {
        log.info("checking task status: {} in Workflow: {}", taskName, workFlowTransactionEntity.getId());
        return workFlowTransactionEntity.getTaskTransactions().stream()
                .filter(taskTransactionEntity -> taskName.equals(taskTransactionEntity.getTaskName()))
                .findFirst()
                .map(TaskTransactionEntity::getTaskStatus)
                .orElse(null);
    }
}
