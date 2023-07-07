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
package com.redhat.parodos.patterndetection.clue.name;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.redhat.parodos.patterndetection.clue.content.stream.ContentInputStreamClientConfiguration;
import com.redhat.parodos.patterndetection.clue.content.stream.InputStreamWrapper;
import com.redhat.parodos.patterndetection.context.PatternDetectionWorkContextDelegate;
import com.redhat.parodos.patterndetection.exceptions.ClueConfigurationException;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Implementation of a Clue that scans the names of Files/Folder for text patterns.
 * Supports Regular Expression
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NameClueImpl extends AbstractClue {

	Pattern targetFileNameRegexPattern;

	@Override
	public WorkReport execute(WorkContext workContext) {
		if (continueToRunIfDetected || !PatternDetectionWorkContextDelegate.getInstance().isThisClueDetected(this, workContext)) {
			List<File> filesToScan = collectFileAndFolderNames(workContext);
			if (filesToScan != null) {
				for (File thisFile : filesToScan) {
					boolean matched = targetFileNameRegexPattern != null
							? targetFileNameRegexPattern.matcher(thisFile.getName()).matches()
							: nameMatchingDelegate.doesNameMatch(thisFile.getName());
					if (matched) {
						PatternDetectionWorkContextDelegate.getInstance().markClueAsDetected(this, thisFile.getAbsolutePath(),
								workContext);
					}
				}
			}
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private List<File> collectFileAndFolderNames(WorkContext workContext) {
		List<File> filesToScan = nameMatchingDelegate.isFolder()
				? PatternDetectionWorkContextDelegate.getInstance().getFoldersToScan(workContext) : new ArrayList<>();
		collectFilesFromAllSources(workContext, filesToScan);
		return filesToScan;
	}

	private void collectFilesFromAllSources(WorkContext workContext, List<File> filesToScan) {
		getFileAndFolderNamesFromLocalFileReference(workContext, filesToScan);
		getFileAndFolderNamesFromClients(workContext, filesToScan);
		getFileAndFolderNamesFromStreamWrappers(workContext, filesToScan);
		getFileAndFoldersFromContextDirectoryList(workContext, filesToScan);
	}

	private void getFileAndFolderNamesFromLocalFileReference(WorkContext workContext, List<File> filesToScan) {
		if (PatternDetectionWorkContextDelegate.getInstance().getFilesToScan(workContext) != null) {
			filesToScan.addAll(PatternDetectionWorkContextDelegate.getInstance().getFilesToScan(workContext));
		}
	}

	private void getFileAndFoldersFromContextDirectoryList(WorkContext workContext, List<File> filesToScan) {
		Map<String, ArrayList<String>> directoriesAndFiles = PatternDetectionWorkContextDelegate.getInstance().getDirectoriesAndFiles(workContext);
		for (var entry : directoriesAndFiles.entrySet()) {
			String directory = entry.getKey();
			filesToScan.add(new File(directory));
			filesToScan.addAll(entry.getValue().stream().map(fileName -> new File(directory, fileName))
					.collect(Collectors.toList()));
		}
	}

	private void getFileAndFolderNamesFromStreamWrappers(WorkContext workContext, List<File> filesToScan) {
		for (InputStreamWrapper wrapper : PatternDetectionWorkContextDelegate.getInstance().getInputStreamWrappers(workContext)) {
			filesToScan.add(new File(wrapper.getFileName()));
		}
	}

	private void getFileAndFolderNamesFromClients(WorkContext workContext, List<File> filesToScan) {
		// get the files names from the ContentInputStreams
		for (ContentInputStreamClientConfiguration clientConfig : PatternDetectionWorkContextDelegate
				.getInstance().getContentClientsAndPaths(workContext)) {
			for (String path : clientConfig.getPathsToProcessForContent()) {
				filesToScan.add(new File(path));
			}
		}
	}

	public static class Builder extends AbstractClue.Builder<NameClueImpl.Builder> {

		private String fileNamePatternString;

		Builder() {
		}

		@Override
		public Builder targetFileNamePatternString(String fileNamePatternString) {
			this.fileNamePatternString = fileNamePatternString;
			return this;
		}

		public NameClueImpl build() {
			NameClueImpl instance = super.build(new NameClueImpl());
			if (fileNamePatternString != null) {
				instance.setTargetFileNameRegexPattern(Pattern.compile(fileNamePatternString));
			}
			else if (super.getNameMatchingDelegate().getTargetFileNameRegexPattern() == null) {
				throw new ClueConfigurationException("Name Clue " + instance.getName()
						+ " must contain a fileNamePatternString or a NameMatchingDelegate.targetFileNamePatternString");
			}
			return instance;
		}

		public static Builder builder() {
			return new Builder();
		}

	}

}
