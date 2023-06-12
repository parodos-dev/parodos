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
		log.info("execute workflow {} (ID: {})", context.workFlowName(), context.executionId());
		WorkContextUtils.updateWorkContextPartially(context.workContext(), context.projectId(), context.userId(),
				context.workFlowName(), context.executionId());
		WorkReport report = WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(workFlow, context.workContext());
		log.info("work report for  {} (ID: {}): {}", context.workFlowName(), context.executionId(), report);
		if (isExecutionFailed(context)) {
			log.error("workflow {} (ID: {}) failed. Check the logs for errors coming from the tasks in this workflow.",
					context.workFlowName(), context.executionId());
			executeRollbackWorkFlowIfNeeded(context);
		}
	}

	private void executeRollbackWorkFlowIfNeeded(ExecutionContext context) {
		if (context.rollbackWorkFlowName() == null) {
			return;
		}

		WorkFlow rollbackWorkFlow = workFlowDelegate.getWorkFlowByName(context.rollbackWorkFlowName());
		if (rollbackWorkFlow == null) {
			log.error("A rollback workflow {} could not be found for failed workflow {} (ID: {})",
					context.rollbackWorkFlowName(), context.workFlowName(), context.executionId());
			return;
		}

		log.info("execute rollback workflow {} for workflow {} (ID: {})", context.rollbackWorkFlowName(),
				context.workFlowName(), context.executionId());
		WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(rollbackWorkFlow, context.workContext());
	}

	private boolean isExecutionFailed(ExecutionContext context) {
		// need to use the status from db to avoid of repetitive execution on rollback
		return workFlowRepository.findById(context.executionId())
				.map(execution -> execution.getStatus() == WorkStatus.FAILED).orElse(false);
	}

}
