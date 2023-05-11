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
public class GitArchiveTaskTest {

	private GitArchiveTask gitArchiveTask;

	private Path gitRepoPath;

	private Repository repository;

	private Path tempDir;

	@BeforeEach
	public void setUp() throws Exception {
		gitArchiveTask = new GitArchiveTask();
		tempDir = Files.createTempDirectory("git-repo");

		gitRepoPath = tempDir.resolve(".git");

		this.repository = FileRepositoryBuilder.create(gitRepoPath.toFile());
		this.repository.create();
		log.info("Created a new repository at {} ", this.repository.getDirectory());
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
		createSingleFileInRepo();
		WorkContext workContext = new WorkContext();
		workContext.put("path", tempDir.toString());

		// when
		var result = this.gitArchiveTask.execute(workContext);

		// then
		assertNull(result.getError());
		assertEquals(result.getStatus(), WorkStatus.COMPLETED);
		assertThat(result.getWorkContext().get("gitArchivePath").toString()).contains("output.zip");
	}

	@Test
	public void testWithContextFromAnotherGit() {
		// given
		createSingleFileInRepo();
		WorkContext workContext = new WorkContext();
		workContext.put("gitDestination", tempDir.toString());

		// when
		var result = this.gitArchiveTask.execute(workContext);

		// then
		assertNull(result.getError());
		assertEquals(result.getStatus(), WorkStatus.COMPLETED);
		assertThat(result.getWorkContext().get("gitArchivePath").toString()).contains("output.zip");
	}

	@Test
	public void testWithInvalidParameters() {
		// given
		createSingleFileInRepo();
		WorkContext workContext = new WorkContext();

		// when
		var result = this.gitArchiveTask.execute(workContext);

		// then
		assertNotNull(result.getError());
		assertEquals(result.getStatus(), WorkStatus.FAILED);
		assertThat(result.getError().toString()).contains("The path parameter cannot be null or empty");
	}

	@Test
	public void testWithInvalidRepo() {
		// given
		createSingleFileInRepo();
		WorkContext workContext = new WorkContext();
		workContext.put("path", "noexists");

		// when
		var result = this.gitArchiveTask.execute(workContext);

		// then
		assertNotNull(result.getError());
		assertEquals(result.getStatus(), WorkStatus.FAILED);
		assertThat(result.getError().toString()).contains("Cannot archive the repository");
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
