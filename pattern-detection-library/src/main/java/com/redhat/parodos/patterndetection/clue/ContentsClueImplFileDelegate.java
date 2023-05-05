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

import com.redhat.parodos.patterndetection.clue.delegate.ContentsDelegate;
import com.redhat.parodos.patterndetection.context.PatternDetectionWorkContextDelegate;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionRuntimeException;
import com.redhat.parodos.workflows.work.WorkContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Contains the logic for the ContentsClueImpl to process File content
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class ContentsClueImplFileDelegate extends ContentsClueImplDelegateBase {
	
	ContentsClueImpl clue;
	
	public ContentsClueImplFileDelegate(ContentsClueImpl clue) {
		this.clue = clue;
	}
	
	/*
	 * Get Files to scan (local) and process their content
	 */
	 void getFilesToScan(WorkContext workContext) {
		List<File> filesToScan = PatternDetectionWorkContextDelegate.getInstance().getFilesToScan(workContext);
		if (clue.continueToRunIfDetected || !PatternDetectionWorkContextDelegate.getInstance().isThisClueDetected(clue, workContext) &&  (filesToScan != null)) {
				filesToScan.stream().forEach(thisFile -> {
					try {
						extractFileContent(workContext, thisFile);
					} catch (IOException e) {
						log.error("Unable to execute Scan of {} clue on File: {}", clue.name,
								thisFile.getAbsolutePath(), e);
						throw new PatternDetectionRuntimeException(
								"Error getting content from files on local File system", e);
					}
				});
			
		}
	}

	void extractFileContent(WorkContext workContext, File thisFile) throws IOException {
		List<String> fileContent;
		if (clue.nameMatchingDelegate.isThisATargetFileExtension(thisFile.getAbsolutePath())) {
			fileContent = ContentsDelegate.fileContentsToList(thisFile);
			processContentsForMatch(workContext, thisFile.getAbsolutePath(), fileContent, clue);
		}
		
	}
	

}
