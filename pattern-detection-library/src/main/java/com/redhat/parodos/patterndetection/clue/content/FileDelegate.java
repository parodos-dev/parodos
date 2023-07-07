package com.redhat.parodos.patterndetection.clue.content;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains logic related to File and Folders
 * 
 * @author Luke Shannon (Github: lshannon)
 */
public class FileDelegate {
	
	private static FileDelegate instance = null;

	private FileDelegate() {
	}

	public static synchronized FileDelegate getInstance() {
		if (instance == null) {
			instance = new FileDelegate();
		}
		return instance;
	}
	
	/**
	 * Provides a list of Files in a base directory. This is applicable for when the
	 * file system exists on disk
	 * 
	 * @param startFolder - starting point to scan
	 * @return list of Strings containing the full path of all the files in the base
	 *         directory
	 * @throws IOException
	 */
	public List<String> getFilePaths(String startFolder) throws IOException {
		if (!doesTargetDirectoryExist(startFolder)) {
			throw new IOException("Supplied path to scan: " + startFolder + " does not exist");
		}
		List<String> filePaths;
		try (Stream<Path> walk = Files.walk(Paths.get(startFolder))) {
			filePaths = walk.filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
		}
		return filePaths;
	}
	
	private static boolean doesTargetDirectoryExist(String basePath) {
		return basePath != null && new File(basePath).exists();
	}

	/**
	 * Provides a list of Folders (and Sub Folders) under a base directory
	 * 
	 * @param basePath
	 * @return List of String values, each value containing the full path of a
	 *         folder or sub-folder found under the base directory
	 * @throws IOException
	 */
	public List<String> getFolderPaths(String basePath) {
		File f = new File(basePath);
		List<String> paths = new ArrayList<>();
		getPaths(f, basePath, paths);
		return paths;
	}

	private void getPaths(File dir, String base, List<String> paths) {
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
	 * 
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
