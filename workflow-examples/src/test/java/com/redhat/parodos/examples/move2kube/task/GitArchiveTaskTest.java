package com.redhat.parodos.examples.move2kube.task;

import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitArchiveTaskTest {

	@Test
	public void testArchiveParams() {
		assertEquals(new GitArchiveTask().getWorkFlowTaskParameters().size(), 0);
	}

	@Test
	public void testGetRepoPath() {
		GitArchiveTask task = new GitArchiveTask();
		WorkContext ctx = new WorkContext();
		ctx.put("gitDestination", "/invalid");
		assertEquals(task.getRepoPath(ctx), "/invalid");
	}

}