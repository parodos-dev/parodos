package com.redhat.parodos.patterndetection.clue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.redhat.parodos.patterndetection.clue.delegate.ContentsDelegate;
import com.redhat.parodos.patterndetection.context.PatternDetectionWorkContextDelegate;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionRuntimeException;
import com.redhat.parodos.workflows.work.WorkContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContentsClueImplDelegate {
	
	/*
	 * Get Files to scan (local) and process their content
	 */
	void getFilesToScan(WorkContext workContext, ContentsClueImpl clue) {
		List<File> filesToScan = PatternDetectionWorkContextDelegate.getFilesToScan(workContext);
		if (clue.continueToRunIfDetected || !PatternDetectionWorkContextDelegate.isThisClueDetected(clue, workContext) &&  (filesToScan != null)) {
				filesToScan.stream().forEach(thisFile -> {
					try {
						extractFileContent(workContext, thisFile, clue);
					} catch (IOException e) {
						log.error("Unable to execute Scan of {} clue on File: {}", clue.name,
								thisFile.getAbsolutePath(), e);
						throw new PatternDetectionRuntimeException(
								"Error getting content from files on local File system", e);
					}
				});
			
		}
	}

	private void extractFileContent(WorkContext workContext, File thisFile, ContentsClueImpl clue) throws IOException {
		List<String> fileContent;
		if (clue.nameMatchingDelegate.isThisATargetFileExtension(thisFile.getAbsolutePath())) {
			fileContent = ContentsDelegate.fileContentsToList(thisFile);
			processContentsForMatch(workContext, thisFile.getAbsolutePath(), fileContent, clue);
		}
		
	}
	
	private void processContentsForMatch(WorkContext workContext, String fileName, List<String> fileContent, ContentsClueImpl clue) {
		for (String line : fileContent) {
			if (!line.isEmpty() && clue.targetPattern.matcher(line.trim()).matches() && clue.continueToRunIfDetected
					|| !PatternDetectionWorkContextDelegate.isThisClueDetected(clue, workContext)) {
				PatternDetectionWorkContextDelegate.markClueAsDetected(clue, fileName, workContext);
			}
		}
	}
	

}
