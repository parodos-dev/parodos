package com.redhat.parodos.tasks.git;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.URIish;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Slf4j
public class GitPushTaskTest {

	private GitPushTask task;

	private Path gitRepoPath;

	private Path tempDir;

	private Repository repository;

	private Path remotePath;

	private Repository remoteRepository;

	@BeforeEach
	public void setUp() throws Exception {
		task = new GitPushTask();
		task.setBeanName("git-push-task");

		tempDir = Files.createTempDirectory("git-repo");
		gitRepoPath = tempDir.resolve(GitConstants.GIT_FOLDER);
		repository = initRepo(gitRepoPath);

		remotePath = Files.createTempDirectory("git-remote");
		remoteRepository = initRepo(remotePath.resolve(GitConstants.GIT_FOLDER));
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
	public void testParams() {
		// when
		List<WorkParameter> params = task.getWorkFlowTaskParameters();

		// then
		assertThat(params).isNotNull();
		assertThat(params.size()).isEqualTo(3);
		assertThat(params.get(0).getKey()).isEqualTo("path");
	}

	@Test
	public void testValidRemote() {
		// given
		WorkContext ctx = getSampleContext();
		ctx.put("path", tempDir.toString());
		WorkContextUtils.addParameter(ctx, "remote", "origin");

		createSingleFileInRepo();
		assertDoesNotThrow(() -> {
			addRemote(remotePath, "origin");
		});

		// then
		task.preExecute(ctx);
		WorkReport report = task.execute(ctx);

		// given
		assertThat(report.getError()).isNull();
		assertDoesNotThrow(() -> {
			RevCommit remoteCommit = getLastCommit(remoteRepository);
			RevCommit repoCommit = getLastCommit(repository);
			assertThat(repoCommit.getId()).isEqualTo(remoteCommit.getId());
		});
	}

	public void testInValidRemote() {
		// given
		WorkContext ctx = getSampleContext();
		ctx.put("path", tempDir.toString());
		WorkContextUtils.addParameter(ctx, "remote", "noValid");

		createSingleFileInRepo();
		assertDoesNotThrow(() -> {
			addRemote(remotePath, "origin");
		});
		// then
		task.preExecute(ctx);
		WorkReport report = task.execute(ctx);

		// given
		assertThat(report.getError()).isNotNull();
		assertThat(report.getError()).isInstanceOf(RuntimeException.class);
		assertThat(report.getError().getMessage()).contains("Cannot push to the remote noValid: noValid: not found");
		assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	@Test
	public void testRemoteParams() {
		// given
		WorkContext ctx = getSampleContext();
		ctx.put("path", tempDir.toString());

		// then
		task.preExecute(ctx);
		WorkReport report = task.execute(ctx);

		// given
		assertThat(report.getError()).isNotNull();
		assertThat(report.getError()).isInstanceOf(MissingParameterException.class);
		assertThat(report.getError().getMessage()).contains("ParameterName: remote");
		assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	@Test
	public void testInvalidRepo() {
		// given
		WorkContext ctx = getSampleContext();
		ctx.put("path", "/tmp/invalid");
		WorkContextUtils.addParameter(ctx, "remote", "origin");
		createSingleFileInRepo();
		assertDoesNotThrow(() -> {
			addRemote(remotePath, "origin");
		});

		// then
		task.preExecute(ctx);
		WorkReport report = task.execute(ctx);

		// given
		assertThat(report.getError()).isNotNull();
		assertThat(report.getError()).isInstanceOf(RuntimeException.class);
		assertThat(report.getError().getMessage()).contains("Cannot push to the remote origin");
		assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	@Test
	public void testwithoutPath() {
		// given
		WorkContext ctx = getSampleContext();

		// then
		task.preExecute(ctx);
		WorkReport report = task.execute(ctx);

		// given
		assertThat(report.getError()).isNotNull();
		assertThat(report.getError()).isInstanceOf(IllegalArgumentException.class);
		assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	private void addRemote(Path remotePath, String remote)
			throws IOException, URISyntaxException, GitAPIException, URISyntaxException {
		Git git = new Git(repository);
		git.remoteAdd().setName(remote).setUri(new URIish(remotePath.toFile().toURI().toString())).call();
		assertThat(git.remoteList().call().size()).isEqualTo(1);
	}

	private void createSingleFileInRepo() {
		Path filePath = tempDir.resolve("file.txt");
		assertDoesNotThrow(() -> {
			Files.write(filePath, "Git Push!".getBytes());
			Git git = Git.init().setDirectory(tempDir.toFile()).call();
			git.add().addFilepattern(".").call();
			git.commit().setMessage("Initial commit").setSign(false).call();
		});

	}

	private RevCommit getLastCommit(Repository repo) throws IOException {

		Ref head = repo.exactRef(GitConstants.GIT_HEAD);
		ObjectId headId = head.getObjectId();
		assertThat(headId).isNotNull();
		// Create a RevWalk instance to walk through commits
		RevWalk revWalk = new RevWalk(repo);
		assertThat(revWalk).isNotNull();

		// Parse the HEAD commit
		RevCommit headCommit = revWalk.parseCommit(headId);
		assertThat(headCommit).isNotNull();
		return headCommit;
	}

	private Repository initRepo(Path path) throws Exception {
		InitCommand command = new InitCommand();
		command.setInitialBranch("main");
		command.setBare(true);
		command.setDirectory(path.toFile());
		Git git = command.call();
		assertThat(git).isNotNull();
		assertThat(git.getRepository().getFullBranch()).isEqualTo("refs/heads/main");
		assertThat(git.getRepository().getBranch()).isEqualTo("main");
		return git.getRepository();
	}

	private WorkContext getSampleContext() {
		WorkContext context = new WorkContext();
		WorkContextUtils.setMainExecutionId(context, UUID.randomUUID());
		return context;
	}

}
