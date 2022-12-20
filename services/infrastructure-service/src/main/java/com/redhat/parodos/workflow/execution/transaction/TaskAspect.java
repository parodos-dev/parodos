package com.redhat.parodos.workflow.execution.transaction;

import com.redhat.parodos.workflow.execution.WorkFlowStatus;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.work.WorkReport;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

@Aspect
@Slf4j
public class TaskAspect {
    private final TaskTransactionRepository taskTransactionRepository;

    public TaskAspect(TaskTransactionRepository taskTransactionRepository) {
        this.taskTransactionRepository = taskTransactionRepository;
    }

    @Before(
            "execution(* com.redhat.parodos.workflows.WorkFlowTask+.execute(..))"
    )
    public void beforeTask(JoinPoint joinPoint) {
        log.info("Arguments Passed=" + Arrays.toString(joinPoint.getArgs()));

        // Create the entity
        taskTransactionRepository.saveAndFlush(
                TaskTransactionEntity.builder()
                        .workflowId(UUID.randomUUID())
                        .taskStatus(WorkFlowStatus.IN_PROGRESS)
                        .taskName(joinPoint.getTarget().getClass().getName())
                        .createdAt(OffsetDateTime.now())
                        .build());
    }

    @AfterReturning(
            pointcut = "execution(* com.redhat.parodos.workflows.WorkFlowTask+.execute(..))",
            returning = "report"
    )
    public void afterTaskFailed(WorkReport report) {
        log.info("return Passed=" + report.toString());
    }

    @AfterThrowing(
            "execution(* com.redhat.parodos.workflows.WorkFlowTask+.execute(..))"
    )
    public void afterTaskCompleted(JoinPoint joinPoint) {
        log.info("exception Passed=" + joinPoint.toString());
    }

//    @Around(
//            "execution(* com.redhat.parodos.workflows.WorkFlowTask+.execute(..))"
//    )
//    public Object beforeTask(ProceedingJoinPoint proceedingJoinPoint) {
//        log.info("Before invoking execute() method");
//        Object value = null;
//        try {
//            value = proceedingJoinPoint.proceed();
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//        log.info("After invoking execute() method. Return value=" + value);
//        return value;
//    }
}
