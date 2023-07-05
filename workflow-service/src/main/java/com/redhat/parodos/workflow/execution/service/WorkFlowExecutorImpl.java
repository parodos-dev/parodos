package com.redhat.parodos.workflow.execution.service;

import com.redhat.parodos.workflow.WorkFlowDelegate;
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
	public void execute(ExecutionContext context) {
		WorkFlow workFlow = workFlowDelegate.getWorkFlowByName(context.workFlowName());
		log.info("Execute workflow {} (ID: {})", context.workFlowName(), context.executionId());
		WorkContextUtils.updateWorkContextPartially(context.workContext(), context.projectId(), context.userId(),
				context.workFlowName(), context.executionId());
		WorkReport report = WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(workFlow, context.workContext());
		log.info("Work report for {} (ID: {}): {}", context.workFlowName(), context.executionId(), report);
		if (isExecutionFailed(context)) {
			log.error("Workflow {} (ID: {}) failed. Check the logs for errors coming from the tasks in this workflow.",
					context.workFlowName(), context.executionId());
			executeFallbackWorkFlowIfNeeded(context);
		}
	}

	private void executeFallbackWorkFlowIfNeeded(ExecutionContext context) {
		if (context.fallbackWorkFlowName() == null) {
			return;
		}

		WorkFlow fallbackWorkFlow = workFlowDelegate.getWorkFlowByName(context.fallbackWorkFlowName());
		if (fallbackWorkFlow == null) {
			log.error("A fallback workflow {} could not be found for failed workflow {} (ID: {})",
					context.fallbackWorkFlowName(), context.workFlowName(), context.executionId());
			return;
		}

		log.info("execute fallback workflow {} for workflow {} (ID: {})", context.fallbackWorkFlowName(),
				context.workFlowName(), context.executionId());
		WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(fallbackWorkFlow, context.workContext());
	}

	private boolean isExecutionFailed(ExecutionContext context) {
		// need to use the status from db to avoid of repetitive execution on fallback
		return workFlowRepository.findById(context.executionId())
				.map(execution -> execution.getStatus() == WorkStatus.FAILED).orElse(false);
	}

}
