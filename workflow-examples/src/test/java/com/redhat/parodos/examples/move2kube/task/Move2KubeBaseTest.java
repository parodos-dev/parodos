package com.redhat.parodos.examples.move2kube.task;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Move2KubeBaseTest {

	@Test
	public void testOutputs() {
		assertThat(new Move2KubeBase().getWorkFlowTaskOutputs().size()).isEqualTo(0);
	}

	public void testExecute() {
		// given

		Move2KubeBase task = new Move2KubeBase();

		// when
		WorkReport report = task.execute(new WorkContext());
		// then
		assertThat(report).isNull();
	}

}