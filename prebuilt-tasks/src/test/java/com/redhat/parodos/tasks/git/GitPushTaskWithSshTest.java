package com.redhat.parodos.tasks.git;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyPair;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.git.GitLocationResolver;
import org.apache.sshd.git.pack.GitPackCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.util.test.EchoShellFactory;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Slf4j
public class GitPushTaskWithSshTest {

	private static SshServer sshd;

	private GitPushTask task;

	private Path gitRepoPath;

	private Path tempDir;

	private Path remoteRepoPath;

	private static String remoteRepoPathName = "remote-repo";

	private Repository repository;

	private Path credentialsDir;

	public void createKeys() {
		SimpleGeneratorHostKeyProvider keyProvider = new SimpleGeneratorHostKeyProvider();
		keyProvider.setAlgorithm(KeyUtils.RSA_ALGORITHM);
		keyProvider.setKeySize(2048);
		assertDoesNotThrow(() -> {
			credentialsDir = Files.createTempDirectory("credentials");
			List<KeyPair> keyPair = keyProvider.loadKeys(null);
			Path privateKeyPah = credentialsDir.resolve("id_rsa");
			Path publicKeyPath = credentialsDir.resolve("id_rsa.pub");
			OpenSSHKeyPairResourceWriter keyPairWriter = new OpenSSHKeyPairResourceWriter();

			try (OutputStream out = new FileOutputStream(privateKeyPah.toFile())) {
				keyPairWriter.writePrivateKey(keyPair.get(0), null, null, out);
			}

			try (OutputStream out = new FileOutputStream(publicKeyPath.toFile())) {
				keyPairWriter.writePublicKey(keyPair.get(0), null, out);
			}

			Files.setPosixFilePermissions(privateKeyPah, PosixFilePermissions.fromString("rwx------"));
			Files.setPosixFilePermissions(publicKeyPath, PosixFilePermissions.fromString("rwxrwxrwx"));
		});
	}

	@BeforeEach
	public void setUp() throws Exception {
		task = new GitPushTask();
		task.setBeanName("git-push-task");
		createKeys();

		tempDir = Files.createTempDirectory("git-repo");
		Path remoteTempDir = Files.createTempDirectory("git-repo-remote");
		remoteRepoPath = Files.createDirectory(remoteTempDir.resolve(remoteRepoPathName),
				PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));

		assertDoesNotThrow(() -> {
			Repository repo = initRepo(remoteRepoPath);
		});

		sshd = SshServer.setUpDefaultServer();
		sshd.setPort(2222);
		FileKeyPairProvider keyPairProvider = new FileKeyPairProvider(credentialsDir.resolve("id_rsa"));
		sshd.setKeyPairProvider(keyPairProvider);
		AuthorizedKeysAuthenticator authorizedKeysAuthenticator = new AuthorizedKeysAuthenticator(
				credentialsDir.resolve("id_rsa.pub"));
		sshd.setPublickeyAuthenticator(authorizedKeysAuthenticator);
		sshd.setShellFactory(new EchoShellFactory());

		GitLocationResolver gitLocationResolver = new GitLocationResolver() {
			@Override
			public Path resolveRootDirectory(String command, String[] args, ServerSession session, FileSystem fs)
					throws IOException {
				return remoteRepoPath.getParent();
			}
		};

		sshd.setCommandFactory(new GitPackCommandFactory().withGitLocationResolver(gitLocationResolver));

		sshd.setPasswordAuthenticator((username, password, session) -> false);
		assertDoesNotThrow(() -> {
			sshd.start();
		});

		assertDoesNotThrow(() -> {
			CloneCommand command = new CloneCommand();
			command.setDirectory(tempDir.toFile());
			command.setURI("ssh://eloy@localhost:2222/remote-repo");

			command.setTransportConfigCallback(GitUtils.getTransport(credentialsDir.resolve("id_rsa")));
			Git git = command.call();
			repository = git.getRepository();
		});
	}

	@AfterEach
	public void tearDown() throws Exception {
		sshd.stop();
	}

	private Repository initRepo(Path path) throws Exception {
		InitCommand command = new InitCommand();
		command.setInitialBranch("main");
		command.setBare(true);
		command.setDirectory(path.toFile());
		Git git = command.call();
		assertThat(git, is(notNullValue()));
		assertThat(git.getRepository().getFullBranch(), equalTo("refs/heads/main"));
		assertThat(git.getRepository().getBranch(), equalTo("main"));

		return git.getRepository();
	}

	private void addCommit(Repository repo) {
		Git git = new Git(repo);

		assertDoesNotThrow(() -> {
			Files.write(tempDir.resolve("file.txt"), "Git Push!".getBytes());
			git.add().addFilepattern(".").call();
			git.commit().setMessage("next commit").setSign(false).call();
		});
	}

	@Test
	public void testWithValidInfo() {

		// given
		WorkContext ctx = getSampleContext();
		ctx.put("path", tempDir.toString());
		WorkContextUtils.addParameter(ctx, "remote", "origin");
		WorkContextUtils.addParameter(ctx, "credentials", credentialsDir.resolve("id_rsa").toString());
		addCommit(repository);

		// then
		task.preExecute(ctx);
		WorkReport report = task.execute(ctx);

		// given
		assertThat(report.getError(), is(nullValue()));
		assertThat(report.getStatus(), equalTo(WorkStatus.COMPLETED));
	}

	@Test
	public void testWithInvalidKeys() {

		// given
		WorkContext ctx = getSampleContext();
		ctx.put("path", tempDir.toString());
		WorkContextUtils.addParameter(ctx, "remote", "origin");
		WorkContextUtils.addParameter(ctx, "credentials", credentialsDir.resolve("invalid").toString());
		addCommit(repository);

		// then
		task.preExecute(ctx);
		WorkReport report = task.execute(ctx);

		// given
		assertThat(report.getError(), is(notNullValue()));
		assertThat(report.getStatus(), equalTo(WorkStatus.FAILED));
	}

	private WorkContext getSampleContext() {
		WorkContext context = new WorkContext();
		WorkContextUtils.setMainExecutionId(context, UUID.randomUUID());
		return context;
	}

}
