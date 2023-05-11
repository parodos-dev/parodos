package com.redhat.parodos.tasks.git;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Slf4j
class GitBranchTaskTest {

	private static String defaultBranch = "main";

	private GitBranchTask gitBranchTask;

	private Path gitRepoPath;

	private Repository repository;

	private Path tempDir;

	@BeforeEach
	public void setUp() throws Exception {
		gitBranchTask = new GitBranchTask();
		gitBranchTask.setBeanName("GitBranchTask");
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
	public void testBranchCheckout() {
		// given
		createSingleFileInRepo();
		WorkContext workContext = new WorkContext();
		workContext.put("path", tempDir.toString());
		WorkContextUtils.addParameter(workContext, "branch", "newBranch");
		WorkContextUtils.addParameter(workContext, "path", tempDir.toString());

		// when
		var result = this.gitBranchTask.execute(workContext);

		// then
		assertNull(result.getError());
		assertEquals(result.getStatus(), WorkStatus.COMPLETED);

		assertDoesNotThrow(() -> {
			Ref branchRef = repository.findRef("newBranch");
			assertNotNull(branchRef);
			assertEquals(repository.getBranch(), "newBranch");
		});
	}

	@Test
	public void testBranchWithAlreadyBranch() {
		// given
		createSingleFileInRepo();

		assertDoesNotThrow(() -> {
			assertEquals(repository.getBranch(), defaultBranch);
		});
		WorkContext workContext = new WorkContext();
		workContext.put("path", tempDir.toString());
		WorkContextUtils.addParameter(workContext, "branch", defaultBranch);

		// when
		var result = this.gitBranchTask.execute(workContext);

		// then
		assertNotNull(result.getError());
		assertEquals(result.getStatus(), WorkStatus.FAILED);
	}

	@Test
	public void testWitMissingParams() {
		// given
		WorkContext workContext = new WorkContext();

		// when
		var result = this.gitBranchTask.execute(workContext);

		// then
		assertNotNull(result.getError());
		assertEquals(result.getStatus(), WorkStatus.FAILED);
		assertThat(result.getError()).isInstanceOf(MissingParameterException.class);
	}

	@Test
	public void testWithNoPath() {
		// given
		WorkContext workContext = new WorkContext();
		WorkContextUtils.addParameter(workContext, "branch", defaultBranch);

		// when
		var result = this.gitBranchTask.execute(workContext);

		// then
		assertNotNull(result.getError());
		assertEquals(result.getStatus(), WorkStatus.FAILED);
		assertThat(result.getError()).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testWithInvalidPath() {
		// given
		WorkContext workContext = new WorkContext();
		workContext.put("path", "/tmp/failingone");
		WorkContextUtils.addParameter(workContext, "branch", defaultBranch);

		// when
		var result = this.gitBranchTask.execute(workContext);

		// then
		assertNotNull(result.getError());
		assertThat(result.getError()).isInstanceOf(RuntimeException.class);
		assertThat(result.getError().toString()).contains("Ref HEAD cannot be resolved");
		assertEquals(result.getStatus(), WorkStatus.FAILED);
	}

	private void createSingleFileInRepo() {
		Path filePath = tempDir.resolve("file.txt");
		assertDoesNotThrow(() -> {
			Files.write(filePath, "Hello, world!".getBytes());
			Git git = new Git(repository);
			git.add().addFilepattern(".").call();
			git.commit().setMessage("Initial commit").setSign(false).call();
		});
	}

}
