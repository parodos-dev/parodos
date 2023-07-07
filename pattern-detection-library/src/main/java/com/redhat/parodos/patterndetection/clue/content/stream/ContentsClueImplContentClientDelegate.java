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
package com.redhat.parodos.patterndetection.clue.content.stream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.redhat.parodos.patterndetection.clue.content.ContentsClueImpl;
import com.redhat.parodos.patterndetection.clue.content.ContentsClueImplDelegateBase;
import com.redhat.parodos.patterndetection.clue.content.ContentsDelegate;
import com.redhat.parodos.patterndetection.context.PatternDetectionWorkContextDelegate;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionRuntimeException;
import com.redhat.parodos.workflows.work.WorkContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Contains the logic for the ContentsClueImpl to process ContentClientConfigurations for content
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class ContentsClueImplContentClientDelegate extends ContentsClueImplDelegateBase {
	
	ContentsClueImpl clue;
	
	public ContentsClueImplContentClientDelegate(ContentsClueImpl clue) {
		super();
		this.clue = clue;
	}

	public void getContentClientConfigurations(WorkContext workContext) {
		List<ContentInputStreamClientConfiguration> contentClientsAndPaths = PatternDetectionWorkContextDelegate.getInstance()
				.getContentClientsAndPaths(workContext);
		if (contentClientsAndPaths != null) {
			contentClientsAndPaths.stream().forEach(inputStreamClient -> {
				try {
					if (clue.continueToRunIfDetected
							|| !PatternDetectionWorkContextDelegate.getInstance().isThisClueDetected(clue, workContext)) {
						processInputsForStreamContentWithClient(workContext, inputStreamClient, clue);
					}
				} catch (IOException e) {
					log.error("Unable to execute Detection using ContentStreamClient {} ", inputStreamClient.getName(),
							e);
					throw new PatternDetectionRuntimeException("Error getting content using a ContentStreamClient", e);
				}
			});
		}
		
	}

	private void processInputsForStreamContentWithClient(WorkContext workContext,
			ContentInputStreamClientConfiguration inputStreamClientConfig, ContentsClueImpl clue) throws IOException {
		for (String path : inputStreamClientConfig.getPathsToProcessForContent()) {
			if (clue.continueToRunIfDetected || !PatternDetectionWorkContextDelegate.getInstance().isThisClueDetected(clue, workContext)) {
				File file = new File(path);
				if (clue.nameMatchingDelegate.isThisATargetFileExtension(file.getName())) {
					List<String> fileContent;
					try (InputStream stream = inputStreamClientConfig.getContentClient().getContentIfRequired(path,
							inputStreamClientConfig.getParametersForClient())) {
						if (stream != null) {
							fileContent = ContentsDelegate.getInstance().inputStreamToList(stream);
							processContentsForMatch(workContext, file.getName(), fileContent, clue);
						}
					}
				}
			}
		}
		
	}

}
