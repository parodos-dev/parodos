package com.redhat.parodos.examples.move2kube.task;

import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class GitBranchTaskTest {

	@Test
	public void testBranchParams() {
		assertThat(new GitBranchTask().getWorkFlowTaskParameters(), hasSize(1));
	}

	@Test
	public void testGetRepoPath() {
		GitBranchTask task = new GitBranchTask();
		WorkContext ctx = new WorkContext();
		ctx.put("gitDestination", "/invalid");
		assertThat(task.getRepoPath(ctx), equalTo("/invalid"));
	}

}
