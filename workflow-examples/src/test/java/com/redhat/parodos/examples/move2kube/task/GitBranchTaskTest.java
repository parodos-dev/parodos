package com.redhat.parodos.examples.move2kube.task;

import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitBranchTaskTest {

	@Test
	public void testBranchParams() {
		assertEquals(new GitBranchTask().getWorkFlowTaskParameters().size(), 1);
	}

	@Test
	public void testGetRepoPath() {
		GitBranchTask task = new GitBranchTask();
		WorkContext ctx = new WorkContext();
		ctx.put("gitDestination", "/invalid");
		assertEquals(task.getRepoPath(ctx), "/invalid");
	}

}