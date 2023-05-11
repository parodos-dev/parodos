package com.redhat.parodos.examples.move2kube.task;

import java.util.List;

import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflows.work.WorkContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitArchiveTask extends com.redhat.parodos.tasks.git.GitArchiveTask {

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of();
	}

	public String getRepoPath(WorkContext workContext) {
		// comes from GitClonePrebuiltTask
		return workContext.get("gitDestination").toString();
	}

}
