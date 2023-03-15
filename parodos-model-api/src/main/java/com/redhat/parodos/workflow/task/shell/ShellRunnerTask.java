package com.redhat.parodos.workflow.task.shell;

import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.file.attribute.PosixFilePermission.*;

/**
 * ShellRunnerTask takes a script and an optional env and runs them to completion and
 * returns the exit code. For exit code 0 the return status should be successful and for
 * anything else it should be a failure.
 */
@Slf4j
public class ShellRunnerTask extends BaseWorkFlowTask {

	public static final int DEFAULT_EXECUTION_TIMEOUT_IN_SECONDS = 600;

	Consumer<String> outputConsumer = log::info;

	private static class ShellRunnerTaskParams {

		/**
		 * cmdline to execute including arguments, must contain at least one string
		 */
		ArrayList<String> cmdline;

		int timeoutInSeconds = DEFAULT_EXECUTION_TIMEOUT_IN_SECONDS;

		ShellRunnerTaskParams(WorkContext workContext) {
			String command = (String) workContext.get("command");
			if (command == null || command.isBlank()) {
				throw new IllegalArgumentException("argument 'command' is empty or blank");
			}
			this.cmdline = new ArrayList<>() {
			};
			this.cmdline.add(command);
			if (workContext.get("args") != null) {
				this.cmdline.addAll(List.of(workContext.get("args").toString().split(" ")));
			}
			if (workContext.get("timeoutInSeconds") != null) {
				this.timeoutInSeconds = Integer.parseInt(workContext.get("timeoutInSeconds").toString());
			}
		}

	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		var params = new ShellRunnerTaskParams(workContext);
		var pb = new ProcessBuilder(params.cmdline);
		File tmpDir = null;
		try {
			tmpDir = Files
					.createTempDirectory("parodos-shelltask-runner",
							PosixFilePermissions.asFileAttribute(EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE)))
					.toFile();
			pb.directory(tmpDir);
			pb.redirectErrorStream(true);

			log.info("begin shell task invocation");
			Process p = pb.start();
			var elapsed = !p.waitFor(params.timeoutInSeconds, TimeUnit.SECONDS);
			if (elapsed) {
				// timeouts, assume incomplete because we can't read the exit code
				return new DefaultWorkReport(WorkStatus.FAILED, workContext);
			}
			// read output only if we finished waiting otherwise the thread
			// waits till the end of execution.
			try (var r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				r.lines().forEach(outputConsumer);
			}
			log.info("ended shell task invocation");
			return new DefaultWorkReport(p.exitValue() == 0 ? WorkStatus.COMPLETED : WorkStatus.FAILED, workContext);
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (tmpDir != null) {
				tmpDir.delete();
			}
		}
	}

}
