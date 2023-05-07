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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.parodos.patterndetection.clue.Clue;
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
public class WorkContextDelegate {

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
		if (context.get(WorkFlowConstants.START_DIRECTORY.toString()) == null
				&& context.get(WorkFlowConstants.START_FILE.toString()) == null) {
			log.error(
					"No target destination to scan defined. Ensure the WorkContext contains an {} entry of type String, or an {} entry of type String",
					WorkFlowConstants.START_DIRECTORY.toString(), WorkFlowConstants.START_FILE.toString());
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
				.get(WorkFlowConstants.DESIRED_PATTERNS.toString())).size();
		@SuppressWarnings("unchecked")
		int numberOfTargetStatesFound = ((List<Pattern>) report.getWorkContext()
				.get(WorkFlowConstants.DETECTED_PATTERNS.toString())).size();
		return numberOfTargetStatesRequired == numberOfTargetStatesFound;
	}

	@SuppressWarnings("unchecked")
	public Set<File> getFilesToScan(WorkContext context) {
		return (Set<File>) context.get(WorkFlowConstants.FILES_TO_SCAN.toString());
	}

	public Map<Clue, List<File>> getDetectedClue(WorkReport report) {
		return getDetectedClue(report.getWorkContext());
	}

	@SuppressWarnings("unchecked")
	public Map<Clue, List<File>> getDetectedClue(WorkContext context) {
		return (Map<Clue, List<File>>) context.get(WorkFlowConstants.DETECTED_CLUES.toString());
	}

	@SuppressWarnings("unchecked")
	public List<Pattern> getDesiredPatterns(WorkContext context) {
		return (List<Pattern>) context.get(WorkFlowConstants.DESIRED_PATTERNS.toString());
	}

	public List<Pattern> getDetectedPatterns(WorkReport report) {
		return getDetectedPatterns(report.getWorkContext());
	}

	@SuppressWarnings("unchecked")
	public List<Pattern> getDetectedPatterns(WorkContext context) {
		return (List<Pattern>) context.get(WorkFlowConstants.DETECTED_PATTERNS.toString());
	}

	@SuppressWarnings("unchecked")
	public Set<File> getFoldersToScan(WorkContext context) {
		return (Set<File>) context.get(WorkFlowConstants.FOLDERS_TO_SCAN.toString());
	}

	@SuppressWarnings("unchecked")
	public boolean isThisPatternDetected(Pattern pattern, WorkReport report) {
		for (Pattern thisPattern : (List<Pattern>) report.getWorkContext()
				.get(WorkFlowConstants.DETECTED_PATTERNS.toString())) {
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
	public void markClueAsDetected(Clue clue, File file, WorkContext context) {
		if (getDetectedClue(context).containsKey(clue)) {
			getDetectedClue(context).get(clue).add(file);
		}
		else {
			List<File> detectedFiles = new ArrayList<>();
			detectedFiles.add(file);
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

	private void getFilesAndDirectoriesFromRoot(WorkContext context) {
		Set<File> fileList = new HashSet<>();
		Set<File> directoryList = new HashSet<>();
		try {
			Files.walkFileTree(Paths.get(((String) context.get(WorkFlowConstants.START_DIRECTORY.toString()))),
					new CollectFiles(fileList, directoryList));
		}
		catch (IOException e) {
			log.error("Unable to get the folders and files to process. Start Directory: {}",
					(String) context.get(WorkFlowConstants.START_DIRECTORY.toString()), e);
		}
		context.put(WorkFlowConstants.FILES_TO_SCAN.toString(), fileList);
		context.put(WorkFlowConstants.FOLDERS_TO_SCAN.toString(), directoryList);
	}

	private void initializeContext(WorkContext context) {
		context.put(WorkFlowConstants.DETECTED_CLUES.toString(), new HashMap<Clue, List<File>>());
		context.put(WorkFlowConstants.DETECTED_PATTERNS.toString(), new ArrayList<Pattern>());
	}

	/*
	 * Internal class to be used by the File Walker to collect the files and directories
	 */
	class CollectFiles extends SimpleFileVisitor<Path> {

		Set<File> fileList;

		Set<File> directoryList;

		public CollectFiles(Set<File> fileList, Set<File> directoryList) {
			super();
			this.fileList = fileList;
			this.directoryList = directoryList;
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

		public static WorkContextBuilder builder() {
			return new WorkContextBuilder();
		}

		WorkContextBuilder() {
			desiredPatterns = new ArrayList<>();
		}

		public WorkContextBuilder startDirectory(String startDirectory) {
			this.startDirectory = startDirectory;
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
			context.put(WorkFlowConstants.START_DIRECTORY.toString(), startDirectory);
			context.put(WorkFlowConstants.DESIRED_PATTERNS.toString(), desiredPatterns);
			return context;
		}

	}

}
