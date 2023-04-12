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
package com.redhat.parodos.patterndetection.clue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.redhat.parodos.patterndetection.clue.client.ContentInputStreamClientConfiguration;
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
		if (continueToRunIfDetected || !workContextDelegate.isThisClueDetected(this, workContext)) {
			List<File> filesToScan = collectFileAndFolderNames(workContext);
			if (filesToScan != null) {
			for (File thisFile : filesToScan) {
				boolean matched = targetFileNameRegexPattern != null
						? targetFileNameRegexPattern.matcher(thisFile.getName()).matches()
						: nameMatchingDelegate.doesNameMatch(thisFile.getName());
				if (matched) {
					workContextDelegate.markClueAsDetected(this, thisFile.getAbsolutePath(), workContext);
				}
			}
			}
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private List<File> collectFileAndFolderNames(WorkContext workContext) {
		List<File> filesToScan = new ArrayList<>();
		// get all the folders
		if (nameMatchingDelegate.isFolder()) {
			filesToScan = workContextDelegate.getFoldersToScan(workContext);
		}
		else {
			// get any lists of files supplied
			if (workContextDelegate.getFilesToScan(workContext) != null) {
				filesToScan.addAll(workContextDelegate.getFilesToScan(workContext));
			}
			// get the files names from the ContentInputStreams
			for (ContentInputStreamClientConfiguration clientConfig : workContextDelegate
					.getContentClientsAndPaths(workContext)) {
				for (String path : clientConfig.getPathsToProcessForContent()) {
					filesToScan.add(new File(path));
				}
			}
			// Get the File names from the InputStreamWrappers
			for (InputStreamWrapper wrapper : workContextDelegate.getInputStreamWrappers(workContext)) {
				filesToScan.add(new File(wrapper.getFileName()));
			}
			// Get the Folder and file names from the directoriesAndFiles
			Map<String, ArrayList<String>> directoriesAndFiles = workContextDelegate
					.getDirectoriesAndFiles(workContext);
			for (Map.Entry<String, ArrayList<String>> entry : directoriesAndFiles.entrySet()) {
				String directory = entry.getKey();
				filesToScan.add(new File(directory));
				filesToScan.addAll(Collections.unmodifiableList(entry.getValue()).stream().map(File::new)
						.collect(Collectors.toList()));
			}
		}
		return filesToScan;
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
