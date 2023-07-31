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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
		assertThat(git, is(notNullValue()));
		assertThat(git.getRepository().getFullBranch(), equalTo("refs/heads/main"));
		assertThat(git.getRepository().getBranch(), equalTo("main"));
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
		assertThat(result.getError(), is(nullValue()));
		assertThat(result.getStatus(), equalTo(WorkStatus.COMPLETED));

		assertDoesNotThrow(() -> {
			Ref branchRef = repository.findRef("newBranch");
			assertThat(branchRef, is(notNullValue()));
			assertThat(repository.getBranch(), equalTo("newBranch"));
		});
	}

	@Test
	public void testBranchWithAlreadyBranch() {
		// given
		createSingleFileInRepo();

		assertDoesNotThrow(() -> {
			assertThat(repository.getBranch(), equalTo(defaultBranch));
		});
		WorkContext workContext = getSampleContext();
		workContext.put("path", tempDir.toString());
		WorkContextUtils.addParameter(workContext, "branch", defaultBranch);

		// when
		gitBranchTask.preExecute(workContext);
		var result = gitBranchTask.execute(workContext);

		// then
		assertThat(result.getError(), is(notNullValue()));
		assertThat(result.getStatus(), equalTo(WorkStatus.FAILED));
	}

	@Test
	public void testWitMissingParams() {
		// given
		WorkContext workContext = getSampleContext();

		// when
		gitBranchTask.preExecute(workContext);
		var result = gitBranchTask.execute(workContext);

		// then
		assertThat(result.getError(), is(notNullValue()));
		assertThat(result.getStatus(), equalTo(WorkStatus.FAILED));
		assertThat(result.getError(), is(instanceOf(MissingParameterException.class)));
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
		assertThat(result.getError(), is(notNullValue()));
		assertThat(result.getStatus(), equalTo(WorkStatus.FAILED));
		assertThat(result.getError(), is(instanceOf(IllegalArgumentException.class)));
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
		assertThat(result.getError(), is(notNullValue()));
		assertThat(result.getError(), is(instanceOf(RuntimeException.class)));
		assertThat(result.getError().toString(), containsString("Ref HEAD cannot be resolved"));
		assertThat(result.getStatus(), equalTo(WorkStatus.FAILED));
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
