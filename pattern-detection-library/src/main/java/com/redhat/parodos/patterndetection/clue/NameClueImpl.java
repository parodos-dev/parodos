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
import java.util.Set;
import java.util.regex.Pattern;
import com.redhat.parodos.patterndetection.clue.delegate.FileContentsDelegate;
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

	private FileContentsDelegate fileContentsDelegate = new FileContentsDelegate();

	Pattern targetFileNameRegexPattern;

	@Override
	public WorkReport execute(WorkContext workContext) {
		if (continueToRunIfDetected || !workContextDelegate.isThisClueDetected(this, workContext)) {
			Set<File> filesToScan;
			if (nameMatchingDelegate.isFolder()) {
				filesToScan = workContextDelegate.getFoldersToScan(workContext);
			}
			else {
				filesToScan = workContextDelegate.getFilesToScan(workContext);
			}
			for (File thisFile : filesToScan) {
				boolean matched = targetFileNameRegexPattern != null
						? targetFileNameRegexPattern.matcher(thisFile.getName()).matches()
						: nameMatchingDelegate.doesNameMatch(thisFile.getName());
				if (matched) {
					workContextDelegate.markClueAsDetected(this, thisFile, workContext);
				}
			}
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
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
