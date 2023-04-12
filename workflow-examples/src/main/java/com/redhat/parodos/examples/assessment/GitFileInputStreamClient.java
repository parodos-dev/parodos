package com.redhat.parodos.examples.assessment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.kohsuke.github.GitHub;

import com.redhat.parodos.patterndetection.clue.client.ContentInputStreamClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitFileInputStreamClient implements ContentInputStreamClient {

	private static final String REPO = "repo";

	private static final String BRANCH = "branch";

	private GitHub github;

	public GitFileInputStreamClient(GitHub github) {
		super();
		this.github = github;
	}

	@Override
	public String getName() {
		return "Github Pattern Detection Task";
	}

	@Override
	public InputStream getContentIfRequired(String filePath, Map<String, Object> map) {
		if (github == null) {
			log.error(
					"The connection to Github was not successfully established. Check the logs for more details. No read attempted forrepo: {} branch: {} file: {}",
					map.get(REPO), map.get(BRANCH), filePath);
			return null;
		}
		try {
			return github.getRepository((String) map.get(REPO)).getFileContent(filePath, (String) map.get(BRANCH))
					.read();
		}
		catch (IOException e) {
			log.error("Unable to read the content for repo: {} branch: {} file: {}", map.get(REPO), map.get(BRANCH),
					filePath);
			return null;
		}
	}

}
