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
package com.redhat.parodos.patterndetection.clue.delegate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import com.redhat.parodos.patterndetection.clue.InputStreamWrapper;
import com.redhat.parodos.patterndetection.clue.client.ContentInputStreamClientConfiguration;

/**
 * Helper methods for working with different content that might be used for detection.
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class ContentsDelegate {

	// Only static method access
	private ContentsDelegate() {
	}

	/**
	 * Provides a list of Files in a base directory. This is applicable for when the file
	 * system exists on disk
	 * @param basePath - starting point to scan
	 * @return list of Strings containing the full path of all the files in the base
	 * directory
	 * @throws IOException
	 */
	public static final List<String> getFilePaths(String basePath) throws IOException {
		if (!doesTargetDirectoryExist(basePath)) {
			throw new IOException("Supplied path to scan: " + basePath + " does not exist");
		}
		List<String> filePaths;
		try (Stream<Path> walk = Files.walk(Paths.get(basePath))) {
			filePaths = walk.filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
		}
		return filePaths;
	}

	private static boolean doesTargetDirectoryExist(String basePath) {
		return basePath != null && new File(basePath).exists();
	}

	/**
	 * Provides a list of Folders (and Sub Folders) under a base directory
	 * @param basePath
	 * @return List of String values, each value containing the full path of a folder or
	 * sub-folder found under the base directory
	 * @throws IOException
	 */
	public static final List<String> getFolderPaths(String basePath) {
		File f = new File(basePath);
		List<String> paths = new ArrayList<>();
		getPaths(f, basePath, paths);
		return paths;
	}

	private static final void getPaths(File dir, String base, List<String> paths) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				paths.add(file.getPath().replace(base, ""));
				getPaths(file, base, paths);
			}
		}
	}

	/**
	 * Converts an InputStream to a list of strings. This is useful when the contents of a
	 * file has been converted into a list of strings (ie: getting the contents of a file
	 * from github)
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public static final List<String> inputStreamToList(InputStream content) throws IOException {
		return IOUtils.readLines(content, StandardCharsets.UTF_8);
	}

	/**
	 * Iterates through the supplied paths and uses the supplied client to generate a list
	 * of InputstreamWrappers. This gives an opportunity for the client to make custom
	 * decisions around how expensive InputStreams can be obtained
	 * @param client
	 * @param paths
	 * @return
	 */
	public static final List<InputStreamWrapper> getContentUsingClient(ContentInputStreamClientConfiguration client) {
		return client.getPathsToProcessForContent().stream().map(path -> {
			InputStream stream = client.getContentClient().getContentIfRequired(path, client.getParametersForClient());
			if (stream != null) {
				return InputStreamWrapper.builder().inputStream(stream).fileName(path).build();
			}
			else {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 * Gets the contents of a file as List of Strings
	 * @param currentFile
	 * @return List of Strings with the contents of the file
	 * @throws IOException
	 */
	public static final List<String> fileContentsToList(File currentFile) throws IOException {
		List<String> fileContent;
		try (Stream<String> stream = Files.lines(Paths.get(currentFile.getCanonicalPath()))) {
			fileContent = stream.collect(Collectors.toList());
		}
		return fileContent;
	}

}
