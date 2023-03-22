package com.redhat.parodos.workflow.task.shell;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ShellRunnerTaskTest {

	ShellRunnerTask underTest;

	WorkContext ctx;

	@Before
	public void setUp() {
		underTest = new ShellRunnerTask();
		ctx = new WorkContext();
	}

	@Test
	public void executeWithOutput() {
		UUID randomUUID = UUID.randomUUID();
		ctx.put("command", "echo");
		ctx.put("args", randomUUID);
		var output = new StringBuilder();
		underTest.outputConsumer = output::append;

		assertThat(underTest.execute(ctx).getStatus()).isEqualTo(WorkStatus.COMPLETED);
		assertThat(output.toString()).isEqualTo(randomUUID.toString());
	}

	@Test
	public void failsWhenTimeouts() {
		ctx.put("command", "sleep");
		ctx.put("args", "5s");
		ctx.put("timeoutInSeconds", "1");
		assertThat(underTest.execute(ctx).getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	@Test
	public void completesIfExitZero() {
		ctx.put("command", "true");
		assertThat(underTest.execute(ctx).getStatus()).isEqualTo(WorkStatus.COMPLETED);
	}

	@Test
	public void failsIfExitNonZero() {
		ctx.put("command", "false");
		assertThat(underTest.execute(ctx).getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	@Test()
	public void failsOnMissingCommand() {
		ctx.put("command", "");
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> underTest.execute(ctx));
	}

}