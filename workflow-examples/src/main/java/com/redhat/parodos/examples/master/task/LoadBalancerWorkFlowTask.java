package com.redhat.parodos.examples.master.task;

import com.redhat.parodos.workflow.task.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class LoadBalancerWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		return null;
	}

	@Override
	public @NonNull List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		return null;
	}

	@Override
	public @NonNull List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return null;
	}

}
