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

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.exceptions.WorkflowExecutionNotFoundException;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
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
	@Pointcut("execution(* com.redhat.parodos.workflow.task.BaseWorkFlowTask+.execute(..))")
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
		WorkReport report;
		BaseWorkFlowTask workFlowTask = ((BaseWorkFlowTask) proceedingJoinPoint.getTarget());
		String workFlowTaskName = workFlowTask.getName();

		log.info("Before invoking execute() on workflow task name: {}", workFlowTaskName);
		WorkFlowTaskDefinition workFlowTaskDefinition = workFlowTaskDefinitionRepository
				.findFirstByName(workFlowTaskName);

		UUID mainWorkFlowExecutionId = WorkContextUtils.getMainExecutionId(workContext);
		WorkFlowExecution mainWorkFlowExecution = workFlowRepository.findById(mainWorkFlowExecutionId).orElseThrow(
				() -> new WorkflowExecutionNotFoundException("mainWorkFlow not found for task: " + workFlowTaskName));

		// get the workflow execution if it's executed again from continuation
		WorkFlowExecution workFlowExecution = handleParentWorkflowUseCase(workContext, workFlowTaskDefinition,
				mainWorkFlowExecution);
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
                    WorkStatus.IN_PROGRESS);
            // @formatter:on
		}
		else if (WorkStatus.FAILED != workFlowTaskExecution.getStatus()) {
			// fail the task if it's processed by other thread
			// skip the task if it's already successful/rejected
			log.info("skipping task: {} with status {}", workFlowTaskName, workFlowTaskExecution.getStatus().name());
			return new DefaultWorkReport(WorkStatus.valueOf(workFlowTaskExecution.getStatus().name()), workContext);
		}
		try {
			workFlowTask.preExecute(workContext);
			report = (WorkReport) proceedingJoinPoint.proceed();
			if (report == null || report.getStatus() == null) {
				throw new NullPointerException("task execution not returns status: " + workFlowTaskName);
			}
		}
		catch (Throwable e) {
			log.error("Workflow task execution {} has failed! error message: {}", workFlowTaskName, e.getMessage());
			report = new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
				workFlowTaskName, WorkContextDelegate.Resource.STATUS, report.getStatus().name());

		workFlowTaskExecution.setStatus(report.getStatus());
		if (report.getError() != null) {
			workFlowTaskExecution.setMessage(report.getError().getMessage());
		}

		workFlowTaskExecution.setAlertMessage(report.getAlertMessage());
		workFlowTaskExecution.setLastUpdateDate(new Date());
		workFlowService.updateWorkFlowTask(workFlowTaskExecution);

		/*
		 * if this task is successful, and it has checker; then schedule workflow checker
		 * for dynamic run on cron expression
		 */
		if (WorkStatus.COMPLETED.equals(report.getStatus())
				&& workFlowTaskDefinition.getWorkFlowCheckerMappingDefinition() != null) {
			handleChecker(workFlowTask, workContext, workFlowTaskDefinition, mainWorkFlowExecution);
			return new DefaultWorkReport(WorkStatus.PENDING, workContext);
		}
		return report;
	}

	// Check the WorkFlow for Checkers
	private void handleChecker(BaseWorkFlowTask workFlowTask, WorkContext workContext,
			WorkFlowTaskDefinition workFlowTaskDefinition, WorkFlowExecution mainWorkFlowExecution) {

		// if this task has no running checker
		WorkFlowExecution checkerWorkFlowExecution = workFlowRepository
				.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(
						workFlowTaskDefinition.getWorkFlowCheckerMappingDefinition().getId(), mainWorkFlowExecution);
		if (checkerWorkFlowExecution == null) {
			// schedule workflow checker for dynamic run on cron expression
			List<WorkFlow> checkerWorkFlows = workFlowTask.getWorkFlowCheckers();
			startCheckerOnSchedule(mainWorkFlowExecution.getProjectId(), mainWorkFlowExecution.getUser().getId(),
					workFlowTaskDefinition.getWorkFlowCheckerMappingDefinition().getCheckWorkFlow().getName(),
					checkerWorkFlows, workFlowTaskDefinition.getWorkFlowCheckerMappingDefinition(), workContext);
		}
	}

	// Deal with any logic related to Parent WorkFlows
	private WorkFlowExecution handleParentWorkflowUseCase(WorkContext workContext,
			WorkFlowTaskDefinition workFlowTaskDefinition, WorkFlowExecution mainWorkFlowExecution) {
		return workFlowTaskDefinition.getWorkFlowDefinition().getName().equalsIgnoreCase(
				WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
						WorkContextDelegate.Resource.NAME).toString()) ? mainWorkFlowExecution
								: workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(
										workFlowTaskDefinition.getWorkFlowDefinition().getId(), mainWorkFlowExecution);
	}

	// Iterate through the all the Checkers in the workflow and start them based on their
	// schedules
	private void startCheckerOnSchedule(UUID projectId, UUID userId, String workFlowName, List<WorkFlow> workFlows,
			WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition, WorkContext workContext) {
		log.info("Schedule workflow checker: {} to run per cron expression: {}", workFlowName,
				workFlowCheckerMappingDefinition.getCronExpression());
		for (WorkFlow workFlow : workFlows) {
			workFlowSchedulerService.schedule(projectId, userId, workFlow, workContext,
					workFlowCheckerMappingDefinition.getCronExpression());
		}

	}

}
