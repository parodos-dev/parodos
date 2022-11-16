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
package com.redhat.parodos.infrastructure.option;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * An InfrastructureOption is an identifier for a collections of tools (ie: CI/CD) and environments (ie: OCP running on AWS) that can be presented to a user as an option to choose from. It
 * contains both the information to be displayed to the User to determine if they want to select the Option, and also the information to execute the Workflow
 * 
 * Identifier: unique identifier for this infrastructure option. An example could be platformname + workflowName (ie: AwsOcpAnsibleSelfHealingV1)
 * 
 * displayName: a readable/user-friendly label for the InfrastructureOption
 * 
 * description: a high level description related to the InfrastructureOption
 * 
 * details: a collection of strings that can provide optional details (i.e: internal charge back for the tooling to be stood up)
 * 
 * workFlowName: a value that corresponds to the value of Map in the registered worklfow.engine (see WorkFlowRegistry)
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */

@Getter
@Setter
public class InfrastructureOption {
	
	private String identifier;
	private String displayName;
	private String description;
	private List<String> details;
	private String workFlowId;

	private InfrastructureOption(Builder builder) {
		this.description = builder.description;
		this.details = builder.details;
		this.identifier = builder.identifier;
		this.workFlowId = builder.workFlowId;
		this.displayName = builder.displayName;
	}


	public static class Builder {
		// required
		private String identifier;
		private String displayName;
		private String description;
		private List<String> details = new ArrayList<>();
		
		// required
		private String workFlowId;
		
		public Builder(String identifier, String workFlowName) {
			this.identifier = identifier;
			this.workFlowId = workFlowName;
		}
		
		public Builder setDescription(String description) {
			this.description = description;
			return this;
		}
		
		public Builder setDetails(List<String> details) {
			this.details = details;
			return this;
		}
		
		public Builder addToDetails(String detail) {
			this.details.add(detail);
			return this;
		}

		public Builder displayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public InfrastructureOption build() {
			return new InfrastructureOption(this);
		}
	}
}
