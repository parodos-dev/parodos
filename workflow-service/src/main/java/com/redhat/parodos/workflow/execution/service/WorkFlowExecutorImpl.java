package com.redhat.parodos.workflow.execution.service;

import java.util.Optional;

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.engine.WorkFlowEngineBuilder;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkFlowExecutorImpl implements WorkFlowExecutor {

	private final WorkFlowDelegate workFlowDelegate;

	private final WorkFlowRepository workFlowRepository;

	public WorkFlowExecutorImpl(WorkFlowDelegate workFlowDelegate, WorkFlowRepository workFlowRepository) {
		this.workFlowDelegate = workFlowDelegate;
		this.workFlowRepository = workFlowRepository;
	}

	@Override
	public void execute(ExecutionContext context, WorkFlowService workFlowService) {
		WorkFlow workFlow = workFlowDelegate.getWorkFlowByName(context.workFlowName());
		log.info("Execute workflow {} (ID: {})", context.workFlowName(), context.executionId());
		WorkContextUtils.updateWorkContextPartially(context.workContext(), context.projectId(), context.userId(),
				context.workFlowName(), context.executionId());
		WorkReport report = WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(workFlow, context.workContext());
		log.info("Work report for {} (ID: {}): {}", context.workFlowName(), context.executionId(), report);
		if (isFallbackExecutionNeeded(context)) {
			log.error("Workflow {} (ID: {}) failed. Check the logs for errors coming from the tasks in this workflow.",
					context.workFlowName(), context.executionId());
			executeFallbackWorkFlowIfNeeded(context, workFlowService);
		}
	}

	private void executeFallbackWorkFlowIfNeeded(ExecutionContext context, WorkFlowService workFlowService) {
		if (context.fallbackWorkFlowName() == null) {
			return;
		}

		log.info("execute fallback workflow {} for workflow {} (ID: {})", context.fallbackWorkFlowName(),
				context.workFlowName(), context.executionId());
		workFlowService.executeFallbackWorkFlow(context.fallbackWorkFlowName(), context.executionId());
	}

	private boolean isFallbackExecutionNeeded(ExecutionContext context) {
		// need to use the status from db to avoid of repetitive execution on fallback
		Optional<WorkFlowExecution> workflow = workFlowRepository.findById(context.executionId());
		if (workflow.isEmpty()) {
			return false;
		}
		Optional<WorkFlowExecution> fallbackWorkflow = workFlowRepository
				.findFallbackWorkFlowExecution(context.executionId());
		return workflow.get().getStatus() == WorkStatus.FAILED && fallbackWorkflow.isEmpty();
	}

}
