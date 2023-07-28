package com.redhat.parodos.workflow.task.shell;

import java.util.UUID;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShellRunnerTaskTest {

	ShellRunnerTask underTest;

	WorkContext ctx;

	@BeforeEach
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

		assertThat(underTest.execute(ctx).getStatus(), equalTo(WorkStatus.COMPLETED));
		assertThat(output.toString(), equalTo(randomUUID.toString()));
	}

	@Test
	public void failsWhenTimeouts() {
		ctx.put("command", "sleep");
		ctx.put("args", "5s");
		ctx.put("timeoutInSeconds", "1");
		assertThat(underTest.execute(ctx).getStatus(), equalTo(WorkStatus.FAILED));
	}

	@Test
	public void completesIfExitZero() {
		ctx.put("command", "true");
		assertThat(underTest.execute(ctx).getStatus(), equalTo(WorkStatus.COMPLETED));
	}

	@Test
	public void failsIfExitNonZero() {
		ctx.put("command", "false");
		assertThat(underTest.execute(ctx).getStatus(), equalTo(WorkStatus.FAILED));
	}

	@Test()
	public void failsOnMissingCommand() {
		ctx.put("command", "");
		Throwable thrown = assertThrows(IllegalArgumentException.class, () -> {
			underTest.execute(ctx);
		});
		assertThat("argument 'command' is empty or blank", equalTo(thrown.getMessage()));
	}

}