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
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.exceptions.WorkflowExecutionNotFoundException;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionContext;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
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

	private final WorkFlowRepository workFlowRepository;

	private final WorkFlowServiceImpl workFlowService;

	private final WorkFlowSchedulerServiceImpl workFlowSchedulerService;

	private final WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private final WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl;

	public WorkFlowExecutionAspect(WorkFlowServiceImpl workFlowService,
								   WorkFlowSchedulerServiceImpl workFlowSchedulerService,
								   WorkFlowDefinitionRepository workFlowDefinitionRepository, WorkFlowRepository workFlowRepository,
								   WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl) {
		this.workFlowService = workFlowService;
		this.workFlowSchedulerService = workFlowSchedulerService;
		this.workFlowDefinitionRepository = workFlowDefinitionRepository;
		this.workFlowRepository = workFlowRepository;
		this.workFlowContinuationServiceImpl = workFlowContinuationServiceImpl;
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

		String masterWorkflowName = WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION, WorkContextDelegate.Resource.NAME).toString();
		boolean isMaster = workflowName.equals(masterWorkflowName);

		WorkFlowExecution workFlowExecution;
		WorkFlowExecution masterWorkFlowExecution = null;
		String arguments = WorkFlowDTOUtil.writeObjectValueAsString(
				WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, workflowName,
						WorkContextDelegate.Resource.ARGUMENTS));
		UUID projectId = UUID.fromString(WorkContextDelegate
				.read(workContext, WorkContextDelegate.ProcessType.PROJECT, WorkContextDelegate.Resource.ID)
				.toString());

		// get master WorkFlowExecution, this is the first time execution for master
		// workflow if return null
		UUID masterWorkFlowExecutionId = Optional.ofNullable(WorkContextDelegate.read(workContext,
						WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ID))
				.map(id -> UUID.fromString(id.toString())).orElse(null);

		if (masterWorkFlowExecutionId == null) {
			workFlowExecution = handleFirstTimeMainWorkFlowExecution(projectId, workFlowDefinition, arguments,
					workContext);
		}
		else {
			masterWorkFlowExecution = workFlowRepository.findById(masterWorkFlowExecutionId)
					.orElseThrow(() -> new WorkflowExecutionNotFoundException(
							"masterWorkFlow not found for sub-workflow: " + workflowName));

			// get the workflow execution if this is triggered by continuation service
			WorkFlowExecution finalMasterWorkFlowExecution = masterWorkFlowExecution;
			workFlowExecution = isMaster ? masterWorkFlowExecution
					: Optional
					.ofNullable(workFlowRepository.findFirstByWorkFlowDefinitionIdAndMasterWorkFlowExecution(
							workFlowDefinition.getId(), masterWorkFlowExecution))
					.orElseGet(() -> this.workFlowService.saveWorkFlow(projectId, workFlowDefinition.getId(),
							WorkFlowStatus.IN_PROGRESS, finalMasterWorkFlowExecution, arguments));

			if (workFlowExecution.getStatus().equals(WorkFlowStatus.COMPLETED)) {
				// skip the workflow if it is already successful
				if (workFlowDefinition.getType().equals(WorkFlowType.CHECKER)) {
					workFlowSchedulerService.stop(projectId.toString(), (WorkFlow) proceedingJoinPoint.getTarget());
				}
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
			}
		}

		try {
			report = (WorkReport) proceedingJoinPoint.proceed();
			log.info("Workflow {} is {}!", workflowName, report.getStatus().name());
		}
		catch (Throwable e) {
			log.error("Workflow {} has failed! with error: {}", workflowName, e.getMessage());
			report = new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}

		// update workflow execution entity
		workFlowExecution.setStatus(WorkFlowStatus.valueOf(report.getStatus().name()));
		workFlowExecution.setEndDate(new Date());

		WorkReport workReport = postExecution(isMaster, workFlowDefinition, (WorkFlow) proceedingJoinPoint.getTarget(),
				report.getStatus(), workContext, workFlowExecution, masterWorkFlowExecution);

		return workReport == null ? report : workReport;
	}

	private WorkFlowExecution handleFirstTimeMainWorkFlowExecution(UUID projectId,
																   WorkFlowDefinition workFlowDefinition, String arguments, WorkContext workContext) {

		/*
		 * if this is the first time execution for master workflow, persist it and write
		 * its execution id to workContext
		 */
		WorkFlowExecution workFlowExecution = this.workFlowService.saveWorkFlow(projectId, workFlowDefinition.getId(),
				WorkFlowStatus.IN_PROGRESS, null, arguments);
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, workFlowExecution.getId());
		return workFlowExecution;
	}

	private WorkReport postExecution(boolean isMaster, WorkFlowDefinition workFlowDefinition, WorkFlow workFlow,
									 WorkStatus workStatus, WorkContext workContext, WorkFlowExecution workFlowExecution,
									 WorkFlowExecution masterWorkFlowExecution) {
		WorkReport workReport = null;
		switch (workFlowDefinition.getType()) {
			case INFRASTRUCTURE:
			case ASSESSMENT:
				workReport = handlePostWorkflowExecution(isMaster, workFlowExecution, workContext, workFlowDefinition,
						masterWorkFlowExecution);
				break;

			case CHECKER:
				handlePostCheckerExecution(workFlowDefinition, workFlow, workFlowExecution, workStatus, workContext,
						masterWorkFlowExecution);
				break;
			default:
				workFlowService.updateWorkFlow(workFlowExecution);
				break;
		}
		return workReport;
	}

	private WorkReport handlePostWorkflowExecution(boolean isMaster, WorkFlowExecution workFlowExecution,
												   WorkContext workContext, WorkFlowDefinition workFlowDefinition, WorkFlowExecution masterWorkFlowExecution) {
		WorkReport report = null;
		if (isMaster) {
			workFlowExecution.setWorkFlowExecutionContext(Optional
					.ofNullable(workFlowExecution.getWorkFlowExecutionContext()).map(workFlowExecutionContext -> {
						workFlowExecutionContext.setWorkContext(workContext);
						return workFlowExecutionContext;
					}).orElse(WorkFlowExecutionContext.builder().masterWorkFlowExecution(workFlowExecution)
							.workContext(workContext).build()));
		}

		/*
		 * if this is infrastructure/assessment workflow, fail it and persist as 'pending'
		 * if any of its sub work's execution is pending
		 */
		Set<WorkFlowCheckerMappingDefinition> workFlowCheckerMappingDefinitions = workFlowDefinition
				.getWorkFlowTaskDefinitions().stream().map(WorkFlowTaskDefinition::getWorkFlowCheckerMappingDefinition)
				.filter(Objects::nonNull).collect(Collectors.toSet());

		if (workFlowCheckerMappingDefinitions.stream()
				.map(workFlowCheckerDefinition -> workFlowRepository
						.findFirstByWorkFlowDefinitionIdAndMasterWorkFlowExecution(
								workFlowCheckerDefinition.getCheckWorkFlow().getId(), masterWorkFlowExecution))
				.anyMatch(checkerExecution -> checkerExecution == null
						|| !WorkFlowStatus.COMPLETED.equals(checkerExecution.getStatus()))) {
			log.info("fail workflow: {} because it has pending/running checker(s)", workFlowDefinition.getName());
			workFlowExecution.setStatus(WorkFlowStatus.PENDING);
			report = new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}

		workFlowService.updateWorkFlow(workFlowExecution);
		return report;
	}

	public void handlePostCheckerExecution(WorkFlowDefinition workFlowDefinition, WorkFlow workFlow,
										   WorkFlowExecution workFlowExecution, WorkStatus workStatus, WorkContext workContext,
										   WorkFlowExecution masterWorkFlowExecution) {
		/*
		 * if this workflow is a checker, schedule workflow checker for dynamic run on
		 * cron expression or stop if done
		 */
		String projectId = workFlowExecution.getProjectId().toString();
		WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition = workFlowDefinition
				.getCheckerWorkFlowDefinition();

		if (workStatus != WorkStatus.COMPLETED) {
			/*
			 * decide if checker-workflow is rejected by filtering rejected checker-task
			 */
			boolean isRejected = workFlowDefinition
					.getWorkFlowTaskDefinitions().stream().map(
							workFlowTaskDefinition -> WorkContextDelegate
									.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
											workFlowTaskDefinition.getName(), WorkContextDelegate.Resource.STATUS)
									.toString())
					.filter(Objects::nonNull).anyMatch(status -> status.equals(WorkStatus.REJECTED.name()));

			if (!isRejected) {
				workFlowService.updateWorkFlow(workFlowExecution);
				log.info("Schedule workflow checker: {} to run per cron expression: {}", workFlow.getName(),
						workFlowCheckerMappingDefinition.getCronExpression());
				workFlowSchedulerService.schedule(projectId, workFlow, workContext,
						workFlowCheckerMappingDefinition.getCronExpression());
			}
			else {
				log.info("Stop rejected workflow checker: {} schedule", workFlow.getName());
				workFlowSchedulerService.stop(projectId, workFlow);
				workFlowExecution.setStatus(WorkFlowStatus.REJECTED);
				workFlowService.updateWorkFlow(workFlowExecution);
			}
			return;
		}

		log.info("Stop workflow checker: {} schedule", workFlow.getName());
		workFlowSchedulerService.stop(projectId, workFlow);

		String masterWorkFlowName = WorkContextDelegate.read(workContext,
				WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION, WorkContextDelegate.Resource.NAME).toString();

		workFlowService.updateWorkFlow(workFlowExecution);
		/*
		 * if this workflow is checker and it's successful, call continuation service to
		 * restart master workflow execution with same execution Id
		 */
		workFlowContinuationServiceImpl.continueWorkFlow(projectId, masterWorkFlowName, workContext,
				masterWorkFlowExecution.getId());
	}

}