package com.redhat.parodos.examples.move2kube.task;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class Move2KubeBaseTest {

	@Test
	public void testOutputs() {
		assertThat(new Move2KubeBase().getWorkFlowTaskOutputs(), hasSize(0));
	}

	@Test
	public void testExecute() {
		// given

		Move2KubeBase task = new Move2KubeBase();

		// when
		WorkReport report = task.execute(new WorkContext());
		// then
		assertThat(report, is(nullValue()));
	}

}
