/*
 * Copyright (c) 2023 Red Hat Developer
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
package com.redhat.parodos.workflow.option;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WorkFlowOptionsTest {

	private final WorkFlowOption option = new WorkFlowOption.Builder("testOption", "testWorkFlowDefinition")
			.displayName("testWorkFlowDefinition").addToDetails("An awesome WorkFlow").build();

	@Test
	public void verifyInfraOptions() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().addNewOption(option).build();

		assertEquals(1, options.getNewOptions().size());
		assertEquals(option, options.getNewOptions().get(0));
		assertFalse(options.isOptionsAvailable());
	}

	@Test
	public void checkOptionAvailability() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().addNewOption(option).addContinuationOption(option)
				.addMigrationOption(option).addUpgradeOption(option).build();

		assertEquals(1, options.getContinuationOptions().size());
		assertEquals(1, options.getMigrationOptions().size());
		assertEquals(1, options.getNewOptions().size());
		assertEquals(1, options.getUpgradeOptions().size());
		assertTrue(options.isOptionsAvailable());
	}

	@Test
	public void verifyContinuation() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().build();

		assertEquals(0, options.getContinuationOptions().size());
		assertFalse(options.hasIncompleteWorkFlow());

		options.addContinuationOption(option);

		assertEquals(1, options.getContinuationOptions().size());
		assertTrue(options.hasIncompleteWorkFlow());
	}

	@Test
	public void verifyMigration() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().build();

		assertEquals(0, options.getMigrationOptions().size());

		options.addMigrationOption(option);

		assertEquals(1, options.getMigrationOptions().size());
	}

	@Test
	public void verifyUpgrade() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().build();

		assertEquals(0, options.getUpgradeOptions().size());

		options.addUpgradeOption(option);

		assertEquals(1, options.getUpgradeOptions().size());
	}

	@Test
	public void verifyOtherOptions() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().build();

		assertEquals(0, options.getOtherOptions().size());

		options.addOtherOption(option);

		assertEquals(1, options.getOtherOptions().size());
	}

	@Test
	public void checkInfra() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().setCurrentInfrastructure(option).build();

		assertFalse(options.hasInfrastructure());

		options.addNewInfrastrutureOption(option);

		assertEquals(1, options.getNewOptions().size());
	}

	@Test
	public void checkCurrent() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().build();

		assertNull(options.getCurrentVersion());

		options.setCurrentVersion(option);

		assertNotNull(options.getCurrentVersion());
	}

}
