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

import java.util.UUID;
import com.redhat.parodos.patterndetection.clue.delegate.NameMatchingDelegate;
import com.redhat.parodos.patterndetection.context.WorkContextDelegate;
import lombok.Data;

/**
 *
 * AbstractClue contains the @see WorkContextDelegate, @see NameMatchingDelegate, Name and
 * continueToRunIfDetected which are also common to all @see Clue references.
 *
 * The AbstractClue contains a builder class that can be extended by any extending class
 * of AbstractClue (@see FileContentsClue for an example). Builder in AbstractClue
 * contains code to simplify the creation of common elements (ie: name,
 * continueToRunIfDetectect, nameMatchingDelegate), this allows for the extending class to
 * only have worry about adding Builder logic for the elements specific to their Clue
 * implementation.
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Data
public abstract class AbstractClue implements Clue {

	WorkContextDelegate workContextDelegate = new WorkContextDelegate();

	NameMatchingDelegate nameMatchingDelegate;

	String name;

	boolean continueToRunIfDetected;

	void setName(String name) {
		if (name == null || name.isEmpty()) {
			name = UUID.randomUUID().toString();
		}
		this.name = name;
	}

	public static class Builder<T extends Builder<T>> {

		String name;

		boolean continueToRunIfDetected;

		String targetFileNamePatternString;

		String targetFileExtensionPatternString;

		String ignoreFileNamePatternString;

		@SuppressWarnings("unchecked")
		public T targetFileNamePatternString(String targetFileNamePatternString) {
			this.targetFileNamePatternString = targetFileNamePatternString;
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		public T ignoreFileNamePatternString(String ignoreFileNamePatternString) {
			this.ignoreFileNamePatternString = ignoreFileNamePatternString;
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		public T targetFileExtensionPatternString(String targetFileExtensionPatternString) {
			this.targetFileExtensionPatternString = targetFileExtensionPatternString;
			return (T) this;
		}

		public NameMatchingDelegate getNameMatchingDelegate() {
			return NameMatchingDelegate.Builder.aNewNameMatchingDelegate()
					.ignoreFileNamePattern(ignoreFileNamePatternString)
					.targetFileExtensionPattern(targetFileExtensionPatternString)
					.targetFileNamePattern(targetFileNamePatternString).build();
		}

		@SuppressWarnings("unchecked")
		public T name(String name) {
			this.name = name;
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		public T continueToRunIfDetected(boolean continueToRunIfDetected) {
			this.continueToRunIfDetected = continueToRunIfDetected;
			return (T) this;
		}

		public <S extends AbstractClue> S build(S instance) {
			instance.setName(name);
			instance.setNameMatchingDelegate(getNameMatchingDelegate());
			instance.setContinueToRunIfDetected(continueToRunIfDetected);
			return (S) instance;
		}

	}

}
