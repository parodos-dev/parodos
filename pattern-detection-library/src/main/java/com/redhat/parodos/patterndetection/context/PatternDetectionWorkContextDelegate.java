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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.redhat.parodos.patterndetection.clue.Clue;
import com.redhat.parodos.patterndetection.clue.InputStreamWrapper;
import com.redhat.parodos.patterndetection.clue.client.ContentInputStreamClientConfiguration;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionConfigurationException;
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

	private static PatternDetectionWorkContextDelegate instance = null;

	private PatternDetectionWorkContextDelegate() {
	}

	public static synchronized PatternDetectionWorkContextDelegate getInstance() {
		if (instance == null) {
			instance = new PatternDetectionWorkContextDelegate();
		}
		return instance;
	}

	/**
	 * - Checks if the WorkContext has everything to perform the Detection:
	 * START_DIRECTORY : the root location to begin processing the files START_FILE
	 * : the root java.util.File to begin processing the files
	 * INPUT_STREAMS_WRAPPERS: key/value pairs of file paths/inputstream contents
	 * INPUT_STREAM_CLIENTS: a collection of @see ContentInputStreamClient
	 * implementations FILES_TO_SCAN: A collection of files on the local file system
	 * FOLDERS_TO_SCAN: A collection of folders on the local file system
	 * 
	 * @param
	 * @return true if at least one of these are present, false if none are
	 *         configured. This will return false if there are no clues or patterns
	 *         configured
	 */
	public boolean validateAndIntializeContext(WorkContext context) {
		if (context == null) {
			log.error("Context was null. Please define a WorkContext reference");
			return false;
		}
		return true;
	}

	public ExecutorService getScanningThreadPool(WorkContext context) {
		return (ExecutorService) context.get(PatternDetectionConstants.SCANNING_THREAD_POOL.toString());
	}

	/**
	 * Compares the DETECT_PATTERNS to the DESIRED_PATTERNS to see if all desired
	 * Patterns were detected
	 * 
	 * @param report WorkReport passed in at the start of the Scan
	 * @return true is all Patterns were detect, false is not all the Patterns were
	 *         detected
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

	/**
	 * Gets the list @see ContentInputStreamClientConfiguration
	 * 
	 * @param context WorkContext for the scan
	 * @return List of ContentInputStreamClientConfiguration
	 */
	@SuppressWarnings("unchecked")
	public List<ContentInputStreamClientConfiguration> getContentClientsAndPaths(WorkContext context) {
		return (List<ContentInputStreamClientConfiguration>) context
				.get(PatternDetectionConstants.INPUT_STREAM_CLIENTS.toString());
	}

	/**
	 * Gets the InputStreamWrappers from the WorkContext
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<InputStreamWrapper> getInputStreamWrappers(WorkContext context) {
		return (List<InputStreamWrapper>) context.get(PatternDetectionConstants.INPUT_STREAMS_WRAPPERS.toString());
	}

	/**
	 * Get the Directories and respective files to scan from the context
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, ArrayList<String>> getDirectoriesAndFiles(WorkContext context) {
		return (Map<String, ArrayList<String>>) context.get(PatternDetectionConstants.DIRECTORY_FILE_PATHS.toString());
	}

	/**
	 * Gets a list of files to scan from the context
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<File> getFilesToScan(WorkContext context) {
		return (List<File>) context.get(PatternDetectionConstants.FILES_TO_SCAN.toString());
	}

	/**
	 * Gets the detected Clues from the report (this should be ran after the scan is
	 * completed)
	 * 
	 * @param report
	 * @return
	 */
	public Map<Clue, List<String>> getDetectedCluesFromReport(WorkReport report) {
		return getDetectedCluesFromContext(report.getWorkContext());
	}

	/**
	 * Get the detected Clues from the Context (used during scanning)
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<Clue, List<String>> getDetectedCluesFromContext(WorkContext context) {
		return (Map<Clue, List<String>>) context.get(PatternDetectionConstants.DETECTED_CLUES.toString());
	}

	/**
	 * Get the desired Patterns from the Context
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Pattern> getDesiredPatterns(WorkContext context) {
		if (context.get(PatternDetectionConstants.DESIRED_PATTERNS.toString()) == null) {
			throw new PatternDetectionConfigurationException("A Work Context for Pattern Detection should contain Desired Patterns using the key: " + PatternDetectionConstants.DESIRED_PATTERNS.toString());
		}
		return (List<Pattern>) context.get(PatternDetectionConstants.DESIRED_PATTERNS.toString());
	}

	/**
	 * Get the desired Patterns from the Context as a Array
	 * 
	 * @param context
	 * @return
	 */
	public Pattern[] getDesiredPatternsArray(WorkContext context) {
		List<Pattern> patternsList = getDesiredPatterns(context);
	    if (patternsList == null) {
	        patternsList = new ArrayList<>();
	    }
	    return patternsList.toArray(new Pattern[0]);
	}

	/**
	 * Gets all the Pattern detected from the @see WorkReport. This can be called at
	 * the end of the scan
	 * 
	 * @param report generated at the end of the scan
	 * @return list of Patterns detected after the scan is completed
	 */
	public List<Pattern> getDetectedPatternsAfterTheScan(WorkReport report) {
		return getDetectedPatternsDuringScan(report.getWorkContext());
	}

	/**
	 * Gets the detected Patterns from the context (used during scanning)
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Pattern> getDetectedPatternsDuringScan(WorkContext context) {
		return (List<Pattern>) context.get(PatternDetectionConstants.DETECTED_PATTERNS.toString());
	}

	/**
	 * Get the Folders to scan from the context
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<File> getFoldersToScan(WorkContext context) {
		return (List<File>) context.get(PatternDetectionConstants.FOLDERS_TO_SCAN.toString());
	}

	/**
	 * Determines if a specific Pattern was detected after the scan
	 * 
	 * @param pattern in question
	 * @param report  generated after the scan has completed
	 * @return true is the Pattern is detected, false if its not
	 */
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
	 * 
	 * @param clue    Clue to check if it was detected
	 * @param context WorkContext used through out the Scan
	 * @return true if the Clue was detected, false if it was not
	 */
	public boolean isThisClueDetected(Clue clue, WorkReport report) {
		return getDetectedCluesFromReport(report).containsKey(clue);
	}

	/**
	 * Checks if a specific Clue is detected
	 * 
	 * @param clue    Clue to check if it was detected
	 * @param context WorkContext used through out the Scan
	 * @return true if the Clue was detected, false if it was not
	 */
	public boolean isThisClueDetected(Clue clue, WorkContext context) {
		return getDetectedCluesFromContext(context).containsKey(clue);
	}

	/**
	 * Updates the WorkContext DETECTED_CLUE collection with this clue
	 * 
	 * @param clue    Clue that has been detected
	 * @param file    File that triggered the detection
	 * @param context WorkContext used during the Scan
	 */
	public void markClueAsDetected(Clue clue, String fileName, WorkContext context) {
		if (getDetectedCluesFromContext(context).containsKey(clue)) {
			getDetectedCluesFromContext(context).get(clue).add(fileName);
		} else {
			List<String> detectedFiles = new ArrayList<>();
			detectedFiles.add(fileName);
			getDetectedCluesFromContext(context).put(clue, detectedFiles);
		}
	}

	/**
	 * This checks all the DESIRED_PATTERNS and compares them to the detected
	 * CLUE(S) to see if any of the DESIRED_PATTERNS could become DETECTED_PATTERNS
	 * 
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
			} else {
				patternDetected = (needAllConditionsRequired && needAllConditionsAchieved)
						|| (needOneOfConditionsRequired && needOneOfConditionsAchieved);
			}
			if (patternDetected) {
				getDetectedPatternsDuringScan(context).add(pattern);
			}
		}
	}

	
	

	
	/*
	 * Checks a Pattern to see if the 'One Of Required Clues' have been detected.
	 * This is done by comparing the One Of Required Clues in the Pattern and the
	 * DETECTED_CLUES in the WorkContext
	 */
	private boolean processNeedOneOfClues(Pattern pattern, WorkContext context) {
		for (Clue oneOfClue : pattern.getOnlyOneIsRequiredClues()) {
			if (isThisClueDetected(oneOfClue, context)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Checks a Pattern to see if the 'All Required Clues' have been detected. This
	 * is done by comparing the All Required Clues in the Pattern and the
	 * DETECTED_CLUES in the WorkContext
	 */
	private boolean processNeedAllClue(Pattern pattern, WorkContext context) {
		long numberToFind = pattern.getAllAreRequiredClues().size();
		long numberFound = 0;
		for (Clue clue : pattern.getAllAreRequiredClues()) {
			if (isThisClueDetected(clue, context)) {
				numberFound++;
			}
		}
		return numberToFind == numberFound;
	}

}
