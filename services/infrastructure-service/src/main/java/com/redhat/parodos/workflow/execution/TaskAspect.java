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
 * Aspect pointcut for WorkFlow Task execution.
 *
 * @author Richard Wang (Github: RichardW98)
 */
@Aspect
@Component
@Slf4j
public class TaskAspect {
    private final WorkTransactionService workTransactionService;

    public TaskAspect(WorkTransactionService workTransactionService) {
        this.workTransactionService = workTransactionService;
    }

    /**
     * the "execute()" method of all subclasses of WorkFlowTask will be caught in the pointcut
     */
    @Pointcut("execution(* com.redhat.parodos.workflows.WorkFlowTask+.execute(..))")
    public void pointcutScope() {
    }

    @Around(
            "pointcutScope() && args(workContext)"
    )
    public WorkReport executeAroundAdvice(ProceedingJoinPoint proceedingJoinPoint, WorkContext workContext) {
        String taskName = ((WorkFlowTask)proceedingJoinPoint.getTarget()).getName();
        WorkFlowTransactionEntity workFlowTransactionEntity = workTransactionService.getWorkFlowTransactionEntity(String.valueOf(workContext.get(WorkFlowConstants.WORKFLOW_EXECUTION_ENTITY_REFERENCES)));
        log.info("Before invoking execute() on task: {}", taskName);
        Status status = getTaskStatus(workFlowTransactionEntity, taskName);
        if (status == null) {
            workFlowTransactionEntity = updateTaskPersistence(workFlowTransactionEntity, taskName, workContext, Status.IN_PROGRESS, true, false);
        } else if (!Status.IN_PROGRESS.equals(status)) {
            log.info("task status found: {}", taskName);
            return new DefaultWorkReport(status.equals(Status.SUCCESS) ? WorkStatus.COMPLETED : WorkStatus.FAILED, workContext);
        }
        WorkReport report = null;
        try {
            report = (WorkReport) proceedingJoinPoint.proceed();
        } catch (Throwable e) {
            log.error("task {} is failed!", taskName);
            updateTaskPersistence(workFlowTransactionEntity, taskName, workContext, Status.FAILED, false, true);
        }
        log.info("task {} is successful!", taskName);
        updateTaskPersistence(workFlowTransactionEntity, taskName, report.getWorkContext(),
                report.getStatus().equals(WorkStatus.COMPLETED) ? Status.SUCCESS : Status.FAILED,
                false, false);
        return report;

    }

    /**
     * write to db must be synchronized among pointcuts
     *
     * @param workFlowTransactionEntity
     * @param taskName
     * @param workContext
     * @param status
     * @param isBeforeExecute
     * @param isExceptionThrown
     * @return
     */
    @Synchronized
    private WorkFlowTransactionEntity updateTaskPersistence(WorkFlowTransactionEntity workFlowTransactionEntity, String taskName, WorkContext workContext, Status status, boolean isBeforeExecute, boolean isExceptionThrown) {
        if (isBeforeExecute) {
            // Create the task entity
            log.info("adding task: {}", taskName);
            workFlowTransactionEntity.getTaskTransactions().add(
                    TaskTransactionEntity.builder()
                            .workFlowTransactionEntity(workFlowTransactionEntity)
                            .taskStatus(Status.IN_PROGRESS)
                            .taskName(taskName)
                            .createdAt(OffsetDateTime.now())
                            .build()
            );
        }
        else {
            workFlowTransactionEntity
                    .getTaskTransactions()
                    .stream()
                    .filter(taskTransactionEntity -> taskName.equals(taskTransactionEntity.getTaskName()))
                    .forEach(entity -> {
                        entity.setTaskStatus(status);
                        entity.setEndAt(OffsetDateTime.now());
                    });
            if (isExceptionThrown)
                workFlowTransactionEntity.setStatus(Status.FAILED.name());
        }
        return workTransactionService.updateWorkFlowTransactionEntity(workFlowTransactionEntity);
    }

    private Status getTaskStatus(WorkFlowTransactionEntity workFlowTransactionEntity, String taskName) {
        log.info("checking task status: {} in Workflow: {}", taskName, workFlowTransactionEntity.getId());
        return workFlowTransactionEntity.getTaskTransactions().stream()
                .filter(taskTransactionEntity -> taskName.equals(taskTransactionEntity.getTaskName()))
                .findFirst()
                .map(TaskTransactionEntity::getTaskStatus)
                .orElse(null);
    }
}
