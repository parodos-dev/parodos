package com.redhat.parodos.tasks.git;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflows.work.WorkContext;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;

public abstract class GitUtils {

	private GitUtils() {
	}

	public static String getRepoPath(WorkContext workContext) {
		var dest = workContext.get(GitConstants.CONTEXT_DESTINATION);
		if (dest == null) {
			return WorkContextDelegate.getOptionalValueFromRequestParams(workContext, GitConstants.GIT_REPO_PATH, "");
		}
		return WorkContextDelegate.getOptionalValueFromRequestParams(workContext, GitConstants.GIT_REPO_PATH,
				dest.toString());
	}

	public static Repository getRepo(String path) throws IOException {
		Path gitDir = Paths.get(path).resolve(GitConstants.GIT_FOLDER);
		return new FileRepositoryBuilder().setGitDir(gitDir.toFile()).build();
	}

	public static TransportConfigCallback getTransport(Path sshKeyPath) throws IOException {
		if (!sshKeyPath.toFile().exists()) {
			throw new IOException("SSH key file at '%s' does not exists".formatted(sshKeyPath.toString()));
		}

		var sshSessionFactory = new JschConfigSessionFactory() {
			@Override
			protected void configure(OpenSshConfig.Host host, Session session) {
				session.setConfig("StrictHostKeyChecking", "no");
				session.setConfig("PreferredAuthentications", "publickey");
			}

			@Override
			protected JSch createDefaultJSch(FS fs) throws JSchException {
				JSch defaultJSch = super.createDefaultJSch(fs);
				defaultJSch.removeAllIdentity();
				defaultJSch.addIdentity(sshKeyPath.toString());
				return defaultJSch;
			}
		};
		return new TransportConfigCallback() {
			@Override
			public void configure(Transport transport) {
				SshTransport sshTransport = (SshTransport) transport;
				sshTransport.setSshSessionFactory(sshSessionFactory);
			}
		};
	}

}
