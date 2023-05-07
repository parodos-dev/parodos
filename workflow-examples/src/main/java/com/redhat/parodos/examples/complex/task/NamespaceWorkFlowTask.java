package com.redhat.parodos.examples.complex.task;

import java.util.List;

import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NamespaceWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("NamespaceWorkFlowTask");
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	@Override
	public List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(WorkParameter.builder().key("projectId").description("The project id")
				.type(WorkParameterType.NUMBER).optional(false).build());
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.HTTP2XX);
	}

}