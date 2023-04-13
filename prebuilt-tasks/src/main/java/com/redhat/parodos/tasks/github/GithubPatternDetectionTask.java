/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.tasks.github;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;

import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;

import lombok.extern.slf4j.Slf4j;

/**
 * This is abstract class that provides functionality to get files from Github
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public abstract class GithubPatternDetectionTask extends BaseAssessmentTask {

	private static final String DIVIDER = "/";

	Map<String, List<String>> directoriesAndFilesPath = new HashMap<>();

	/*
	 * This needs to be supplied in the Workflow config, or is 'githubPersonalToken' is
	 * set as an environment variable, one will be created using the personal access token
	 * set in the variable
	 */
	private GitHub github;

	protected GithubPatternDetectionTask(GitHub github, List<WorkFlowOption> workflowOptions) {
		super(workflowOptions);
		this.github = github;
	}

	/**
	 * Returns the InputStream for a file in GitHub
	 * @param path
	 * @return
	 */
	public InputStream getFileData(String org, String branch, String filePath) {
		if (github == null) {
			log.error(
					"The connection to Github was not successfully established. Check the logs for more details. No read attempted forrepo: {} branch: {} file: {}",
					org, branch, filePath);
			return null;
		}
		try {
			return github.getRepository(org).getFileContent(filePath, branch).read();
		}
		catch (IOException e) {
			log.error("Unable to read the content for repo: {} branch: {} file: {}", org, branch, filePath);
			return null;
		}
	}

	/**
	 * Gets list of all the paths for directories and files within from Github. This is an
	 * expensive method for a large/monolithic repo
	 * @param repo
	 * @param branch
	 * @return
	 * @throws IOException
	 */
	public Map<String, List<String>> getDirectoriesAndFiles(String org, String repo) throws IOException {
		directoriesAndFilesPath.put(DIVIDER, new ArrayList<>());
		search(org, github.getRepository(repo).getDirectoryContent("/"));
		return directoriesAndFilesPath;
	}

	/*
	 * Recursively processes the repo to build up the collection
	 *
	 */
	private void search(String org, List<GHContent> gitHubContent) throws IOException {
		for (GHContent content : gitHubContent) {
			if (content.isDirectory()) {
				directoriesAndFilesPath.computeIfAbsent(content.getPath(), k -> new ArrayList<>());
				search(org, github.getRepository(org).getDirectoryContent(content.getPath()));
			}
			else {
				if (content.getPath().indexOf(DIVIDER) == -1) {
					directoriesAndFilesPath.get(DIVIDER).add(content.getPath());
				}
				else {
					directoriesAndFilesPath.get(content.getPath().substring(0, content.getPath().lastIndexOf(DIVIDER)))
							.add(content.getPath());
				}
			}
		}
	}

}
