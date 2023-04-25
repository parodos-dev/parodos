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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.redhat.parodos.patterndetection.clue.client.ContentInputStreamClientConfiguration;
import com.redhat.parodos.patterndetection.clue.delegate.ContentsDelegate;
import com.redhat.parodos.patterndetection.context.PatternDetectionWorkContextDelegate;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionConfigurationException;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionRuntimeException;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Implementation of a Clue that scans the contents of a file/inputstream for specific
 * string values. Supports regular expressions
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class ContentsClueImpl extends AbstractClue {

	private Pattern targetContentPattern = null;

	@Override
	public WorkReport execute(WorkContext workContext) {
		if (continueToRunIfDetected || !PatternDetectionWorkContextDelegate.isThisClueDetected(this, workContext)) {
			getContentClientConfigurations(workContext);
			getInputStreamWrappers(workContext);
			getFilesToScan(workContext);
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	/*
	 * Get Files to scan (local) and process their content
	 */
	private void getFilesToScan(WorkContext workContext) {
		List<File> filesToScan = PatternDetectionWorkContextDelegate.getFilesToScan(workContext);
		if (filesToScan != null) {
			filesToScan.stream().forEach(thisFile -> {
				try {
					extractFileContent(workContext, thisFile);
				}
				catch (IOException e) {
					log.error("Unable to execute Scan of {} clue on File: {}", this.name, thisFile.getAbsolutePath(),
							e);
					throw new PatternDetectionRuntimeException("Error getting content from files on local File system",
							e);
				}
			});
		}
	}

	/*
	 * Get the InputStream wrappers from the WorkContext and process their content
	 */
	private void getInputStreamWrappers(WorkContext workContext) {
		List<InputStreamWrapper> inputStreamWrappers = PatternDetectionWorkContextDelegate
				.getInputStreamWrappers(workContext);
		inputStreamWrappers.stream().forEach(inputStreamWrapper -> {
			try {
				extractInputStreamContent(workContext, inputStreamWrapper);
			}
			catch (IOException e) {
				log.error("Unable to execute Detection of {} clue on File: {}", inputStreamWrapper.getFileName(), e);
				throw new PatternDetectionRuntimeException("Error getting content using a InputStreamWrapper", e);
			}
		});
	}

	/*
	 * Get the ContentClient Configurations from the WorkContext and get each client to
	 * obtain its content
	 */
	private void getContentClientConfigurations(WorkContext workContext) {
		List<ContentInputStreamClientConfiguration> contentClientsAndPaths = PatternDetectionWorkContextDelegate
				.getContentClientsAndPaths(workContext);
		contentClientsAndPaths.stream().forEach(inputStreamClient -> {
			try {
				processInputsForStreamContentWithClient(workContext, inputStreamClient);
			}
			catch (IOException e) {
				log.error("Unable to execute Detection using ContentStreamClient {} ", inputStreamClient.getName(), e);
				throw new PatternDetectionRuntimeException("Error getting content using a ContentStreamClient", e);
			}
		});
	}

	/*
	 * Process a list of files using the supplied ContentInputStreamClient
	 */
	private void processInputsForStreamContentWithClient(WorkContext workContext,
			ContentInputStreamClientConfiguration inputStreamClientConfig) throws IOException {
		for (String path : inputStreamClientConfig.getPathsToProcessForContent()) {
			File file = new File(path);
			if (nameMatchingDelegate.isThisATargetFileExtension(file.getName())) {
				List<String> fileContent;
				InputStream stream = inputStreamClientConfig.getContentClient().getContentIfRequired(path,
						inputStreamClientConfig.getParametersForClient());
				if (stream != null) {
					fileContent = ContentsDelegate.inputStreamToList(stream);
					processContentsForMatch(workContext, file.getName(), fileContent);
				}
			}
		}

	}

	/*
	 * Process an InputStreamWrapper for content
	 */
	private void extractInputStreamContent(WorkContext workContext, InputStreamWrapper inputStreamWrapper)
			throws IOException {
		List<String> fileContent;
		if (nameMatchingDelegate.isThisATargetFileExtension(inputStreamWrapper.getFileName())) {
			fileContent = ContentsDelegate.inputStreamToList(inputStreamWrapper.getInputStream());
			processContentsForMatch(workContext, inputStreamWrapper.getFileName(), fileContent);
		}

	}

	/*
	 * Process a file reference on the file system for content
	 */
	private void extractFileContent(WorkContext workContext, File thisFile) throws IOException {
		List<String> fileContent;
		if (nameMatchingDelegate.isThisATargetFileExtension(thisFile.getAbsolutePath())) {
			fileContent = ContentsDelegate.fileContentsToList(thisFile);
			processContentsForMatch(workContext, thisFile.getAbsolutePath(), fileContent);
		}
	}

	/*
	 * Process the List of strings obtained from a file source for the target pattern
	 */
	private void processContentsForMatch(WorkContext workContext, String fileName, List<String> fileContent) {
		for (String line : fileContent) {
			if (!line.isEmpty() && targetContentPattern.matcher(line.trim()).matches()) {
				PatternDetectionWorkContextDelegate.markClueAsDetected(this, fileName, workContext);
			}
		}
	}

	/**
	 * Provides a Builder for creating a ContentsClue and its associated members
	 *
	 * @author Luke Shannon (Github: lshannon)
	 *
	 */
	public static class Builder extends AbstractClue.Builder<ContentsClueImpl.Builder> {

		String targetContentPatternString;

		Builder() {
		}

		public Builder targetContentPatternString(String targetContentPatternString) {
			if (targetContentPatternString == null || targetContentPatternString.isEmpty()) {
				throw new PatternDetectionConfigurationException(
						"The targetContentPattern must not be blank or null for a ContentsClue");
			}
			this.targetContentPatternString = targetContentPatternString;
			return this;
		}

		public ContentsClueImpl build() {
			ContentsClueImpl instance = new ContentsClueImpl();
			try {
				instance.setTargetContentPattern(Pattern.compile(targetContentPatternString));
			}
			catch (PatternSyntaxException patternException) {
				throw new PatternDetectionConfigurationException(
						targetContentPatternString + " is not a valid pattern. " + patternException.getMessage());
			}
			return super.build(instance);
		}

		public static Builder builder() {
			return new Builder();
		}

	}

}
