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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;

/**
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */	
class AbstractClueTest {

	private static final String TEST_EXTENSION = "testExtension";
	private static final String IGNORE_PATTERN = "ignorePattern";
	private static final String TEST_PATTERN = "testPattern";
	private static final String TEST_NAME = "testName";

	@Test
	void testSetName() {
		// Test setting a valid name
		String validName = TEST_NAME;
		AbstractClue clue = new AbstractClue.Builder<>().name(validName).build(new TestClue());
		assertEquals(validName, clue.name);
		
		// Test setting a null name
		AbstractClue nullNameClue = new AbstractClue.Builder<>().name(null).build(new TestClue());
		assertNotNull(nullNameClue.name);
		
		// Test setting an empty name
		AbstractClue emptyNameClue = new AbstractClue.Builder<>().name("").build(new TestClue());
		assertNotNull(emptyNameClue.name);
	}

	@Test
	void testBuilder() {
		// Test building a clue with valid parameters
		AbstractClue clue = new AbstractClue.Builder<>()
				.name(TEST_NAME)
				.continueToRunIfDetected(true)
				.targetFileNamePatternString(TEST_PATTERN)
				.ignoreFileNamePatternString(IGNORE_PATTERN)
				.targetFileExtensionPatternString(TEST_EXTENSION)
				.build(new TestClue());
		assertNotNull(clue.nameMatchingDelegate);
		assertEquals(TEST_NAME, clue.name);
		assertTrue(clue.continueToRunIfDetected);
		
		// Test building a clue with null parameters
		AbstractClue nullParamsClue = new AbstractClue.Builder<>()
				.name(null)
				.continueToRunIfDetected(false)
				.targetFileNamePatternString(null)
				.ignoreFileNamePatternString(null)
				.targetFileExtensionPatternString(null)
				.build(new TestClue());
		assertNotNull(nullParamsClue.nameMatchingDelegate);
		assertNotNull(nullParamsClue.name);
		assertFalse(nullParamsClue.continueToRunIfDetected);
	}
	
	// Define a test implementation of AbstractClue for testing purposes
	private static class TestClue extends AbstractClue {

		@Override
		public WorkReport execute(WorkContext workContext) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
