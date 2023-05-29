package com.redhat.parodos.examples.move2kube.task;

import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GitArchiveTaskTest {

	@Test
	public void testArchiveParams() {
		assertThat(new GitArchiveTask().getWorkFlowTaskParameters().size()).isEqualTo(0);
	}

	@Test
	public void testGetRepoPath() {
		GitArchiveTask task = new GitArchiveTask();
		WorkContext ctx = new WorkContext();
		ctx.put("gitDestination", "/invalid");
		assertThat(task.getRepoPath(ctx)).isEqualTo("/invalid");
	}

}