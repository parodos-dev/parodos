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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.redhat.parodos.common.patterndetection.ScanningThreadPool;
import com.redhat.parodos.common.patterndetection.clue.Clue;
import com.redhat.parodos.patterndetection.exceptions.PatternDetectionConfigurationException;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.workflow.ParallelFlow;

/**
 * 
 * An implementation of @see Pattern that will serve most Pattern Detection needs
 * 
 * allAreRequiredClues - every @see Clue added to this list needs to be detected for the @see Pattern to be considered detected
 * onlyOneIsRequiredClues - only one @see Clue from this list needs to be detected for the @see Pattern to be considered detected
 * 
 * If both onlyOneIsRequiredClues and allAreRequiredClues are configured, then all of the @Clue in allAreRequiredClues must be detected AND one of the @see Clue in
 * the onlyOneIsRequiredClues must be detected 
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class BasicPatternImpl implements Pattern {

	private final List<Clue> allAreRequiredClues;

	private final List<Clue> onlyOneIsRequiredClues;

	BasicPatternImpl(List<Clue> allAreRequiredClues, List<Clue> onlyOneIsRequiredClues) {
		this.allAreRequiredClues = allAreRequiredClues;
		this.onlyOneIsRequiredClues = onlyOneIsRequiredClues;
	}

	/**
	 * Executes all the Clues in the DESIRED_PATTERNS set in the WorkContext
	 * 
	 */
	@Override
	public WorkReport execute(WorkContext workContext) {
		return ParallelFlow.Builder.aNewParallelFlow()
				.execute(combineAllClues())
				.with(ScanningThreadPool.getThreadPoolExecutor())
				.build()
				.execute(workContext);
	}


	/*
	 * Combine the allAreRequiredClues and onlyOneIsRequired into a single list
	 */
	private Work[] combineAllClues() {
		List<Clue> allClues = new ArrayList<>();
		allClues.addAll(allAreRequiredClues);
		allClues.addAll(onlyOneIsRequiredClues);
		return allClues.stream().toArray(Work[] ::new);
	}

	@Override
	public List<Clue> getAllAreRequiredClues() {
		return allAreRequiredClues;
	}

	@Override
	public List<Clue> getOnlyOneIsRequiredClues() {
		return onlyOneIsRequiredClues;
	}

	public static class Builder {

		private Builder() {
		}

		public static PatternImplBuilder aNewPattern() {
			return new PatternImplBuilder();
		}

		public static class PatternImplBuilder {

			private List<Clue> allAreRequiredClues = new ArrayList<>();

			private List<Clue> onlyOneIsRequiredClues = new ArrayList<>();

			public PatternImplBuilder addThiToOneIsRequiredClues(Clue clue) {
				return addTheseToOneOfTheseClues(Collections.singletonList(clue));
			}

			public PatternImplBuilder addTheseToOneOfTheseClues(List<Clue> conditions) {
				if (conditions == null) {
					throw new PatternDetectionConfigurationException("OnOfTheseClues list cannot be null");
				}
				if (!conditions.isEmpty()) {
					onlyOneIsRequiredClues.addAll(conditions);
				}
				return this;
			}

			public PatternImplBuilder addThisToAllAreRequiredClues(Clue clue) {
				return addTheseToAllCluesRequired(Collections.singletonList(clue));
			}

			public PatternImplBuilder addTheseToAllCluesRequired(List<Clue> clues) {
				if (clues == null) {
					throw new PatternDetectionConfigurationException("AllRequiredClues list cannot be null");
				}
				if (!clues.isEmpty()) {
					allAreRequiredClues.addAll(clues);
				}
				return this;
			}

			PatternImplBuilder() {
			}

			public BasicPatternImpl build() {
				return new BasicPatternImpl(allAreRequiredClues, onlyOneIsRequiredClues);
			}
		}

	}

}
