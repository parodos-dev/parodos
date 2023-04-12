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

import com.redhat.parodos.patterndetection.clue.Clue;
import com.redhat.parodos.patterndetection.clue.InputStreamWrapper;
import com.redhat.parodos.patterndetection.clue.client.ContentInputStreamClientConfiguration;
import com.redhat.parodos.patterndetection.pattern.Pattern;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;

import lombok.extern.slf4j.Slf4j;

/**
 * Contains helper methods for working with the data in the WorkContext.
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class PatternDetectionWorkContextDelegate {

	/**
	 * Checks if the WorkContext has everything to perform the Detection: -
	 * START_DIRECTORY : the root location to begin processing the files - START_FILE :
	 * the root java.util.File to begin processing the files
	 * @param context easy-flow WorkContext
	 * @return true if everything, false if required parameter is missing
	 */
	public boolean validateAndIntializeContext(WorkContext context) {
		if (context == null) {
			log.error("Context was null. Please define a WorkContext reference");
			return false;
		}
		if (context.get(PatternDetectionConstants.START_DIRECTORY.toString()) == null
				&& context.get(PatternDetectionConstants.START_FILE.toString()) == null
				&& context.get(PatternDetectionConstants.INPUT_STREAMS_WRAPPERS.toString()) == null
				&& context.get(PatternDetectionConstants.INPUT_STREAM_CLIENTS.toString()) == null
				&& context.get(PatternDetectionConstants.DIRECTORY_FILE_PATHS.toString()) == null
				&& context.get(PatternDetectionConstants.FILES_TO_SCAN.toString()) == null
				&& context.get(PatternDetectionConstants.FOLDERS_TO_SCAN.toString()) == null) {
			log.error("No target content defined. Ensure the WorkContext contains an a valid scanning target");
			return false;
		}
		for (Pattern pattern : getDesiredPatterns(context)) {
			if (pattern.getAllAreRequiredClues().isEmpty() && pattern.getOnlyOneIsRequiredClues().isEmpty()) {
				log.error(
						"Pattern {} does not have any associated Clues. Running this Scan will never result in anything being detected",
						pattern.getName());
			}
		}
		initializeContext(context);
		getFilesAndDirectoriesFromRoot(context);
		return true;
	}

	/**
	 * Compares the DETECT_PATTERNS to the DESIRED_PATTERNS to see if all were detected
	 * @param report WorkReport passed in at the start of the Scan
	 * @return true is all Patterns were detect, false is not all the Patterns were
	 * detected
	 */
	public boolean areAllPatternsDetected(WorkReport report) {
		@SuppressWarnings("unchecked")
		int numberOfTargetStatesRequired = ((List<Pattern>) report.getWorkContext()
				.get(PatternDetectionConstants.DESIRED_PATTERNS.toString())).size();
		@SuppressWarnings("unchecked")
		int numberOfTargetStatesFound = ((List<Pattern>) report.getWorkContext()
				.get(PatternDetectionConstants.DETECTED_PATTERNS.toString())).size();
		return numberOfTargetStatesRequired == numberOfTargetStatesFound;
	}

	@SuppressWarnings("unchecked")
	public List<ContentInputStreamClientConfiguration> getContentClientsAndPaths(WorkContext context) {
		return (List<ContentInputStreamClientConfiguration>) context
				.get(PatternDetectionConstants.INPUT_STREAM_CLIENTS.toString());
	}

	@SuppressWarnings("unchecked")
	public List<InputStreamWrapper> getInputStreamWrappers(WorkContext context) {
		return (List<InputStreamWrapper>) context.get(PatternDetectionConstants.INPUT_STREAMS_WRAPPERS.toString());
	}

	@SuppressWarnings("unchecked")
	public Map<String, ArrayList<String>> getDirectoriesAndFiles(WorkContext context) {
		return (Map<String, ArrayList<String>>) context.get(PatternDetectionConstants.DIRECTORY_FILE_PATHS.toString());
	}

	@SuppressWarnings("unchecked")
	public List<File> getFilesToScan(WorkContext context) {
		return (List<File>) context.get(PatternDetectionConstants.FILES_TO_SCAN.toString());
	}

	public Map<Clue, List<String>> getDetectedClue(WorkReport report) {
		return getDetectedClue(report.getWorkContext());
	}

	@SuppressWarnings("unchecked")
	public Map<Clue, List<String>> getDetectedClue(WorkContext context) {
		return (Map<Clue, List<String>>) context.get(PatternDetectionConstants.DETECTED_CLUES.toString());
	}

	@SuppressWarnings("unchecked")
	public List<Pattern> getDesiredPatterns(WorkContext context) {
		return (List<Pattern>) context.get(PatternDetectionConstants.DESIRED_PATTERNS.toString());
	}

	public Pattern[] getDesiredPatternsArray(WorkContext context) {
		return getDesiredPatterns(context).toArray(new Pattern[0]);
	}

	public List<Pattern> getDetectedPatterns(WorkReport report) {
		return getDetectedPatterns(report.getWorkContext());
	}

	@SuppressWarnings("unchecked")
	public List<Pattern> getDetectedPatterns(WorkContext context) {
		return (List<Pattern>) context.get(PatternDetectionConstants.DETECTED_PATTERNS.toString());
	}

	@SuppressWarnings("unchecked")
	public List<File> getFoldersToScan(WorkContext context) {
		return (List<File>) context.get(PatternDetectionConstants.FOLDERS_TO_SCAN.toString());
	}

	@SuppressWarnings("unchecked")
	public boolean isThisPatternDetected(Pattern pattern, WorkReport report) {
		for (Pattern thisPattern : (List<Pattern>) report.getWorkContext()
				.get(PatternDetectionConstants.DETECTED_PATTERNS.toString())) {
			if (thisPattern.getName().equals(pattern.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a specific Clue is detected
	 * @param clue Clue to check if it was detected
	 * @param context WorkContext used through out the Scan
	 * @return true if the Clue was detected, false if it was not
	 */
	public boolean isThisClueDetected(Clue clue, WorkReport report) {
		return getDetectedClue(report).containsKey(clue);
	}

	/**
	 * Checks if a specific Clue is detected
	 * @param clue Clue to check if it was detected
	 * @param context WorkContext used through out the Scan
	 * @return true if the Clue was detected, false if it was not
	 */
	public boolean isThisClueDetected(Clue clue, WorkContext context) {
		return getDetectedClue(context).containsKey(clue);
	}

	/**
	 * Updates the WorkContext DETECTED_CLUE collection with this clue
	 * @param clue Clue that has been detected
	 * @param file File that triggered the detection
	 * @param context WorkContext used during the Scan
	 */
	public void markClueAsDetected(Clue clue, String fileName, WorkContext context) {
		if (getDetectedClue(context).containsKey(clue)) {
			getDetectedClue(context).get(clue).add(fileName);
		}
		else {
			List<String> detectedFiles = new ArrayList<>();
			detectedFiles.add(fileName);
			getDetectedClue(context).put(clue, detectedFiles);
		}
	}

	/**
	 * This checks all the DESIRED_PATTERNS and compares them to the detected CLUE(S) to
	 * see if any of the DESIRED_PATTERNS could become DETECTED_PATTERNS
	 * @param context WorkContext used during the Scan
	 */
	public void processResultsAfterScan(WorkContext context) {
		for (Pattern pattern : getDesiredPatterns(context)) {
			boolean needAllConditionsRequired = !pattern.getAllAreRequiredClues().isEmpty();
			boolean needOneOfConditionsRequired = !pattern.getOnlyOneIsRequiredClues().isEmpty();
			boolean needAllConditionsAchieved = processNeedAllClue(pattern, context);
			boolean needOneOfConditionsAchieved = processNeedOneOfClues(pattern, context);
			boolean patternDetected = false;
			if (needAllConditionsRequired && needOneOfConditionsRequired) {
				patternDetected = needAllConditionsAchieved && needOneOfConditionsAchieved;
			}
			else {
				patternDetected = (needAllConditionsRequired && needAllConditionsAchieved)
						|| (needOneOfConditionsRequired && needOneOfConditionsAchieved);
			}
			if (patternDetected) {
				getDetectedPatterns(context).add(pattern);
			}
		}
	}

	/*
	 * Used when a root path is given
	 */
	private void getFilesAndDirectoriesFromRoot(WorkContext context) {
		if (context.get(PatternDetectionConstants.START_DIRECTORY.toString()) != null) {
			List<File> fileList = new ArrayList<>();
			List<File> directoryList = new ArrayList<>();
			try {
				Files.walkFileTree(Paths.get(((String) context.get(PatternDetectionConstants.START_DIRECTORY.toString()))),
						new CollectFiles(fileList, directoryList));
			}
			catch (IOException e) {
				log.error("Unable to get the folders and files to process. Start Directory: {}",
						(String) context.get(PatternDetectionConstants.START_DIRECTORY.toString()), e);
			}
			context.put(PatternDetectionConstants.FILES_TO_SCAN.toString(), fileList);
			context.put(PatternDetectionConstants.FOLDERS_TO_SCAN.toString(), directoryList);
			}
	}

	private void initializeContext(WorkContext context) {
		context.put(PatternDetectionConstants.DETECTED_CLUES.toString(), new HashMap<Clue, List<File>>());
		context.put(PatternDetectionConstants.DETECTED_PATTERNS.toString(), new ArrayList<Pattern>());
	}

	/*
	 * Internal class to be used by the File Walker to collect the files and directories
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
	 * Checks a Pattern to see if the 'One Of Required Clues' have been detected. This is
	 * done by comparing the One Of Required Clues in the Pattern and the DETECTED_CLUES
	 * in the WorkContext
	 */
	private boolean processNeedOneOfClues(Pattern pattern, WorkContext context) {
		for (Clue oneOfClue : pattern.getOnlyOneIsRequiredClues()) {
			if (isThisClueDetected((Clue) oneOfClue, context)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Checks a Pattern to see if the 'All Required Clues' have been detected. This is
	 * done by comparing the All Required Clues in the Pattern and the DETECTED_CLUES in
	 * the WorkContext
	 */
	private boolean processNeedAllClue(Pattern pattern, WorkContext context) {
		long numberToFind = pattern.getAllAreRequiredClues().size();
		long numberFound = 0;
		for (Clue clue : pattern.getAllAreRequiredClues()) {
			if (isThisClueDetected((Clue) clue, context)) {
				numberFound++;
			}
		}
		return numberToFind == numberFound;
	}

	public static class WorkContextBuilder {

		private List<Pattern> desiredPatterns;

		private String startDirectory;

		private List<ContentInputStreamClientConfiguration> contentClients;

		private Map<String, ArrayList<String>> directoriesAndFiles;

		private List<InputStreamWrapper> inputStreams;

		public static WorkContextBuilder builder() {
			return new WorkContextBuilder();
		}

		WorkContextBuilder() {
			desiredPatterns = new ArrayList<>();
			directoriesAndFiles = new HashMap<>();
			inputStreams = new ArrayList<>();
			contentClients = new ArrayList<>();
		}

		public WorkContextBuilder setDirectoriesAndFiles(Map<String, ArrayList<String>> directoriesAndFiles) {
			this.directoriesAndFiles = directoriesAndFiles;
			return this;
		}

		public WorkContextBuilder addContentInputStreamClientAndPaths(ContentInputStreamClientConfiguration client) {
			this.contentClients.add(client);
			return this;
		}

		public WorkContextBuilder addAllToContentInputStreamClientAndPaths(
				List<ContentInputStreamClientConfiguration> clients) {
			this.contentClients.addAll(clients);
			return this;
		}

		public WorkContextBuilder startDirectory(String startDirectory) {
			this.startDirectory = startDirectory;
			return this;
		}

		public WorkContextBuilder setInputStreams(List<InputStreamWrapper> inputStreams) {
			this.inputStreams = inputStreams;
			return this;
		}

		public WorkContextBuilder addThisToInputStreams(InputStreamWrapper inputStream) {
			inputStreams.add(inputStream);
			return this;
		}

		public WorkContextBuilder addThisToDesiredPatterns(Pattern pattern) {
			desiredPatterns.add(pattern);
			return this;
		}

		public WorkContextBuilder addTheseToDesiredPatterns(List<Pattern> newDesiredPatterns) {
			desiredPatterns.addAll(newDesiredPatterns);
			return this;
		}

		public WorkContext build() {
			WorkContext context = new WorkContext();
			if (startDirectory != null) {
				context.put(PatternDetectionConstants.START_DIRECTORY.toString(), startDirectory);
			}
			context.put(PatternDetectionConstants.DESIRED_PATTERNS.toString(), desiredPatterns);
			context.put(PatternDetectionConstants.INPUT_STREAMS_WRAPPERS.toString(), inputStreams);
			context.put(PatternDetectionConstants.DIRECTORY_FILE_PATHS.toString(), directoriesAndFiles);
			context.put(PatternDetectionConstants.INPUT_STREAM_CLIENTS.toString(), contentClients);
			return context;
		}

	}

}
