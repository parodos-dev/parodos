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
package com.redhat.parodos.patterndetection.clue.content;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.redhat.parodos.patterndetection.clue.AbstractClue;
import com.redhat.parodos.patterndetection.clue.AbstractClue.Builder;
import com.redhat.parodos.patterndetection.clue.content.stream.ContentsClueImpInputStreamDelegate;
import com.redhat.parodos.patterndetection.clue.content.stream.ContentsClueImplContentClientDelegate;
import com.redhat.parodos.patterndetection.context.PatternDetectionWorkContextDelegate;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionConfigurationException;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * Implementation of a Clue that scans the contents of a file/inputstream for
 * specific string values. Supports regular expressions
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContentsClueImpl extends AbstractClue {

	Pattern targetPattern;
	ContentsClueImplFileDelegate contentsClueImplFileDelegate;
	ContentsClueImplContentClientDelegate contentsClueImplContentClientDelegate;
	ContentsClueImpInputStreamDelegate contentsClueImpInputStreamDelegate;
	
	public ContentsClueImpl() {
		super();
		contentsClueImplFileDelegate = new ContentsClueImplFileDelegate(this);
		contentsClueImplContentClientDelegate = new ContentsClueImplContentClientDelegate(this);
		contentsClueImpInputStreamDelegate = new ContentsClueImpInputStreamDelegate(this);
	}
	
	@Override
	public WorkReport execute(WorkContext workContext) {
		if (continueToRunIfDetected || !PatternDetectionWorkContextDelegate.getInstance().isThisClueDetected(this, workContext)) {
			processContent(workContext);
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	void processContent(WorkContext workContext) {
		contentsClueImplContentClientDelegate.getContentClientConfigurations(workContext);
		contentsClueImpInputStreamDelegate.getInputStreamWrappers(workContext);
		contentsClueImplFileDelegate.processFiles(workContext);
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
				instance.setTargetPattern(Pattern.compile(targetContentPatternString));
			} catch (PatternSyntaxException patternException) {
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
