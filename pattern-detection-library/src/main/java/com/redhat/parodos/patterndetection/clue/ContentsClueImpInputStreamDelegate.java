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

import java.io.IOException;
import java.util.List;

import com.redhat.parodos.patterndetection.clue.delegate.ContentsDelegate;
import com.redhat.parodos.patterndetection.context.PatternDetectionWorkContextDelegate;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionRuntimeException;
import com.redhat.parodos.workflows.work.WorkContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Contains the logic for the ContentsClueImpl to process InputStreams
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class ContentsClueImpInputStreamDelegate extends ContentsClueImplDelegateBase {
	
	ContentsClueImpl clue;

	public ContentsClueImpInputStreamDelegate(ContentsClueImpl clue) {
		super();
		this.clue = clue;
	}

	/*
	 * Get the InputStream wrappers from the WorkContext and process their content
	 */
	public void getInputStreamWrappers(WorkContext workContext) {
		List<InputStreamWrapper> inputStreamWrappers = PatternDetectionWorkContextDelegate.getInstance()
				.getInputStreamWrappers(workContext);
		if (inputStreamWrappers != null) {
			inputStreamWrappers.stream().forEach(inputStreamWrapper -> {
				try {
					if (clue.continueToRunIfDetected
							|| !PatternDetectionWorkContextDelegate.getInstance().isThisClueDetected(clue, workContext)) {
						extractInputStreamContent(workContext, inputStreamWrapper);
					}
				} catch (IOException e) {
					log.error("Unable to execute Detection of {} clue on File: {}", inputStreamWrapper.getFileName(),
							e);
					throw new PatternDetectionRuntimeException("Error getting content using a InputStreamWrapper", e);
				}
			});
		}
	}
	
	/*
	 * Process an InputStreamWrapper for content
	 */
	private void extractInputStreamContent(WorkContext workContext, InputStreamWrapper inputStreamWrapper)
			throws IOException {
		List<String> fileContent;
		if (clue.nameMatchingDelegate.isThisATargetFileExtension(inputStreamWrapper.getFileName())) {
			fileContent = ContentsDelegate.inputStreamToList(inputStreamWrapper.getInputStream());
			processContentsForMatch(workContext, inputStreamWrapper.getFileName(), fileContent, clue);
		}

	}

}
