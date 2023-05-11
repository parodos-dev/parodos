package com.redhat.parodos.examples.move2kube.task;

import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitCommitTaskTest {

	@Test
	public void testCommitParams() {
		assertEquals(new GitCommitTask().getWorkFlowTaskParameters().size(), 1);
	}

	@Test
	public void testGetRepoPath() {
		GitCommitTask task = new GitCommitTask();
		WorkContext ctx = new WorkContext();
		ctx.put("gitDestination", "/invalid");
		assertEquals(task.getRepoPath(ctx), "/invalid");
	}

}