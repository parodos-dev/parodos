package com.redhat.parodos.examples.master.task;

import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SplunkMonitoringWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("SplunkMonitoringWorkFlowTask");
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	@Override
	public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkFlowTaskParameter.builder().key("clusterName").description("The cluster name")
						.type(WorkFlowTaskParameterType.TEXT).optional(false).build(),
				WorkFlowTaskParameter.builder().key("hostname").description("The hostname")
						.type(WorkFlowTaskParameterType.TEXT).optional(false).build());
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.OTHER);
	}

}