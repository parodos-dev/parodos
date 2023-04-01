package com.redhat.parodos.workflow.execution.aspect;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.redhat.parodos.workflow.execution.aspect.WorkFlowExecutionFactory.isMasterWorkFlow;

import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionContext;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssessmentInfrastructureWorkFlowPostInterceptor implements WorkFlowPostInterceptor {

	private final WorkFlowDefinition workFlowDefinition;

	private final WorkContext workContext;

	private final WorkFlowExecution workFlowExecution;

	private final WorkFlowExecution masterWorkFlowExecution;

	private final WorkFlowServiceImpl workFlowService;

	private final WorkFlowRepository workFlowRepository;

	public AssessmentInfrastructureWorkFlowPostInterceptor(WorkFlowDefinition workFlowDefinition,
			WorkContext workContext, WorkFlowServiceImpl workFlowService, WorkFlowRepository workFlowRepository,
			WorkFlowExecution workFlowExecution, WorkFlowExecution masterWorkFlowExecution) {
		this.workFlowDefinition = workFlowDefinition;
		this.workContext = workContext;
		this.workFlowService = workFlowService;
		this.workFlowRepository = workFlowRepository;
		this.workFlowExecution = workFlowExecution;
		this.masterWorkFlowExecution = masterWorkFlowExecution;
	}

	public WorkReport handlePostWorkFlowExecution() {
		WorkReport report = null;
		if (isMasterWorkFlow(workFlowDefinition, workContext)) {
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
			report = new DefaultWorkReport(WorkStatus.IN_PROGRESS, workContext);
		}

		workFlowService.updateWorkFlow(workFlowExecution);
		return report;
	}

}
