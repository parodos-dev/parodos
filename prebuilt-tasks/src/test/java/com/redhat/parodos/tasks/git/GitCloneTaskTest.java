package com.redhat.parodos.tasks.git;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class GitCloneTaskTest {

	private GitCloneTask gitCloneTask;

	private Path gitRepoPath;

	private Repository repository;

	private Path tempDir;

	@BeforeEach
	public void setUp() throws Exception {
		gitCloneTask = new GitCloneTask();
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
	public void testWithValidClone() {
		// given
		WorkContext workContext = new WorkContext();
		workContext.put("uri", this.repository.getDirectory().toString());
		workContext.put("branch", "master");

		// then
		var result = this.gitCloneTask.execute(workContext);

		// when
		assertNull(result.getError());
		assertEquals(result.getStatus(), WorkStatus.COMPLETED);
		assertNotNull(result.getWorkContext().get("gitDestination"));
		assertNotNull(result.getWorkContext().get("gitUri"));
	}

	@Test
	public void testWithInValidClone() {
		// given
		WorkContext workContext = new WorkContext();
		workContext.put("uri", "InvalidFolder");
		workContext.put("branch", "master");

		// then
		var result = this.gitCloneTask.execute(workContext);

		// when
		assertNotNull(result.getError());
		assertThat(result.getError().toString()).contains("Remote repository InvalidFolder is not available");
		assertEquals(result.getStatus(), WorkStatus.FAILED);
		assertNull(result.getWorkContext().get("gitDestination"));
		assertNull(result.getWorkContext().get("gitUri"));
	}

	@Test
	public void testWithInValidBranch() {
		// given
		WorkContext workContext = new WorkContext();
		workContext.put("uri", this.repository.getDirectory().toString());
		workContext.put("branch", "fooBranch");

		// then
		var result = this.gitCloneTask.execute(workContext);

		// when
		assertNotNull(result.getError());
		assertThat(result.getError().toString()).contains("cannot connect to the repository server");
		assertEquals(result.getStatus(), WorkStatus.FAILED);
		assertNull(result.getWorkContext().get("gitDestination"));
		assertNull(result.getWorkContext().get("gitUri"));
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

}
