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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskStatus;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import lombok.extern.slf4j.Slf4j;

/**
 * Aspect pointcut to perform state management for a workflow task executions
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Luke Shannon (Github: lshannon)
 * @author Annel Ketcha (Github: anludke)
 */

@Aspect
@Component
@Slf4j
public class WorkFlowTaskExecutionAspect {

	private final WorkFlowRepository workFlowRepository;

	private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private final WorkFlowServiceImpl workFlowService;

	private final WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	public WorkFlowTaskExecutionAspect(WorkFlowRepository workFlowRepository,
			WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository, WorkFlowServiceImpl workFlowService,
			WorkFlowSchedulerServiceImpl workFlowSchedulerService) {
		this.workFlowRepository = workFlowRepository;
		this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
		this.workFlowService = workFlowService;
		this.workFlowSchedulerService = workFlowSchedulerService;
	}

	/**
	 * the "execute()" method of all subclasses of WorkFlowTask are targeted
	 */
	@Pointcut("execution(* com.redhat.parodos.workflow.task.WorkFlowTask+.execute(..))")
	public void pointcutScopeTask() {
	}

	/**
	 * Main entry point. Determines if a WorkFlowTask should be continued to execute, also
	 * persists/updates execution state in the DB
	 * @param proceedingJoinPoint - JoinPoint supplied by framework
	 * @param workContext - @see WorkFlowContext reference being used for the execution
	 * @return WorkReport with the results of the Workflow execution
	 */
	@Around("pointcutScopeTask() && args(workContext)")
	public WorkReport executeAroundAdviceTask(ProceedingJoinPoint proceedingJoinPoint, WorkContext workContext) {
		WorkReport report = null;
		String workFlowTaskName = ((WorkFlowTask) proceedingJoinPoint.getTarget()).getName();
		log.info("Before invoking execute() on workflow task name: {}", workFlowTaskName);
		WorkFlowTaskDefinition workFlowTaskDefinition = workFlowTaskDefinitionRepository
				.findFirstByName(workFlowTaskName);

		// skip the task if it's already successful
		UUID masterWorkFlowExecutionId = UUID.fromString(WorkContextDelegate
				.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ID)
				.toString());

		WorkFlowExecution masterWorkFlowExecution = workFlowRepository.findById(masterWorkFlowExecutionId).get();
		// get the workflow if it's executed again from continuation
		WorkFlowExecution workFlowExecution = handleParentWorkflowUseCase(workContext, workFlowTaskDefinition, masterWorkFlowExecution);
		WorkFlowTaskExecution workFlowTaskExecution = workFlowService.getWorkFlowTask(workFlowExecution.getId(),
				workFlowTaskDefinition.getId());
		if (workFlowTaskExecution == null) {
			workFlowTaskExecution = workFlowService.saveWorkFlowTask(
					// @formatter:off
					WorkFlowDTOUtil.writeObjectValueAsString(WorkContextDelegate.read(
					workContext,
					WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, 
					workFlowTaskName,
					WorkContextDelegate.Resource.ARGUMENTS)),
					workFlowTaskDefinition.getId(), 
					workFlowExecution.getId(), 
					WorkFlowTaskStatus.IN_PROGRESS);
					// @formatter:on
		}
		else if (workFlowTaskExecution.getStatus().equals(WorkFlowTaskStatus.COMPLETED))
			// skip the task if it's already successful
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);

		try {
			report = (WorkReport) proceedingJoinPoint.proceed();
		}
		catch (Throwable e) {
			log.error("Workflow task execution {} has failed!", workFlowTaskName);
		}
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
				workFlowTaskName, WorkContextDelegate.Resource.STATUS, report.getStatus().name());

		workFlowTaskExecution.setStatus(WorkFlowTaskStatus.valueOf(report.getStatus().name()));
		workFlowTaskExecution.setLastUpdateDate(new Date());
		workFlowService.updateWorkFlowTask(workFlowTaskExecution);

		/*
		 * if this task has checker schedule workflow checker for dynamic run on cron
		 * expression or stop if done
		 */
		if (workFlowTaskDefinition.getWorkFlowCheckerMappingDefinition() != null) {
			// if this task has no running checker
			WorkFlowExecution checkerWorkFlowExecution = workFlowRepository
					.findFirstByWorkFlowDefinitionIdAndMasterWorkFlowExecution(
							workFlowTaskDefinition.getWorkFlowCheckerMappingDefinition().getId(),
							masterWorkFlowExecution);
			if (checkerWorkFlowExecution == null) {
				// schedule workflow checker for dynamic run on cron expression
				List<WorkFlow> checkerWorkFlows = ((BaseInfrastructureWorkFlowTask) proceedingJoinPoint.getTarget())
						.getWorkFlowCheckers();
				startCheckerOnSchedule(
						workFlowTaskDefinition.getWorkFlowCheckerMappingDefinition().getCheckWorkFlow().getName(),
						checkerWorkFlows, workFlowTaskDefinition.getWorkFlowCheckerMappingDefinition(), workContext);
			}
		}
		return report;
	}

	private WorkFlowExecution handleParentWorkflowUseCase(WorkContext workContext,
			WorkFlowTaskDefinition workFlowTaskDefinition, WorkFlowExecution masterWorkFlowExecution) {
		WorkFlowExecution workFlowExecution;
		boolean isParentWorkFlowMaster = workFlowTaskDefinition.getWorkFlowDefinition().getName().equalsIgnoreCase(
				WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
						WorkContextDelegate.Resource.NAME).toString());
		if (isParentWorkFlowMaster) {
			workFlowExecution = masterWorkFlowExecution;
		}
		else {
			workFlowExecution = workFlowRepository.findFirstByWorkFlowDefinitionIdAndMasterWorkFlowExecution(
					workFlowTaskDefinition.getWorkFlowDefinition().getId(), masterWorkFlowExecution);
		}
		return workFlowExecution;
	}

	private void startCheckerOnSchedule(String workFlowName, List<WorkFlow> workFlows,
			WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition, WorkContext workContext) {
		log.info("Schedule workflow checker: {} to run per cron expression: {}", workFlowName,
				workFlowCheckerMappingDefinition.getCronExpression());
		for (WorkFlow workFlow : workFlows) {
			workFlowSchedulerService.schedule(workFlow, workContext, workFlowCheckerMappingDefinition.getCronExpression());
		}
		
	}

}
