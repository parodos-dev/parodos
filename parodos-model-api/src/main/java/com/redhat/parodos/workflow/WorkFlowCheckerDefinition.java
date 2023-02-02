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
package com.redhat.parodos.workflow;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * WorkFlowChecker is type of WorkFlow is long running and needs to be scheduled. It is assumed that all the WorkFlowTasks in the
 * the WorkFlowChecker workflow will be executed based on the schedule.
 * 
 * To ensure the workflow-service schedules the a Workflow that does WorkflowChecker functionality, the WorkFlowDefinition associated with the Workflow needs to be
 * of this type
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
@Getter
@SuperBuilder
public class WorkFlowCheckerDefinition extends WorkFlowDefinition {
    
	private WorkFlowDefinition nextWorkFlowDefinition;
    private String cronExpression;
    
    public WorkFlowType getWorkFlowType() {
    	return WorkFlowType.CHECKER;
    }
    
}
