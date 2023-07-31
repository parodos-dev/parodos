package com.redhat.parodos.tasks.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
		WorkContext workContext = getSampleContext();
		workContext.put("path", tempDir.toString());

		// when
		gitArchiveTask.preExecute(workContext);
		var result = this.gitArchiveTask.execute(workContext);

		// then
		assertThat(result.getError(), is(nullValue()));
		assertThat(result.getStatus(), equalTo(WorkStatus.COMPLETED));
		assertThat(result.getWorkContext().get("gitArchivePath").toString(), containsString("output.zip"));
		try {
			listFilesInZip(result.getWorkContext().get("gitArchivePath").toString());
		}
		catch (IOException e) {
			return;
		}
	}

	private static void listFilesInZip(String zipFilePath) throws IOException {
		File file = new File(zipFilePath);
		try (ZipFile zipFile = new ZipFile(file)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
			}
		}
	}

	@Test
	public void testWithContextFromAnotherGit() {
		// given
		createSingleFileInRepo();
		WorkContext workContext = getSampleContext();
		workContext.put("gitDestination", tempDir.toString());

		// when
		gitArchiveTask.preExecute(workContext);
		var result = this.gitArchiveTask.execute(workContext);

		// then
		assertThat(result.getError(), is(nullValue()));
		assertThat(result.getStatus(), equalTo(WorkStatus.COMPLETED));
		assertThat(result.getWorkContext().get("gitArchivePath").toString(), containsString("output.zip"));
	}

	@Test
	public void testWithInvalidParameters() {
		// given
		createSingleFileInRepo();
		WorkContext workContext = getSampleContext();

		// when
		gitArchiveTask.preExecute(workContext);
		var result = this.gitArchiveTask.execute(workContext);

		// then
		assertThat(result.getError(), is(notNullValue()));
		assertThat(result.getStatus(), equalTo(WorkStatus.FAILED));
		assertThat(result.getError().toString(), containsString("The path parameter cannot be null or empty"));

	}

	@Test
	public void testWithInvalidRepo() {
		// given
		createSingleFileInRepo();
		WorkContext workContext = getSampleContext();
		workContext.put("path", "noexists");

		// when
		gitArchiveTask.preExecute(workContext);
		var result = this.gitArchiveTask.execute(workContext);

		// then
		assertThat(result.getError(), is(notNullValue()));
		assertThat(result.getStatus(), equalTo(WorkStatus.FAILED));
		assertThat(result.getError().toString(), containsString("Cannot archive the repository"));
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
