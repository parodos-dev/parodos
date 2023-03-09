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
package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.WorkFlowStatus;
import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * Aspect pointcut to perform state management for a workflow execution
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 */

@Aspect
@Component
@Slf4j
public class WorkFlowExecutionAspect {

	private final WorkFlowServiceImpl workFlowService;

	private final WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	private final WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private final WorkFlowDelegate workFlowDelegate;

	public WorkFlowExecutionAspect(WorkFlowServiceImpl workFlowService,
			WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowDefinitionRepository workFlowDefinitionRepository, WorkFlowDelegate workFlowDelegate) {
		this.workFlowService = workFlowService;
		this.workFlowSchedulerService = workFlowSchedulerService;
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowDelegate = workFlowDelegate;
	}

	/**
	 * the "execute()" method of all subclasses of WorkFlowTask are targeted
	 */
	@Pointcut("execution(* com.redhat.parodos.workflows.workflow.WorkFlow+.execute(..))")
	public void pointcutScope() {
	}

	/**
	 * Main entry point. Determines if a WorkFlowTask should be continued to execute, also
	 * persists/updates execution state in the DB
	 * @param proceedingJoinPoint - JoinPoint supplied by framework
	 * @param workContext - @see WorkFlowContext reference being used for the execution
	 * @return WorkReport with the results of the Workflow execution
	 */
	@Around("pointcutScope() && args(workContext)")
	public WorkReport executeAroundAdvice(ProceedingJoinPoint proceedingJoinPoint, WorkContext workContext) {
		WorkReport report = null;
		// String workFlowName = WorkContextDelegate.read(workContext,
		// WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
		// WorkContextDelegate.Resource.NAME).toString();
		String workFlowName = ((WorkFlow) proceedingJoinPoint.getTarget()).getName();
		log.info("Before invoking execute() on workflow: {} with workContext: {}", workFlowName, workContext);
		// get workflow definition entity
		WorkFlowDefinition workFlowDefinition = this.workFlowDefinitionRepository.findByName(workFlowName).stream()
				.findFirst().get();

		// save work execution entity
		WorkFlowExecution workFlowExecution = this.workFlowService
				.saveWorkFlow(
						UUID.fromString(WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.PROJECT,
								WorkContextDelegate.Resource.ID).toString()),
						workFlowDefinition.getId(), WorkFlowStatus.IN_PROGRESS);

		// update work context
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
				WorkContextDelegate.Resource.ID, workFlowDefinition.getId().toString());
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, workFlowExecution.getId().toString());
		workFlowDefinition.getWorkFlowTaskDefinitions()
				.forEach(workFlowTaskDefinitionEntity -> WorkContextDelegate.write(workContext,
						WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, workFlowTaskDefinitionEntity.getName(),
						WorkContextDelegate.Resource.ID, workFlowTaskDefinitionEntity.getId().toString()));
		try {
			report = (WorkReport) proceedingJoinPoint.proceed();
		}
		catch (Throwable e) {
			log.error("Workflow {} has failed!", workFlowName);
		}
		log.info("Workflow {} is {}!", workFlowName, report.getStatus().name());
		// update workflow execution entity
		workFlowExecution.setStatus(WorkFlowStatus.valueOf(report.getStatus().name()));
		workFlowExecution.setEndDate(new Date());
		workFlowService.updateWorkFlow(workFlowExecution);
		// schedule workflow checker for dynamic run on cron expression or stop if done
		if (WorkFlowType.CHECKER.name().toUpperCase().equals(workFlowDefinition.getType())) {
			startOrStopWorkFlowCheckerOnSchedule(workFlowDefinition.getName(),
					(WorkFlow) proceedingJoinPoint.getTarget(), workFlowDefinition.getCheckerWorkFlowDefinition(),
					report.getStatus(), workContext);
		}
		return report;
	}

	private void startOrStopWorkFlowCheckerOnSchedule(String workFlowName, WorkFlow workFlow,
			WorkFlowCheckerDefinition workFlowCheckerDefinition, WorkStatus workStatus, WorkContext workContext) {
		if (workStatus != WorkStatus.COMPLETED) {
			log.info("Schedule workflow checker: {} to run per cron expression: {}", workFlowName,
					workFlowCheckerDefinition.getCronExpression());
			workFlowSchedulerService.schedule(workFlow, workContext, workFlowCheckerDefinition.getCronExpression());
			return;
		}

		log.info("Stop workflow checker: {} schedule", workFlowName);
		workFlowSchedulerService.stop(workFlow);
		workFlowDelegate.getWorkFlowExecutionByName(workFlowCheckerDefinition.getNextWorkFlow().getName())
				.execute(workContext);
	}

}
