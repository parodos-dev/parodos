package com.redhat.parodos.patterndetection.context;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.redhat.parodos.patterndetection.clue.Clue;
import com.redhat.parodos.patterndetection.clue.InputStreamWrapper;
import com.redhat.parodos.patterndetection.clue.client.ContentInputStreamClientConfiguration;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionConfigurationException;
import com.redhat.parodos.patterndetection.pattern.Pattern;
import com.redhat.parodos.workflows.work.WorkContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PatternDetectionContextBuilder {
	
	private final WorkContext context;
	
	private List<Pattern> desiredPatterns;

	private String startDirectory;

	private List<ContentInputStreamClientConfiguration> contentClients;

	private Map<String, ArrayList<String>> directoriesAndFiles;

	private List<File> files;

	private List<File> folders;

	private List<InputStreamWrapper> inputStreams;


	public PatternDetectionContextBuilder() {
		context = new WorkContext();
		desiredPatterns = new ArrayList<>();
		directoriesAndFiles = new HashMap<>();
		inputStreams = new ArrayList<>();
		contentClients = new ArrayList<>();
		files = new ArrayList<>();
		folders = new ArrayList<>();
	}
	
	/*
	 * Internal class to be used by the File Walker to collect the files and
	 * directories
	 */
	class CollectFiles extends SimpleFileVisitor<Path> {

		List<File> fileList;

		List<File> directoryList;

		public CollectFiles(List<File> fileList2, List<File> directoryList2) {
			super();
			this.fileList = fileList2;
			this.directoryList = directoryList2;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
			fileList.add(file.toFile());
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
			directoryList.add(dir.toFile());
			return FileVisitResult.CONTINUE;
		}

	}

	
	/*
	 * Used when a root path is given using the START_DIRECTORY. A list of files and
	 * folders were be determined by recursively stepping through the file hierarchy
	 */
	private void getFilesAndDirectoriesFromLocalStartFolder(WorkContext context) {
		if (context.get(PatternDetectionConstants.START_DIRECTORY.toString()) != null) {
			List<File> fileList = new ArrayList<>();
			List<File> directoryList = new ArrayList<>();
			try {
				Files.walkFileTree(
						Paths.get(((String) context.get(PatternDetectionConstants.START_DIRECTORY.toString()))),
						new CollectFiles(fileList, directoryList));
			} catch (IOException e) {
				log.error("Unable to get the folders and files to process. Start Directory: {}",
						(String) context.get(PatternDetectionConstants.START_DIRECTORY.toString()), e);
			}
			context.put(PatternDetectionConstants.FILES_TO_SCAN.toString(), fileList);
			context.put(PatternDetectionConstants.FOLDERS_TO_SCAN.toString(), directoryList);
		}
	}


	public PatternDetectionContextBuilder addFileToFilesToScan(File file) {
		this.files.add(file);
		return this;
	}

	public PatternDetectionContextBuilder addAllToFilesToScan(List<File> files) {
		this.files.addAll(files);
		return this;
	}

	public PatternDetectionContextBuilder addFolderToFoldersToScan(File folder) {
		this.folders.add(folder);
		return this;
	}

	public PatternDetectionContextBuilder addAllToFoldersToScan(List<File> folders) {
		this.folders.addAll(folders);
		return this;
	}

	public PatternDetectionContextBuilder setDirectoriesAndFiles(Map<String, ArrayList<String>> directoriesAndFiles) {
		this.directoriesAndFiles = directoriesAndFiles;
		return this;
	}

	public PatternDetectionContextBuilder addContentInputStreamClientAndPaths(ContentInputStreamClientConfiguration client) {
		this.contentClients.add(client);
		return this;
	}

	public PatternDetectionContextBuilder addAllToContentInputStreamClientAndPaths(
			List<ContentInputStreamClientConfiguration> clients) {
		this.contentClients.addAll(clients);
		return this;
	}

	public PatternDetectionContextBuilder startDirectory(String startDirectory) {
		this.startDirectory = startDirectory;
		return this;
	}

	public PatternDetectionContextBuilder setInputStreams(List<InputStreamWrapper> inputStreams) {
		this.inputStreams = inputStreams;
		return this;
	}

	public PatternDetectionContextBuilder addThisToInputStreams(InputStreamWrapper inputStream) {
		inputStreams.add(inputStream);
		return this;
	}

	public PatternDetectionContextBuilder addThisToDesiredPatterns(Pattern pattern) {
		desiredPatterns.add(pattern);
		return this;
	}

	public PatternDetectionContextBuilder addTheseToDesiredPatterns(List<Pattern> newDesiredPatterns) {
		desiredPatterns.addAll(newDesiredPatterns);
		return this;
	}
	
	/*
	 * Create the Maps and lists to contain detected Clues and Patterns
	 */
	private void initializeContext(WorkContext context) {
		if (context.get(PatternDetectionConstants.DETECTED_CLUES.toString()) == null) {
			context.put(PatternDetectionConstants.DETECTED_CLUES.toString(), new HashMap<Clue, List<File>>());
		}
		if (context.get(PatternDetectionConstants.DETECTED_PATTERNS.toString()) == null) {
			context.put(PatternDetectionConstants.DETECTED_PATTERNS.toString(), new ArrayList<Pattern>());
		}
		context.put(PatternDetectionConstants.DETECTED_PATTERNS.toString(), new ArrayList<Pattern>());
	}

	/**
	 * Method for building a WorkContext
	 * 
	 * @return WorkContext with
	 */
	public WorkContext build() {
	
		// handle the case of there being a local directory that detection will start
		Optional.ofNullable(startDirectory).ifPresent(dir -> {
			context.put(PatternDetectionConstants.START_DIRECTORY.toString(), dir);
			getFilesAndDirectoriesFromLocalStartFolder(context);
		});

		// Check for empty targets
		if (startDirectory == null && desiredPatterns.isEmpty() && inputStreams.isEmpty()
				&& directoriesAndFiles.isEmpty() && contentClients.isEmpty() && files.isEmpty()
				&& folders.isEmpty()) {
			throw new PatternDetectionConfigurationException(
					"No target content defined. Ensure the WorkContext contains a valid scanning target");
		}

		// Populate the context
		context.put(PatternDetectionConstants.DESIRED_PATTERNS.toString(), desiredPatterns);
		context.put(PatternDetectionConstants.INPUT_STREAMS_WRAPPERS.toString(), inputStreams);
		context.put(PatternDetectionConstants.DIRECTORY_FILE_PATHS.toString(), directoriesAndFiles);
		context.put(PatternDetectionConstants.INPUT_STREAM_CLIENTS.toString(), contentClients);
		context.put(PatternDetectionConstants.FILES_TO_SCAN.toString(), files);
		context.put(PatternDetectionConstants.FOLDERS_TO_SCAN.toString(), folders);

		// Log error for patterns without clues
		PatternDetectionWorkContextDelegate.getInstance().getDesiredPatterns(context).stream()
				.filter(pattern -> pattern.getAllAreRequiredClues().isEmpty()
						&& pattern.getOnlyOneIsRequiredClues().isEmpty())
				.forEach(pattern -> log.error(
						"Pattern {} does not have any associated Clues. Running this Scan will never result in anything being detected",
						pattern.getName()));
		// create the structures to contain detected Patterns and Clues
		initializeContext(context);
		return context;
	}

}
