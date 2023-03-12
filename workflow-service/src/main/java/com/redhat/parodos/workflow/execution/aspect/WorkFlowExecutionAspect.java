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

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionContext;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
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

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

	private final WorkFlowTaskRepository workFlowTaskRepository;

	private final WorkFlowRepository workFlowRepository;

	private final WorkFlowServiceImpl workFlowService;

	private final WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	private final WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private final WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl;

	private final WorkFlowWorkRepository workFlowWorkRepository;

	public WorkFlowExecutionAspect(WorkFlowServiceImpl workFlowService,
			WorkFlowSchedulerServiceImpl workFlowSchedulerService,
			WorkFlowDefinitionRepository workFlowDefinitionRepository, WorkFlowRepository workFlowRepository,
			WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl,
			WorkFlowTaskRepository workFlowTaskRepository, WorkFlowWorkRepository workFlowWorkRepository) {
		this.workFlowService = workFlowService;
		this.workFlowSchedulerService = workFlowSchedulerService;
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowRepository = workFlowRepository;
		this.workFlowContinuationServiceImpl = workFlowContinuationServiceImpl;
		this.workFlowTaskRepository = workFlowTaskRepository;
		this.workFlowWorkRepository = workFlowWorkRepository;
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
		// TODO: name vs description
		String workFlowName = ((WorkFlow) proceedingJoinPoint.getTarget()).getName();
		log.info("Before invoking execute() on workflow: {} with workContext: {}", workFlowName, workContext);

		// get workflow definition entity
		WorkFlowDefinition workFlowDefinition = this.workFlowDefinitionRepository.findFirstByName(workFlowName);

		boolean isMaster = workFlowWorkRepository.findByWorkDefinitionId(workFlowDefinition.getId()).isEmpty()
				&& !workFlowDefinition.getType().equals(WorkFlowType.CHECKER.name());
		// get/set master WorkFlowExecution
		UUID masterWorkFlowExecutionId = Optional.ofNullable(WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ID))
				.map(id -> UUID.fromString(id.toString())).orElse(null);

		WorkFlowExecution workFlowExecution;
		if (masterWorkFlowExecutionId == null) {
			// this is first time execution for master workflow
			// save and write execution id to workcontext
			workFlowExecution = this.workFlowService
					.saveWorkFlow(
							UUID.fromString(
									WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.PROJECT,
											WorkContextDelegate.Resource.ID).toString()),
							workFlowDefinition.getId(), WorkFlowStatus.IN_PROGRESS, null);
			masterWorkFlowExecutionId = workFlowExecution.getId();
			WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
					WorkContextDelegate.Resource.ID, workFlowExecution.getId());
		}
		else {
			WorkFlowExecution masterWorkFlowExecution = workFlowRepository.findById(masterWorkFlowExecutionId).get();

			// get the workflow execution if it's to continue
			if (isMaster)
				workFlowExecution = masterWorkFlowExecution;
			else
				workFlowExecution = workFlowRepository.findFirstByWorkFlowDefinitionIdAndMasterWorkFlowExecution(
						workFlowDefinition.getId(), masterWorkFlowExecution);

			if (workFlowExecution == null) {
				workFlowExecution = this.workFlowService.saveWorkFlow(
						UUID.fromString(WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.PROJECT,
								WorkContextDelegate.Resource.ID).toString()),
						workFlowDefinition.getId(), WorkFlowStatus.IN_PROGRESS, masterWorkFlowExecution);
			}
			else if (workFlowExecution.getStatus().equals(WorkFlowStatus.COMPLETED))
				// skip the workflow if it's already successful
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}

		if (!isMaster)
			WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, workFlowName,
					WorkContextDelegate.Resource.ID, workFlowExecution.getId().toString());
		workFlowDefinition.getWorkFlowTaskDefinitions()
				.forEach(workFlowTaskDefinitionEntity -> WorkContextDelegate.write(workContext,
						WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, workFlowTaskDefinitionEntity.getName(),
						WorkContextDelegate.Resource.ID, workFlowTaskDefinitionEntity.getId().toString()));
		try {
			report = (WorkReport) proceedingJoinPoint.proceed();
		}
		catch (Throwable e) {
			log.error("Workflow {} has error!", workFlowName);
		}
		log.info("Workflow {} is {}!", workFlowName, report.getStatus().name());
		// update workflow execution entity
		workFlowExecution.setStatus(WorkFlowStatus.valueOf(report.getStatus().name()));
		workFlowExecution.setEndDate(new Date());
		workFlowExecution.setArguments(WorkFlowDTOUtil.writeObjectValueAsString(WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ARGUMENTS)));
		if (!WorkFlowType.CHECKER.name().equals(workFlowDefinition.getType().toUpperCase())) {
			// TODO: save workContext to execution if this is master workflow
			WorkFlowExecution masterWorkFlowExecution;
			if (masterWorkFlowExecutionId == null) {
				workFlowExecution.setWorkFlowExecutionContext(WorkFlowExecutionContext.builder()
						.masterWorkFlowExecution(workFlowExecution).workContext(workContext).build());
				masterWorkFlowExecution = workFlowExecution;

			}
			else {
				masterWorkFlowExecution = workFlowRepository.findById(masterWorkFlowExecutionId).get();
			}

			// TODO: if this is infras/assess workflow, fail it and persist as 'pending'
			// if
			// any of its checkers' execution is not successful/not started
			Set<WorkFlowCheckerMappingDefinition> workFlowCheckerMappingDefinitions = workFlowDefinition
					.getWorkFlowTaskDefinitions().stream()
					.map(WorkFlowTaskDefinition::getWorkFlowCheckerMappingDefinition).filter(Objects::nonNull)
					.collect(Collectors.toSet());

			if (workFlowCheckerMappingDefinitions.stream()
					.map(workFlowCheckerDefinition -> workFlowRepository
							.findFirstByWorkFlowDefinitionIdAndMasterWorkFlowExecution(
									workFlowCheckerDefinition.getCheckWorkFlow().getId(), masterWorkFlowExecution))
					.anyMatch(checkerExecution -> checkerExecution == null
							|| !checkerExecution.getStatus().equals(WorkFlowStatus.COMPLETED))) {
				log.info("failed wf: {}", workFlowName);
				workFlowExecution.setStatus(WorkFlowStatus.PENDING);
				workFlowService.updateWorkFlow(workFlowExecution);
				return new DefaultWorkReport(WorkStatus.FAILED, workContext);
			}
			workFlowService.updateWorkFlow(workFlowExecution);

		}
		else {
			// if this workflow is a checker, schedule workflow checker for dynamic run on
			// cron expression or stop if done
			workFlowService.updateWorkFlow(workFlowExecution);
			startOrStopWorkFlowCheckerOnSchedule(workFlowDefinition.getName(),
					(WorkFlow) proceedingJoinPoint.getTarget(), workFlowDefinition.getCheckerWorkFlowDefinition(),
					report.getStatus(), workContext, workFlowExecution.getProjectId().toString(),
					masterWorkFlowExecutionId,
					WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
							WorkContextDelegate.Resource.NAME).toString());
		}
		return report;
	}

	private void startOrStopWorkFlowCheckerOnSchedule(String workFlowName, WorkFlow workFlow,
			WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition, WorkStatus workStatus,
			WorkContext workContext, String projectId, UUID masterWorkFlowExecution, String masterWorkFlowName) {
		if (workStatus != WorkStatus.COMPLETED) {
			log.info("Schedule workflow checker: {} to run per cron expression: {}", workFlowName,
					workFlowCheckerMappingDefinition.getCronExpression());
			workFlowSchedulerService.schedule(workFlow, workContext,
					workFlowCheckerMappingDefinition.getCronExpression());
			return;
		}

		log.info("Stop workflow checker: {} schedule", workFlowName);
		workFlowSchedulerService.stop(workFlow);

		// TODO: if this workflow is checker and it's successful, call continuation
		// service to restart master workflow execution with same execution Id
		workFlowContinuationServiceImpl.continueWorkFlow(projectId, masterWorkFlowName, workContext,
				masterWorkFlowExecution);
	}

}
