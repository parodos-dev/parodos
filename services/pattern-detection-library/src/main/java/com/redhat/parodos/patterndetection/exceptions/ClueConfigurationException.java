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
package com.redhat.parodos.patterndetection.exceptions;

/**
 * 
 * Throw if a @see Clue is being built that can never be detected (ie: missing any of the basic criteria to scan for). This is to prevent @see Clue references that run without any potiential of being detected. Too many of these could affect performance
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class ClueConfigurationException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public ClueConfigurationException(String message) {
		super(message);
	}

}
