package com.redhat.parodos.tasks.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

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
			this.cmdline = new ArrayList<>();
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
		try {
			workContext.put("command", getRequiredParameterValue("command"));
		}
		catch (Exception e) {
			log.error("error command");
		}
		var params = new ShellRunnerTaskParams(workContext);
		File tmpFile = null;
		try {
			tmpFile = Files
					.createTempFile("parodos-shelltask-runner", "",
							PosixFilePermissions.asFileAttribute(EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE)))
					.toFile();
			FileWriter writer = new FileWriter(tmpFile.getAbsolutePath(), true);
			writer.append((String) workContext.get("command"));
			writer.close();
		}
		catch (Exception e) {
			log.error("file write failed");
		}
		try {
			var pb = new ProcessBuilder(tmpFile.getAbsolutePath());
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
			try (var r = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
				r.lines().forEach(outputConsumer);
			}
			log.info("ended shell task invocation");
			return new DefaultWorkReport(p.exitValue() == 0 ? WorkStatus.COMPLETED : WorkStatus.FAILED, workContext);
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (tmpFile != null) {
				tmpFile.delete();
			}
		}
	}

}
