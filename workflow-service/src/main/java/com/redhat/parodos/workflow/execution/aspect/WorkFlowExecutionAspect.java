///*
// * Copyright (c) 2022 Red Hat Developer
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.redhat.parodos.workflow.execution.aspect;
//
//import java.util.Date;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.stereotype.Component;
//import com.redhat.parodos.security.SecurityUtils;
//import com.redhat.parodos.workflow.WorkFlowCheckerDefinition;
//import com.redhat.parodos.workflow.WorkFlowDefinition;
//import com.redhat.parodos.workflow.WorkFlowDelegate;
//import com.redhat.parodos.workflow.WorkFlowStatus;
//import com.redhat.parodos.workflow.context.WorkContextDelegate;
//import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinitionEntity;
//import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
//import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionEntity;
//import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
//import com.redhat.parodos.workflow.execution.service.WorkFlowExecutionServiceImpl;
//import com.redhat.parodos.workflows.work.WorkContext;
//import com.redhat.parodos.workflows.work.WorkReport;
//import com.redhat.parodos.workflows.work.WorkStatus;
//import com.redhat.parodos.workflows.workflow.WorkFlow;
//
//import lombok.extern.slf4j.Slf4j;
//
///**
// * Aspect pointcut to perform state management for WorkFlow executions
// *
// * @author Richard Wang (Github: RichardW98)
// * @author Luke Shannon (Github: lshannon)
// * @author Annel Ketcha (Github: anludke)
// */
//
//@Aspect
//@Component
//@Slf4j
//public class WorkFlowExecutionAspect {
//    private final WorkFlowDefinitionServiceImpl workFlowDefinitionService;
//    private final WorkFlowExecutionServiceImpl workFlowExecutionService;
//    private final WorkFlowSchedulerServiceImpl workFlowSchedulerService;
//    private final WorkFlowDelegate workFlowDelegate;
//
//    private final SecurityUtils securityUtils;
//
//    public WorkFlowExecutionAspect(WorkFlowDefinitionServiceImpl workFlowDefinitionService, WorkFlowExecutionServiceImpl workFlowExecutionService, WorkFlowSchedulerServiceImpl workFlowSchedulerService, WorkFlowDelegate workFlowDelegate, SecurityUtils securityUtils) {
//        this.workFlowDefinitionService = workFlowDefinitionService;
//        this.workFlowExecutionService = workFlowExecutionService;
//        this.workFlowSchedulerService = workFlowSchedulerService;
//        this.workFlowDelegate = workFlowDelegate;
//        this.securityUtils = securityUtils;
//    }
//
//    /**
//     * the "execute()" method of all subclasses of WorkFlowTask are targeted
//     */
//    @Pointcut("execution(* com.redhat.parodos.workflows.workflow.WorkFlow+.execute(..))")
//    public void pointcutScope() {
//    }
//
//    /**
//     * Main entry point. Determines if a WorkFlowTask should be continued to execute, also persists/updates execution state in the DB
//     *
//     * @param proceedingJoinPoint - JoinPoint supplied by framework
//     * @param workContext         - @see WorkFlowContext reference being used for the execution
//     * @return WorkReport with the results of the Workflow execution
//     */
//    @Around(
//            "pointcutScope() && args(workContext)"
//    )
//    public WorkReport executeAroundAdvice(ProceedingJoinPoint proceedingJoinPoint, WorkContext workContext) {
//        WorkReport report = null;
//        String workFlowName = ((WorkFlow) proceedingJoinPoint.getTarget()).getName();
//        log.info("Before invoking execute() on workflow: {} with workContext: {}", workFlowName, workContext);
//        // get workflow definition entity
//        WorkFlowDefinitionEntity workFlowDefinitionEntity = this.workFlowDefinitionService.getWorkFlowDefinitionByName(workFlowName);
//        // get workflow definition
//        WorkFlowDefinition workFlowDefinition = workFlowDelegate.getWorkFlowDefinitionById(workFlowDefinitionEntity.getId());
//        // save work execution entity
//        WorkFlowExecutionEntity workFlowExecutionEntity = this.workFlowExecutionService.saveWorkFlow(securityUtils.getUsername(),
//                "Reason",
//                workFlowDefinitionEntity.getId(),
//                WorkFlowStatus.IN_PROGRESS);
//        // update work context
//        WorkContextDelegate.write(workContext,
//        		WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
//        		WorkContextDelegate.Resource.ID,
//                workFlowDefinitionEntity.getId().toString());
//        WorkContextDelegate.write(workContext,
//        		WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
//        		WorkContextDelegate.Resource.ID,
//                workFlowExecutionEntity.getId().toString());
//        workFlowDefinitionEntity.getTasks().forEach(workFlowTaskDefinitionEntity -> WorkContextDelegate.write(workContext,
//        		WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
//                workFlowTaskDefinitionEntity.getName(),
//                WorkContextDelegate.Resource.ID,
//                workFlowTaskDefinitionEntity.getId().toString()));
//        try {
//            report = (WorkReport) proceedingJoinPoint.proceed();
//        } catch (Throwable e) {
//            log.error("Workflow {} has failed!", workFlowName);
//        }
//        log.info("Workflow {} is successful!", workFlowName);
//        // update workflow execution entity
//        workFlowExecutionEntity.setStatus(WorkFlowStatus.valueOf(report.getStatus().name()));
//        workFlowExecutionEntity.setEndDate(new Date());
//        workFlowExecutionService.updateWorkFlow(workFlowExecutionEntity);
//        // schedule workflow checker for dynamic run on cron expression or stop if done
//        if (workFlowDefinition instanceof WorkFlowCheckerDefinition) {
//            startOrStopWorkFlowCheckerOnSchedule((WorkFlow)proceedingJoinPoint.getTarget(),
//                    (WorkFlowCheckerDefinition)workFlowDefinition,
//                    report.getStatus(),
//                    workContext);
//        }
//        return report;
//    }
//
//    private void startOrStopWorkFlowCheckerOnSchedule(WorkFlow workFlow, WorkFlowCheckerDefinition workFlowCheckerDefinition, WorkStatus workStatus, WorkContext workContext) {
//        if (workStatus != WorkStatus.COMPLETED) {
//            log.info("Schedule workflow checker: {} to run per cron expression: {}", workFlow.getName(), workFlowCheckerDefinition.getCronExpression());
//            workFlowSchedulerService.schedule(workFlow, workContext, workFlowCheckerDefinition.getCronExpression());
//        }
//        else {
//            log.info("Stop workflow checker: {} schedule", workFlow.getName());
//            workFlowSchedulerService.stop(workFlow);
//        }
//    }
//}
