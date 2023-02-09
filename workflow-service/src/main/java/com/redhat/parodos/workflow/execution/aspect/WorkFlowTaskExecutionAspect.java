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
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflow.task.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import java.util.Date;
import java.util.UUID;
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

	private final WorkFlowServiceImpl workFlowExecutionService;

	public WorkFlowTaskExecutionAspect(WorkFlowServiceImpl workFlowExecutionService) {
		this.workFlowExecutionService = workFlowExecutionService;
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
	public WorkReport executeAroundAdviceTask(ProceedingJoinPoint proceedingJoinPoint, WorkContext workContext)
			throws NoSuchMethodException {
		WorkReport report = null;
		String workFlowTaskName = ((WorkFlowTask) proceedingJoinPoint.getTarget()).getName();
		log.info("Before invoking execute() on workflow task name: {}, work context is: {}", workFlowTaskName,
				workContext);
		try {
			report = (WorkReport) proceedingJoinPoint.proceed();
		}
		catch (Throwable e) {
			log.error("Workflow task execution {} has failed!", workFlowTaskName);
		}
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
				workFlowTaskName, WorkContextDelegate.Resource.STATUS, report.getStatus().name());

		WorkFlowTaskExecution workFlowTaskExecution = workFlowExecutionService.getWorkFlowTask(
				UUID.fromString(
						WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
								WorkContextDelegate.Resource.ID).toString()),
				UUID.fromString(
						WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_DEFINITION,
								workFlowTaskName, WorkContextDelegate.Resource.ID).toString()));

		if (workFlowTaskExecution == null) {
			workFlowExecutionService.saveWorkFlowTask(
					WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
							workFlowTaskName, WorkContextDelegate.Resource.ARGUMENTS).toString(),
					UUID.fromString(WorkContextDelegate
							.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_DEFINITION,
									workFlowTaskName, WorkContextDelegate.Resource.ID)
							.toString()),
					UUID.fromString(
							WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
									WorkContextDelegate.Resource.ID).toString()),
					WorkFlowTaskStatus.valueOf(report.getStatus().name()));
		}
		else {
			workFlowTaskExecution.setStatus(WorkFlowTaskStatus.valueOf(report.getStatus().name()));
			workFlowTaskExecution.setLastUpdateDate(new Date());
			workFlowExecutionService.updateWorkFlowTask(workFlowTaskExecution);
		}
		return report;
	}

}
