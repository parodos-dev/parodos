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
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.redhat.parodos.patterndetection.clue.delegate.FileContentsDelegate;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionConfigurationException;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Implementation of a Clue that scans the contents of a file for specific strings.
 * Supports regular expressions
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class ContentsClueImpl extends AbstractClue {

	private Pattern targetContentPattern = null;

	private FileContentsDelegate fileContentsDelegate = new FileContentsDelegate();

	@Override
	public WorkReport execute(WorkContext workContext) {
		if (continueToRunIfDetected || !workContextDelegate.isThisClueDetected(this, workContext)) {
			for (File thisFile : workContextDelegate.getFilesToScan(workContext)) {
				try {
					processFileForContentMatches(workContext, thisFile);
				}
				catch (IOException e) {
					log.error("Unable to execute Scan of {} clue on File: {}", this.name, thisFile.getAbsolutePath(),
							e);
					return new DefaultWorkReport(WorkStatus.FAILED, workContext);
				}
			}
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private void processFileForContentMatches(WorkContext workContext, File thisFile) throws IOException {
		List<String> fileContent;
		if (nameMatchingDelegate.isThisATargetFileExtension(thisFile)) {
			fileContent = fileContentsDelegate.fileContentsToList(thisFile);
			processFileContentsForContentMatch(workContext, thisFile, fileContent);
		}
	}

	private void processFileContentsForContentMatch(WorkContext workContext, File thisFile, List<String> fileContent) {
		for (String line : fileContent) {
			if (!line.isEmpty() && targetContentPattern.matcher(line.trim()).matches()) {
				workContextDelegate.markClueAsDetected(this, thisFile, workContext);
			}
		}
	}

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
