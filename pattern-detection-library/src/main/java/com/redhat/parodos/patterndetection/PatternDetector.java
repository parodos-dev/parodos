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

import com.redhat.parodos.patterndetection.context.PatternDetectionWorkContextDelegate;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionConfigurationException;
import com.redhat.parodos.patterndetection.results.DetectionResults;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.ParallelFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 *
 * Main entry point to the library. This runs the Scan with all arguments placed in the
 * WorkContext
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class PatternDetector {

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
	public static DetectionResults detect(WorkContext context) {
		if (PatternDetectionWorkContextDelegate.getInstance().validateAndIntializeContext(context)) {
			// @formatter:off
			WorkFlow workflow = ParallelFlow
						.Builder
						.aNewParallelFlow()
						.execute(PatternDetectionWorkContextDelegate.getInstance().getDesiredPatternsArray(context))
						.with(PatternDetectionWorkContextDelegate.getInstance().getScanningThreadPool(context) != null ? PatternDetectionWorkContextDelegate.getInstance().getScanningThreadPool(context) : ScanningThreadPool.getThreadPoolExecutor())
						.build();
			// @formatter:on
			return new DetectionResults.Builder(context, workflow).build();
		}
		else {
			throw new PatternDetectionConfigurationException(
					"The Scan for Patterns could not be started due to a misconfiguration. Please review the log files");
		}
	}

}