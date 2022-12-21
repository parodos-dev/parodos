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
package com.redhat.parodos.workflow.execution.transaction;

import com.redhat.parodos.workflow.execution.WorkFlowStatus;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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

    @Before(
            "pointcutScope() && args(workContext)"
    )
    public void beforeTask(JoinPoint joinPoint, WorkContext workContext) {
        String taskName = joinPoint.getTarget().getClass().getSimpleName();
        log.info("pointcut on task: " + taskName);

        updateTaskStatus(taskName, workContext, WorkFlowStatus.IN_PROGRESS, true, false);
    }

    @AfterReturning(
            pointcut = "pointcutScope()",
            returning = "report"
    )
    public void afterTaskCompleted(JoinPoint joinPoint, WorkReport report) {
        String taskName = joinPoint.getTarget().getClass().getSimpleName();
        log.info("task {} is successful!", taskName);
        updateTaskStatus(taskName, report.getWorkContext(),
                report.getStatus().equals(WorkStatus.COMPLETED) ? WorkFlowStatus.SUCCESS : WorkFlowStatus.FAILED,
                false, false);
    }

    @AfterThrowing(
            "pointcutScope() && args(workContext)"
    )
    public void afterTaskFailed(JoinPoint joinPoint, WorkContext workContext) {
        String taskName = joinPoint.getTarget().getClass().getSimpleName();
        log.error("task {} is failed!", taskName);
        updateTaskStatus(taskName, workContext, WorkFlowStatus.FAILED, false, true);
    }

    @Synchronized
    private void updateTaskStatus(String taskName, WorkContext workContext, WorkFlowStatus status, boolean isBeforeExecute, boolean isExceptionThrown) {
        WorkFlowTransactionEntity workFlowTransactionEntity = workTransactionService.getWorkFlowTransactionEntity(String.valueOf(workContext.get(WorkFlowConstants.WORKFLOW_EXECUTION_ENTITY_REFERENCES)));

        if (isBeforeExecute)
            // Create the task entity
            workFlowTransactionEntity.getTaskTransactions().add(
                    TaskTransactionEntity.builder()
                            .workFlowTransaction(workFlowTransactionEntity)
                            .taskStatus(WorkFlowStatus.IN_PROGRESS)
                            .taskName(taskName)
                            .createdAt(OffsetDateTime.now())
                            .build()
            );
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
                workFlowTransactionEntity.setStatus(WorkFlowStatus.FAILED.name());
        }
        workTransactionService.updateWorkFlowTransactionEntity(workFlowTransactionEntity);
    }
}
