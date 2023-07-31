package com.redhat.parodos.examples.move2kube.task;

import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GitArchiveTaskTest {

	@Test
	public void testArchiveParams() {
		assertThat(new GitArchiveTask().getWorkFlowTaskParameters().size(), equalTo(0));
	}

	@Test
	public void testGetRepoPath() {
		GitArchiveTask task = new GitArchiveTask();
		WorkContext ctx = new WorkContext();
		ctx.put("gitDestination", "/invalid");
		assertThat(task.getRepoPath(ctx), equalTo("/invalid"));
	}

}
