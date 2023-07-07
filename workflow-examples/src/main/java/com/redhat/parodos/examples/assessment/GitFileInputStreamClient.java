/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.examples.assessment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.kohsuke.github.GitHub;

import com.redhat.parodos.patterndetection.clue.content.stream.ContentInputStreamClient;

import lombok.extern.slf4j.Slf4j;

/**
 * An example of a ContentInputStreamClient based on a Github client to obtain the
 * InputStreams
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class GitFileInputStreamClient implements ContentInputStreamClient {

	private static final String REPO = "REPO";

	private static final String BRANCH = "BRANCH";

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
