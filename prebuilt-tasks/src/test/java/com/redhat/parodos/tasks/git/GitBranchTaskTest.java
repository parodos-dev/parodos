package com.redhat.parodos.tasks.git;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
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
		assertThat(git).isNotNull();
		assertThat(git.getRepository().getFullBranch()).isEqualTo("refs/heads/main");
		assertThat(git.getRepository().getBranch()).isEqualTo("main");
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
		WorkContext workContext = getSampleContext();
		workContext.put("path", tempDir.toString());
		WorkContextUtils.addParameter(workContext, "branch", "newBranch");
		WorkContextUtils.addParameter(workContext, "path", tempDir.toString());

		// when
		gitBranchTask.preExecute(workContext);
		var result = gitBranchTask.execute(workContext);

		// then
		assertThat(result.getError()).isNull();
		assertThat(result.getStatus()).isEqualTo(WorkStatus.COMPLETED);

		assertDoesNotThrow(() -> {
			Ref branchRef = repository.findRef("newBranch");
			assertThat(branchRef).isNotNull();
			assertThat(repository.getBranch()).isEqualTo("newBranch");
		});
	}

	@Test
	public void testBranchWithAlreadyBranch() {
		// given
		createSingleFileInRepo();

		assertDoesNotThrow(() -> {
			assertThat(repository.getBranch()).isEqualTo(defaultBranch);
		});
		WorkContext workContext = getSampleContext();
		workContext.put("path", tempDir.toString());
		WorkContextUtils.addParameter(workContext, "branch", defaultBranch);

		// when
		gitBranchTask.preExecute(workContext);
		var result = gitBranchTask.execute(workContext);

		// then
		assertThat(result.getError()).isNotNull();
		assertThat(result.getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	@Test
	public void testWitMissingParams() {
		// given
		WorkContext workContext = getSampleContext();

		// when
		gitBranchTask.preExecute(workContext);
		var result = gitBranchTask.execute(workContext);

		// then
		assertThat(result.getError()).isNotNull();
		assertThat(result.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(result.getError()).isInstanceOf(MissingParameterException.class);
	}

	@Test
	public void testWithNoPath() {
		// given
		WorkContext workContext = getSampleContext();
		WorkContextUtils.addParameter(workContext, "branch", defaultBranch);

		// when
		gitBranchTask.preExecute(workContext);
		var result = gitBranchTask.execute(workContext);

		// then
		assertThat(result.getError()).isNotNull();
		assertThat(result.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(result.getError()).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testWithInvalidPath() {
		// given
		WorkContext workContext = getSampleContext();
		workContext.put("path", "/tmp/failingone");
		WorkContextUtils.addParameter(workContext, "branch", defaultBranch);

		// when
		gitBranchTask.preExecute(workContext);
		var result = gitBranchTask.execute(workContext);

		// then
		assertThat(result.getError()).isNotNull();
		assertThat(result.getError()).isInstanceOf(RuntimeException.class);
		assertThat(result.getError().toString()).contains("Ref HEAD cannot be resolved");
		assertThat(result.getStatus()).isEqualTo(WorkStatus.FAILED);
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

	private WorkContext getSampleContext() {
		WorkContext context = new WorkContext();
		WorkContextUtils.setMainExecutionId(context, UUID.randomUUID());
		return context;
	}

}
