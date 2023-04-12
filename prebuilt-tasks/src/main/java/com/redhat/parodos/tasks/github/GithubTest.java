package com.redhat.parodos.tasks.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

public class GithubTest {

	Map<String, List<String>> directoriesAndFilesPath = new HashMap<>();

	GitHub github;

	String org = "parodos-dev/parodos";

	public GithubTest() throws IOException {
		github = new GitHubBuilder().withOAuthToken("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").build();
	}

	private void listFileContents(String org, String branch, String path) throws IOException {
		GHContent content = github.getRepository(org).getFileContent(path, branch);
		List<String> lines = IOUtils.readLines(content.read(), StandardCharsets.UTF_8);
		lines.forEach(System.out::println);
	}

	public static void main(String[] args) throws IOException {
		GithubTest test = new GithubTest();
		test.listFileContents("parodos-dev/parodos", "main", "parodos-model-api/pom.xml");
		test.listDirectoriesAndFiles("parodos-dev/parodos", "/");
	}

	public void listDirectoriesAndFiles(String repo, String startingPath) throws IOException {
		directoriesAndFilesPath.put("/", new ArrayList<>());
		search(github.getRepository(repo).getDirectoryContent(startingPath));
		directoriesAndFilesPath.forEach((k, v) -> {
			System.out.println("key: " + k + ", value: " + v.toArray());
		});
	}

	private void search(List<GHContent> gitHubContent) throws IOException {
		for (GHContent content : gitHubContent) {
			if (content.isDirectory()) {
				directoriesAndFilesPath.computeIfAbsent(content.getPath(), k -> new ArrayList<>());
				search(github.getRepository(org).getDirectoryContent(content.getPath()));
			}
			else {
				if (content.getPath().indexOf("/") == -1) {
					directoriesAndFilesPath.get("/").add(content.getPath());
				}
				else {
					// get the parent
					directoriesAndFilesPath.get(content.getPath().substring(0, content.getPath().lastIndexOf("/")))
							.add(content.getPath());
				}
			}
		}
	}

}
