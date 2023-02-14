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
package com.redhat.parodos.workflow.option;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * This consumed by a @see BaseAssessmentTask, updated by that task and then consumed by
 * the UI layer for the user to choose which Workflow they wish to execute
 *
 * - currentVersion: The tool chain the application is currently using (it might be none -
 * meaning tooling must be created)
 *
 * - upgradeOptions: Any available upgrade options for the existing tool chain
 *
 * - migrationOptions: Existing tooling/environments that the user can to move their
 * workload over too (ie: moving from VMs to k8)
 *
 * - newOptions: New tool chains/environments that can be created for the application
 *
 * - continuationOptions: if a workflow has started and not been completed due to missing
 * information from the user. Selecting one of these options allows them to continue
 *
 * - otherOptions: Workflows that are specific to the team (ie: adding/removing a
 * developer from a project)
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class WorkFlowOptions {

	private WorkFlowOption currentVersion;

	private List<WorkFlowOption> upgradeOptions;

	private List<WorkFlowOption> migrationOptions;

	private List<WorkFlowOption> newOptions;

	private List<WorkFlowOption> continuationOptions;

	private List<WorkFlowOption> otherOptions;

	public boolean isOptionsAvailable() {
		return !upgradeOptions.isEmpty() && !migrationOptions.isEmpty() && !newOptions.isEmpty()
				&& !continuationOptions.isEmpty() && otherOptions.isEmpty();
	}

	public boolean hasInfrastructure() {
		return currentVersion == null;
	}

	public boolean hasIncompleteWorkFlow() {
		return !continuationOptions.isEmpty();
	}

	public WorkFlowOption getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(WorkFlowOption currentVersion) {
		this.currentVersion = currentVersion;
	}

	/**
	 * Add a Continuation Options
	 *
	 */
	public List<WorkFlowOption> getContinuationOptions() {
		return continuationOptions;
	}

	public List<WorkFlowOption> addContinuationOption(WorkFlowOption continuationOption) {
		continuationOptions.add(continuationOption);
		return continuationOptions;
	}

	public void setContinuationOptions(List<WorkFlowOption> continuationOptions) {
		this.continuationOptions = continuationOptions;
	}

	/**
	 * Add an Other option to the existing other Options
	 * @param otherOption new Workflow Option to add to the other options list
	 * @return the updated reference
	 */

	public List<WorkFlowOption> getOtherOptions() {
		return otherOptions;
	}

	public List<WorkFlowOption> addOtherOption(WorkFlowOption otherOption) {
		upgradeOptions.add(otherOption);
		return upgradeOptions;
	}

	public void setOtherOptions(List<WorkFlowOption> otherOptions) {
		this.otherOptions = otherOptions;
	}

	/**
	 * Add an Upgrade option to the existing upgrade Options
	 * @param upgradeOption new InfrastructureOption to add to the upgrade option list
	 * @return the updated reference
	 */

	public List<WorkFlowOption> getUpgradeOptions() {
		return upgradeOptions;
	}

	public List<WorkFlowOption> addUpgradeOption(WorkFlowOption upgradeOption) {
		upgradeOptions.add(upgradeOption);
		return upgradeOptions;
	}

	public void setUpgradeOptions(List<WorkFlowOption> upgradeOptions) {
		this.upgradeOptions = upgradeOptions;
	}

	/**
	 * Add a Migration option to the migration option list
	 * @param migrationOption new migration option to add the migration option list
	 * @return the updated reference
	 */
	public List<WorkFlowOption> addMigrationOption(WorkFlowOption migrationOption) {
		migrationOptions.add(migrationOption);
		return migrationOptions;
	}

	public List<WorkFlowOption> getMigrationOptions() {
		return migrationOptions;
	}

	public void setMigrationOptions(List<WorkFlowOption> migrationOptions) {
		this.migrationOptions = migrationOptions;
	}

	/**
	 * Add an Infrastructure option to the new Infrastructure options list
	 * @param newOption the new option to add
	 * @return the updated reference
	 */
	public List<WorkFlowOption> addNewInfrastrutureOption(WorkFlowOption newOption) {
		newOptions.add(newOption);
		return newOptions;
	}

	public void setNewOptions(List<WorkFlowOption> newOptions) {
		this.newOptions = newOptions;
	}

	public List<WorkFlowOption> getNewOptions() {
		return newOptions;
	}

	// Should only be called by the Builder
	private WorkFlowOptions(Builder builder) {
		this.currentVersion = builder.currentVersion;
		this.migrationOptions = builder.migrationOptions;
		this.newOptions = builder.newOptions;
		this.upgradeOptions = builder.upgradeOptions;
		this.continuationOptions = builder.continuationOptions;
		this.otherOptions = builder.otherOptions;
	}

	/**
	 * Used to build a reference for an WorkFlowOptions
	 *
	 * @author Luke Shannon (Github: lshannon)
	 *
	 */
	public static class Builder {

		private WorkFlowOption currentVersion;

		private List<WorkFlowOption> upgradeOptions = new ArrayList<WorkFlowOption>();

		private List<WorkFlowOption> migrationOptions = new ArrayList<WorkFlowOption>();

		private List<WorkFlowOption> newOptions = new ArrayList<WorkFlowOption>();

		private List<WorkFlowOption> continuationOptions = new ArrayList<WorkFlowOption>();

		private List<WorkFlowOption> otherOptions = new ArrayList<WorkFlowOption>();

		public Builder() {
		}

		public Builder setCurrentInfrastructure(WorkFlowOption currentVersion) {
			this.currentVersion = currentVersion;
			return this;
		}

		public Builder addContinuationOption(WorkFlowOption continuationOption) {
			this.continuationOptions.add(continuationOption);
			return this;
		}

		public Builder addNewOption(WorkFlowOption newOption) {
			this.newOptions.add(newOption);
			return this;
		}

		public Builder addUpgradeOption(WorkFlowOption updgradeOption) {
			this.upgradeOptions.add(updgradeOption);
			return this;
		}

		public Builder addMigrationOption(WorkFlowOption migrationOption) {
			this.migrationOptions.add(migrationOption);
			return this;
		}

		public Builder addOtherOption(WorkFlowOption otherOption) {
			this.otherOptions.add(otherOption);
			return this;
		}

		public WorkFlowOptions build() {
			return new WorkFlowOptions(this);
		}

	}

}
