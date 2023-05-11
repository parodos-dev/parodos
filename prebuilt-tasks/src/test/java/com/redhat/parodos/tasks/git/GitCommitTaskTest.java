package com.redhat.parodos.tasks.git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
		WorkContext workContext = new WorkContext();
		workContext.put("path", tempDir.toString());
		WorkContextUtils.addParameter(workContext, commitMessageCtxKey, message);

		// when
		WorkReport report = gitCommitTask.execute(workContext);

		// then
		assertNull(report.getError());
		assertEquals(report.getStatus(), WorkStatus.COMPLETED);

		assertDoesNotThrow(() -> {
			RevCommit commit = getLastCommit();
			assertEquals(commit.getFullMessage(), message);
		});
	}

	@Test
	public void testWithNoCommitMessage() {
		// given
		WorkContext workContext = new WorkContext();
		workContext.put("path", tempDir.toString());
		// when
		WorkReport report = gitCommitTask.execute(workContext);

		// then
		assertNotNull(report.getError());
		assertThat(report.getError()).isInstanceOf(MissingParameterException.class);
		assertEquals(report.getStatus(), WorkStatus.FAILED);
	}

	@Test
	public void testWithMissingRepo() {
		// given
		WorkContext workContext = new WorkContext();
		WorkContextUtils.addParameter(workContext, commitMessageCtxKey, "new one");

		// when
		WorkReport report = gitCommitTask.execute(workContext);

		// then
		assertNotNull(report.getError());
		assertThat(report.getError()).isInstanceOf(IllegalArgumentException.class);
		assertEquals(report.getStatus(), WorkStatus.FAILED);
	}

	@Test
	public void testWithEmptyRepo() {
		// given
		WorkContext workContext = new WorkContext();
		workContext.put("path", "");
		WorkContextUtils.addParameter(workContext, commitMessageCtxKey, "new one");

		// when
		WorkReport report = gitCommitTask.execute(workContext);

		// then
		assertNotNull(report.getError());
		assertThat(report.getError()).isInstanceOf(IllegalArgumentException.class);
		assertEquals(report.getStatus(), WorkStatus.FAILED);
	}

	@Test
	public void testWithInvalidRepo() {
		// given
		WorkContext workContext = new WorkContext();
		workContext.put("path", "/tmp/invalidRepo");
		WorkContextUtils.addParameter(workContext, commitMessageCtxKey, "new one");

		// when
		WorkReport report = gitCommitTask.execute(workContext);

		// then
		assertNotNull(report.getError());
		assertThat(report.getError()).isInstanceOf(Exception.class);
		assertThat(report.getError().getMessage()).contains("Commit on repo without HEAD currently not supported");
		assertEquals(report.getStatus(), WorkStatus.FAILED);
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

	private RevCommit getLastCommit() throws IOException {

		Ref head = repository.exactRef("HEAD");
		ObjectId headId = head.getObjectId();
		assertNotNull(headId);
		// Create a RevWalk instance to walk through commits
		RevWalk revWalk = new RevWalk(repository);
		assertNotNull(revWalk);

		// Parse the HEAD commit
		RevCommit headCommit = revWalk.parseCommit(headId);
		assertNotNull(headCommit);
		return headCommit;
	}

}
