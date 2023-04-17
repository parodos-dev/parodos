package com.redhat.parodos.workflow.execution.aspect;

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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.redhat.parodos.workflow.execution.aspect.WorkFlowExecutionFactory.isMainWorkFlow;

@Slf4j
public class AssessmentInfrastructureWorkFlowPostInterceptor implements WorkFlowPostInterceptor {

	private final WorkFlowDefinition workFlowDefinition;

	private final WorkContext workContext;

	private final WorkFlowExecution workFlowExecution;

	private final WorkFlowExecution mainWorkFlowExecution;

	private final WorkFlowServiceImpl workFlowService;

	private final WorkFlowRepository workFlowRepository;

	public AssessmentInfrastructureWorkFlowPostInterceptor(WorkFlowDefinition workFlowDefinition,
			WorkContext workContext, WorkFlowServiceImpl workFlowService, WorkFlowRepository workFlowRepository,
			WorkFlowExecution workFlowExecution, WorkFlowExecution mainWorkFlowExecution) {
		this.workFlowDefinition = workFlowDefinition;
		this.workContext = workContext;
		this.workFlowService = workFlowService;
		this.workFlowRepository = workFlowRepository;
		this.workFlowExecution = workFlowExecution;
		this.mainWorkFlowExecution = mainWorkFlowExecution;
	}

	public WorkReport handlePostWorkFlowExecution() {
		WorkReport report = null;
		if (isMainWorkFlow(workFlowDefinition, workContext)) {
			workFlowExecution.setWorkFlowExecutionContext(Optional
					.ofNullable(workFlowExecution.getWorkFlowExecutionContext()).map(workFlowExecutionContext -> {
						workFlowExecutionContext.setWorkContext(workContext);
						return workFlowExecutionContext;
					}).orElse(WorkFlowExecutionContext.builder().mainWorkFlowExecution(workFlowExecution)
							.workContext(workContext).build()));
		}

		if (workFlowExecution.getStatus().equals(WorkFlowStatus.FAILED)) {
			workFlowService.updateWorkFlow(workFlowExecution);
			return null;
		}
		/*
		 * if this is infrastructure/assessment workflow, fail it and persist as 'pending'
		 * if any of its sub work's execution is pending
		 */
		Set<WorkFlowCheckerMappingDefinition> workFlowCheckerMappingDefinitions = workFlowDefinition
				.getWorkFlowTaskDefinitions().stream().map(WorkFlowTaskDefinition::getWorkFlowCheckerMappingDefinition)
				.filter(Objects::nonNull).collect(Collectors.toSet());

		List<WorkFlowExecution> checkerExecutions = workFlowCheckerMappingDefinitions.stream().map(
				workFlowCheckerDefinition -> workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(
						workFlowCheckerDefinition.getCheckWorkFlow().getId(), mainWorkFlowExecution))
				.collect(Collectors.toList());

		for (WorkFlowExecution checkerExecution : checkerExecutions)
			if (checkerExecution != null && checkerExecution.getStatus().isRejected()) {
				log.info("fail workflow: {} because it has declined checker(s)", workFlowDefinition.getName());
				workFlowExecution.setStatus(WorkFlowStatus.FAILED);
				report = new DefaultWorkReport(WorkStatus.FAILED, workContext);
				break;
			}
			else if (checkerExecution == null || checkerExecution.getStatus().isFailed()) {
				log.info("workflow: {} has a pending/running checker: {}", workFlowDefinition.getName(),
						checkerExecution == null ? "checker is pending"
								: checkerExecution.getWorkFlowDefinitionId().toString());
				workFlowExecution.setStatus(WorkFlowStatus.IN_PROGRESS);
				report = new DefaultWorkReport(WorkStatus.IN_PROGRESS, workContext);
			}

		workFlowService.updateWorkFlow(workFlowExecution);
		return report;
	}

}
