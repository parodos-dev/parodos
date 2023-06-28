package com.redhat.parodos.tasks.git;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class GitCloneTaskTest {

	private GitCloneTask gitCloneTask;

	private Path gitRepoPath;

	private Repository repository;

	private Path tempDir;

	@BeforeEach
	public void setUp() throws Exception {
		gitCloneTask = new GitCloneTask();
		gitCloneTask.setBeanName("GitCloneTask");
		tempDir = Files.createTempDirectory("git-repo");

		gitRepoPath = tempDir.resolve(".git");

		InitCommand command = new InitCommand();
		command.setInitialBranch("main");
		command.setDirectory(tempDir.toFile());
		Git git = command.call();
		assertNotNull(git);
		assertEquals(git.getRepository().getFullBranch(), "refs/heads/main");
		assertEquals(git.getRepository().getBranch(), "main");
		repository = git.getRepository();

		log.info("Created a new repository at '{}'", this.repository.getDirectory());
		this.createSingleFileInRepo();
	}

	@AfterEach
	public void tearDown() throws Exception {
		assertDoesNotThrow(() -> {
			repository.close();
		});
		try (Stream<Path> walk = Files.walk(tempDir)) {
			walk.sorted(java.util.Comparator.reverseOrder()).map(Path::toFile).forEach(p -> {
				assertDoesNotThrow(() -> Files.delete(p.toPath()));
			});
		}
	}

	@Test
	public void testWithValidClone() {
		// given
		WorkContext workContext = getSampleContext();
		WorkContextUtils.addParameter(workContext, "uri", tempDir.toString());
		WorkContextUtils.addParameter(workContext, "branch", "main");

		// then
		gitCloneTask.preExecute(workContext);
		var result = gitCloneTask.execute(workContext);

		// when
		assertThat(result.getError()).isNull();
		assertThat(result.getStatus()).isEqualTo(WorkStatus.COMPLETED);
		assertThat(result.getWorkContext().get("gitDestination")).isNotNull();
		assertThat(result.getWorkContext().get("gitUri")).isNotNull();
	}

	@Test
	public void testWithInValidClone() {
		// given
		WorkContext workContext = getSampleContext();
		WorkContextUtils.addParameter(workContext, "uri", "invalidFolder");
		WorkContextUtils.addParameter(workContext, "branch", "main");

		// then
		gitCloneTask.preExecute(workContext);
		var result = gitCloneTask.execute(workContext);

		// when
		assertThat(result.getError()).isNotNull();
		assertThat(result.getError().toString()).contains("Remote repository invalidFolder is not available");
		assertThat(result.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(result.getWorkContext().get("gitDestination")).isNull();
		assertThat(result.getWorkContext().get("gitUri")).isNull();
	}

	@Test
	public void testWithInValidBranch() {
		// given
		WorkContext workContext = getSampleContext();
		WorkContextUtils.addParameter(workContext, "uri", tempDir.toString());
		WorkContextUtils.addParameter(workContext, "branch", "fooBranch");

		// then
		gitCloneTask.preExecute(workContext);
		var result = gitCloneTask.execute(workContext);

		// when
		assertThat(result.getError()).isNotNull();
		assertThat(result.getError().toString()).contains("cannot connect to the repository server");
		assertThat(result.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(result.getWorkContext().get("gitDestination")).isNull();
		assertThat(result.getWorkContext().get("gitUri")).isNull();
	}

	private void createSingleFileInRepo() {
		Path filePath = tempDir.resolve("file.txt");
		assertDoesNotThrow(() -> {
			Files.write(filePath, "Hello, world!".getBytes());
			Git git = Git.init().setDirectory(tempDir.toFile()).call();
			git.add().addFilepattern(".").call();
			git.commit().setMessage("Initial commit").setSign(false).call();
		});
	}

	private WorkContext getSampleContext() {
		WorkContext context = new WorkContext();
		WorkContextUtils.setMainExecutionId(context, UUID.randomUUID());
		return context;
	}

}
