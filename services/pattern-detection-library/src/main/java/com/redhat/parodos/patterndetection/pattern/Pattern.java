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
package com.redhat.parodos.patterndetection.pattern;

import java.util.List;

import com.redhat.parodos.patterndetection.clue.Clue;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * Pattern is what the Scan is looking to Detect.
 * 
 * If is composed on AllAreRequiredClues @see Clue (all Clue in this list must be detected) and OnlyOneIsRequiredClues (only one @see Clue from this collection needs to be detected).
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public interface Pattern extends WorkFlow {
	
	List<? extends Clue> getAllAreRequiredClues();
	List<? extends Clue> getOnlyOneIsRequiredClues();

}
