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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class WorkFlowOptionsTest {

	private final WorkFlowOption option = new WorkFlowOption.Builder("testOption", "testWorkFlowDefinition")
			.displayName("testWorkFlowDefinition").addToDetails("An awesome WorkFlow").build();

	@Test
	public void verifyInfraOptions() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().addNewOption(option).build();
		assertThat(options.getNewOptions(), hasSize(1));
		assertThat(options.getNewOptions().get(0), equalTo(option));
		assertThat(options.isOptionsAvailable(), is(false));
	}

	@Test
	public void checkOptionAvailability() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().addNewOption(option).addContinuationOption(option)
				.addMigrationOption(option).addUpgradeOption(option).build();
		assertThat(options.getContinuationOptions(), hasSize(1));
		assertThat(options.getMigrationOptions(), hasSize(1));
		assertThat(options.getNewOptions(), hasSize(1));
		assertThat(options.getUpgradeOptions(), hasSize(1));
		assertThat(options.isOptionsAvailable(), is(true));
	}

	@Test
	public void verifyContinuation() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().build();
		assertThat(options.getContinuationOptions(), hasSize(0));
		assertThat(options.hasIncompleteWorkFlow(), is(false));

		options.addContinuationOption(option);

		assertThat(options.getContinuationOptions(), hasSize(1));
		assertThat(options.hasIncompleteWorkFlow(), is(true));

	}

	@Test
	public void verifyMigration() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().build();
		assertThat(options.getMigrationOptions(), hasSize(0));

		options.addMigrationOption(option);

		assertThat(options.getMigrationOptions(), hasSize(1));
	}

	@Test
	public void verifyUpgrade() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().build();
		assertThat(options.getUpgradeOptions(), hasSize(0));

		options.addUpgradeOption(option);

		assertThat(options.getUpgradeOptions(), hasSize(1));
	}

	@Test
	public void verifyOtherOptions() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().build();
		assertThat(options.getOtherOptions(), hasSize(0));

		options.addOtherOption(option);
		assertThat(options.getOtherOptions(), hasSize(1));
	}

	@Test
	public void checkInfra() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().setCurrentInfrastructure(option).build();

		assertThat(options.hasInfrastructure(), is(false));

		options.addNewInfrastrutureOption(option);

		assertThat(options.getNewOptions(), hasSize(1));
	}

	@Test
	public void checkCurrent() {
		WorkFlowOptions options = new WorkFlowOptions.Builder().build();
		assertThat(options.getCurrentVersion(), is(nullValue()));

		options.setCurrentVersion(option);

		assertThat(options.getCurrentVersion(), is(notNullValue()));

	}

}
