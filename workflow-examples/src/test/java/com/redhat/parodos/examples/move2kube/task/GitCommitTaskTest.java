package com.redhat.parodos.examples.move2kube.task;

import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GitCommitTaskTest {

	@Test
	public void testCommitParams() {
		assertThat(new GitCommitTask().getWorkFlowTaskParameters().size()).isEqualTo(1);
	}

	@Test
	public void testGetRepoPath() {
		GitCommitTask task = new GitCommitTask();
		WorkContext ctx = new WorkContext();
		ctx.put("gitDestination", "/invalid");
		assertThat(task.getRepoPath(ctx)).isEqualTo("/invalid");
	}

}