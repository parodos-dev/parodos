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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import com.redhat.parodos.patterndetection.exceptions.PatternDetectionRuntimeException;

/**
 * Helper methods for working with Files and their contents.
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class FileContentsDelegate {

	/**
	 * Provides a list of Files in a base directory
	 * @param basePath
	 * @return list of Strings containing the full path of all the files in the base
	 * directory
	 * @throws IOException
	 */
	public List<String> getFilePaths(String basePath) throws IOException {
		if (!doesTargetDirectoryExist(basePath)) {
			throw new IOException("Supplied path to scan: " + basePath + " does not exist");
		}
		List<String> filePaths;
		try (Stream<Path> walk = Files.walk(Paths.get(basePath))) {
			filePaths = walk.filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
		}
		return filePaths;
	}

	private boolean doesTargetDirectoryExist(String basePath) {
		if (basePath == null) {
			return false;
		}
		return new File(basePath).exists();
	}

	/**
	 * Provides a list of Folders (and Sub Folders) under a base directory
	 * @param basePath
	 * @return List of String values, each value containing the full path of a folder or
	 * sub-folder found under the base directory
	 * @throws IOException
	 */
	public List<String> getFolderPaths(String basePath) {
		File f = new File(basePath);
		List<String> paths = new ArrayList<>();
		getPaths(f, basePath, paths);
		return paths;
	}

	/**
	 * Converts an InputStream to a list of strings. This is useful when the contents of a
	 * file has been converted into a list of strings (ie: getting the contents of a file
	 * from github)
	 * @param content
	 * @return
	 */
	public List<String> getContentFromInputStream(InputStream content) {
		try {
			return IOUtils.readLines(content, StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new PatternDetectionRuntimeException("Unable to convert input stream to content list: ", e);
		}
	}

	/**
	 * A recursive method that updates the passed in List of strings to contains the paths
	 * of each file/folder found under the directory
	 * @param dir Current location in the scan (can be a file or folder). If its a folder
	 * it triggers a recursive call
	 * @param base the location from which the scan started
	 * @param paths the list of file system locations as a String that is being
	 * recursively brought up
	 */
	public void getPaths(File dir, String base, List<String> paths) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				paths.add(file.getPath().replace(base, ""));
				getPaths(file, base, paths);
			}
		}
	}

	/**
	 * Gets the contents of a file as List of Strings
	 * @param currentFile
	 * @return List of Strings with the contents of the file
	 * @throws IOException
	 */
	public List<String> fileContentsToList(File currentFile) throws IOException {
		List<String> fileContent;
		try (Stream<String> stream = Files.lines(Paths.get(currentFile.getCanonicalPath()))) {
			fileContent = stream.collect(Collectors.toList());
		}
		return fileContent;
	}

}
