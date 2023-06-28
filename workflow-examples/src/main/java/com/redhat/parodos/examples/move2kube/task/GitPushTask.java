package com.redhat.parodos.examples.move2kube.task;

import java.util.List;
import java.util.stream.Collectors;

import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitPushTask extends com.redhat.parodos.tasks.git.GitPushTask {

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return super.getWorkFlowTaskParameters().stream().filter(param -> !param.getKey().equals("path"))
				.collect(Collectors.toList());
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		WorkReport report = super.execute(workContext);
		return report;
	}

	public String getRepoPath(WorkContext workContext) {
		String repo = workContext.get("gitDestination").toString();
		return repo;
	}

}
