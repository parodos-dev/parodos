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

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * An WorkFlowOption is an identifier for a collection of @see WorkFlowTasks that can be presented to a user as an option to choose from.
 * <p>
 * WorkFlowOption contains both the information to be displayed to the User to determine if they want to select the WorkFlow, and also the information to execute the WorkFlow
 * <p>
 * <p>
 * <p>
 * workFlowId: unique identifier for this Workflow option. An example could be platformname + workflowName (ie: AwsOcpAnsibleSelfHealingV1). This should corespond to the value the workflow-service end points expect to execute a Workflow
 * <p>
 * name: a readable/user-friendly label for the WorkflowOption
 * <p>
 * description: a high level description related to the WorkflowOption
 * <p>
 * details: a collection of strings that can provide optional details (i.e: internal charge back for the tooling to be stood up)
 * <p>
 * As much of this information is already required for the @see WorkFlowDefinition, this object is leveraged to reduce having to specify the same information in two different places
 *
 * @author Luke Shannon (Github: lshannon)
 */
@Getter
@Setter
public class WorkFlowOption {
    private String identifier;
    private String displayName;
    private String description;
    private List<String> details;
    private String workFlowId;

    private WorkFlowOption(Builder builder) {
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

        public WorkFlowOption build() {
            return new WorkFlowOption(this);
        }
    }
}
