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
package com.redhat.parodos.patterndetection;

import java.util.Date;
import com.redhat.parodos.patterndetection.context.WorkContextDelegate;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionConfigurationException;
import com.redhat.parodos.patterndetection.pattern.Pattern;
import com.redhat.parodos.patterndetection.results.DetectionResults;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.workflow.ParallelFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 *
 * Main entry point to the library
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class PatternDetector {

	private static final WorkContextDelegate contextDelegate = new WorkContextDelegate();

	private PatternDetector() {
	}

	/**
	 * -
	 *
	 * Takes a @see WorkContext and a List of @Pattern reference, performs a Scan on
	 * code/configuration to determine if the @see Clue references are present to detect
	 * one of the desired @Pattern
	 * @param context contains inputs for the scan, and is passed around components of the
	 * scan to capture all outputs generated during the scan
	 * @return DetectionResults contains the detected @see Clue(s) and detected @see
	 * Pattern(s)
	 *
	 * @author Luke Shannon (Github: lshannon)
	 *
	 */
	public static DetectionResults detect(WorkContext context, Pattern[] desiredPatterns) {
		if (contextDelegate.validateAndIntializeContext(context)) {
			Date startTime = new Date();
			// put all the Patterns into a ParallelFlow - they will all execute at the
			// same time
			// @formatter:off
			WorkFlow workflow = ParallelFlow
						.Builder
						.aNewParallelFlow()
						.execute(desiredPatterns)
						.with(ScanningThreadPool.getThreadPoolExecutor())
						.build();
			// @formatter:on
			// get the end report
			WorkReport report = workflow.execute(context);
			// process the results to make more user friendly results
			contextDelegate.processResultsAfterScan(report.getWorkContext());
			// return the user friendly results
			// @formatter:off
			return DetectionResults
					.builder()
					.detectedClues(contextDelegate.getDetectedClue(report))
					.detectedPatterns(contextDelegate.getDetectedPatterns(report))
					.startTime(startTime)
					.endTime(new Date())
					.build();
			// @formatter:on
		}
		else {
			throw new PatternDetectionConfigurationException(
					"The Scan for Patterns could not be started due to a misconfiguration. Please review the log files");
		}
	}

}