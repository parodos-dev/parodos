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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.redhat.parodos.patterndetection.context.PatternDetectionConstants;
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
	 void processFiles(WorkContext workContext) {
		//handle predefined file list
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
		Set<File> localDirectoryFiles = getFilesAndDirectoriesFromRoot(workContext);
		if (clue.continueToRunIfDetected || !PatternDetectionWorkContextDelegate.getInstance().isThisClueDetected(clue, workContext) &&  (filesToScan != null)) {
			localDirectoryFiles.stream().forEach(thisFile -> {
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
	 
	 private Set<File> getFilesAndDirectoriesFromRoot(WorkContext context) {
			Set<File> fileList = new HashSet<>();
			try {
				Files.walkFileTree(Paths.get(((String) context.get(PatternDetectionConstants.START_DIRECTORY.toString()))),
						new CollectFiles(fileList));
			}
			catch (IOException e) {
				log.error("Unable to get the folders and files to process. Start Directory: {}",
						context.get(PatternDetectionConstants.START_DIRECTORY.toString()), e);
			}
			return fileList;
		}
	 
	 /*
		 * Internal class to be used by the File Walker to collect the files
		 */
		class CollectFiles extends SimpleFileVisitor<Path> {

			Set<File> fileList;

			public CollectFiles(Set<File> fileList) {
				super();
				this.fileList = fileList;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
				fileList.add(file.toFile());
				return FileVisitResult.CONTINUE;
			}

		}

	void extractFileContent(WorkContext workContext, File thisFile) throws IOException {
		List<String> fileContent;
		if (clue.nameMatchingDelegate.isThisATargetFileExtension(thisFile.getAbsolutePath())) {
			fileContent = ContentsDelegate.getInstance().fileContentsToList(thisFile);
			processContentsForMatch(workContext, thisFile.getAbsolutePath(), fileContent, clue);
		}
		
	}
	

}
