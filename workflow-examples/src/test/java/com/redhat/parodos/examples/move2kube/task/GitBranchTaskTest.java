package com.redhat.parodos.examples.move2kube.task;

import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GitBranchTaskTest {

	@Test
	public void testBranchParams() {
		assertThat(new GitBranchTask().getWorkFlowTaskParameters().size()).isEqualTo(1);
	}

	@Test
	public void testGetRepoPath() {
		GitBranchTask task = new GitBranchTask();
		WorkContext ctx = new WorkContext();
		ctx.put("gitDestination", "/invalid");
		assertThat(task.getRepoPath(ctx)).isEqualTo("/invalid");
	}

}