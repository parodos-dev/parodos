package com.redhat.parodos.tasks.git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Slf4j
class GitCommitTaskTest {

	private GitCommitTask gitCommitTask;

	private Path gitRepoPath;

	private Repository repository;

	private static String commitMessageCtxKey = "commitMessage";

	private Path tempDir;

	@BeforeEach
	public void setUp() throws Exception {
		gitCommitTask = new GitCommitTask();
		gitCommitTask.setBeanName("GitCommitTask");
		tempDir = Files.createTempDirectory("git-repo");

		gitRepoPath = tempDir.resolve(".git");

		this.repository = FileRepositoryBuilder.create(gitRepoPath.toFile());
		this.repository.create();
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
	public void testWithValidData() {
		// given
		String message = "My commit message";
		WorkContext workContext = getSampleContext();
		workContext.put("path", tempDir.toString());
		WorkContextUtils.addParameter(workContext, commitMessageCtxKey, message);

		// when
		gitCommitTask.preExecute(workContext);
		WorkReport report = gitCommitTask.execute(workContext);

		// then
		assertThat(report.getError()).isNull();
		assertThat(report.getStatus()).isEqualTo(WorkStatus.COMPLETED);

		assertDoesNotThrow(() -> {
			RevCommit commit = getLastCommit();
			assertThat(commit.getFullMessage()).isEqualTo(message);
		});
	}

	@Test
	public void testWithNoCommitMessage() {
		// given
		WorkContext workContext = getSampleContext();
		workContext.put("path", tempDir.toString());

		// when
		gitCommitTask.preExecute(workContext);
		WorkReport report = gitCommitTask.execute(workContext);

		// then
		assertThat(report.getError()).isNotNull();
		assertThat(report.getError()).isInstanceOf(MissingParameterException.class);
		assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	@Test
	public void testWithMissingRepo() {
		// given
		WorkContext workContext = getSampleContext();
		WorkContextUtils.addParameter(workContext, commitMessageCtxKey, "new one");

		// when
		gitCommitTask.preExecute(workContext);
		WorkReport report = gitCommitTask.execute(workContext);

		// then
		assertThat(report.getError()).isNotNull();
		assertThat(report.getError()).isInstanceOf(IllegalArgumentException.class);
		assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	@Test
	public void testWithEmptyRepo() {
		// given
		WorkContext workContext = getSampleContext();
		workContext.put("path", "");
		WorkContextUtils.addParameter(workContext, commitMessageCtxKey, "new one");

		// when
		gitCommitTask.preExecute(workContext);
		WorkReport report = gitCommitTask.execute(workContext);

		// then
		assertThat(report.getError()).isNotNull();
		assertThat(report.getError()).isInstanceOf(IllegalArgumentException.class);
		assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	@Test
	public void testWithInvalidRepo() {
		// given
		WorkContext workContext = getSampleContext();
		workContext.put("path", "/tmp/invalidRepo");
		WorkContextUtils.addParameter(workContext, commitMessageCtxKey, "new one");

		// when
		gitCommitTask.preExecute(workContext);
		WorkReport report = gitCommitTask.execute(workContext);

		// then
		assertThat(report.getError()).isNotNull();
		assertThat(report.getError()).isInstanceOf(Exception.class);
		assertThat(report.getError().getMessage()).contains("Commit on repo without HEAD currently not supported");
		assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	private void createSingleFileInRepo() {
		Path filePath = tempDir.resolve("file.txt");
		assertDoesNotThrow(() -> {
			Files.write(filePath, "Hello, world!".getBytes());
			Git git = Git.init().setDirectory(tempDir.toFile()).call();
			git.add().addFilepattern(".").call();
			git.commit().setSign(false).setMessage("Initial commit").call();
		});
	}

	private RevCommit getLastCommit() throws IOException {

		Ref head = repository.exactRef("HEAD");
		ObjectId headId = head.getObjectId();
		assertThat(headId).isNotNull();
		// Create a RevWalk instance to walk through commits
		RevWalk revWalk = new RevWalk(repository);
		assertThat(revWalk).isNotNull();

		// Parse the HEAD commit
		RevCommit headCommit = revWalk.parseCommit(headId);
		assertThat(headCommit).isNotNull();
		return headCommit;
	}

	private WorkContext getSampleContext() {
		WorkContext context = new WorkContext();
		WorkContextUtils.setMainExecutionId(context, UUID.randomUUID());
		return context;
	}

}
