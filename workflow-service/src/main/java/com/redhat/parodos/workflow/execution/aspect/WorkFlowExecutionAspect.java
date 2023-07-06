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

import com.redhat.parodos.common.exceptions.IDType;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.common.exceptions.ResourceType;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
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

	private final WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	private final WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private final WorkFlowExecutionFactory workFlowExecutionFactory;

	public WorkFlowExecutionAspect(WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowDefinitionRepository workFlowDefinitionRepository,
			WorkFlowExecutionFactory workFlowExecutionFactory) {
		this.workFlowSchedulerService = workFlowSchedulerService;
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowExecutionFactory = workFlowExecutionFactory;
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
		WorkReport report;
		String workflowName = ((WorkFlow) proceedingJoinPoint.getTarget()).getName();
		log.info("Before invoking execute() on workflow: {} with workContext: {}", workflowName, workContext);

		/* get workflow definition entity */
		WorkFlowDefinition workFlowDefinition = this.workFlowDefinitionRepository.findFirstByName(workflowName);
		if (workFlowDefinition == null) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new ResourceNotFoundException(ResourceType.WORKFLOW_DEFINITION, IDType.NAME, workflowName));
		}
		WorkFlowExecutionInterceptor executionHandler = workFlowExecutionFactory
				.createExecutionHandler(workFlowDefinition, workContext);
		WorkFlowExecution workFlowExecution = executionHandler.handlePreWorkFlowExecution();

		if (workFlowExecution.getStatus().equals(WorkStatus.COMPLETED)) {
			// skip the workflow if it is already successful
			if (workFlowDefinition.getType().equals(WorkFlowType.CHECKER)) {
				workFlowSchedulerService.stop(workFlowExecution.getProjectId(), workFlowExecution.getUser().getId(),
						(WorkFlow) proceedingJoinPoint.getTarget());
			}
			log.info("skipping workflow: {} is already completed", workFlowDefinition.getName());
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		try {
			report = (WorkReport) proceedingJoinPoint.proceed();
			log.info("Workflow {} is {}!", workflowName, report.getStatus().name());
		}
		catch (Throwable e) {
			e.printStackTrace();
			log.error("Workflow {} has failed! with error: {}", workflowName, e.getMessage());
			report = new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		return executionHandler.handlePostWorkFlowExecution(report, (WorkFlow) proceedingJoinPoint.getTarget());
	}

}
