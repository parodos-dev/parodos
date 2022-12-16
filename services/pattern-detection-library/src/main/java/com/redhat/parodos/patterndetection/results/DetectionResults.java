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
package com.redhat.parodos.patterndetection.results;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.redhat.parodos.common.patterndetection.clue.Clue;
import com.redhat.parodos.patterndetection.pattern.Pattern;
import lombok.Builder;
import lombok.Data;

/**
 * Wrapper for the results of the Scan:
 * 
 * - Detected @see Pattern references (meaning all required @see Clue were discovered)
 * - A list of all the detected @see Clue and the files that triggered the detection. This list is populated regardless of if @see Patterns were detected or not
 * - Time the scan started
 * - Time the scan ended
 * - allStatesWhereDetected returns true is all desired @see Pattern that were specified in the @see WorkContext were detected
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Data
@Builder
public class DetectionResults {
	
	private Map<Clue, List<File>> detectedClues;
	private List<Pattern> detectedPatterns;
	private Date startTime;
	private Date endTime;
	private boolean allPatternsWhereDetected;

}
