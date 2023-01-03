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
package com.redhat.parodos.workflows;

import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.Getter;

/**
 * Base Class for an InfrastrcutureWorkFlowTask.
 * 
 * If the infrastructure WorkTask ends with a long running task outside of Parodos (i.e: waiting for ticket approval), a @see WorkFlowChecker can be specified with the
 * logic required to check the status of this external tasks
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public abstract class BaseInfrastructureWorkFlowTask implements WorkFlowTask {
	
	//WorkFlowChecker check a process that has been initiated by a WorkFlow to see if its been completed
	private WorkFlow workFlowChecker;

	public WorkFlow getGetWorkFlowChecker() {
		return workFlowChecker;
	}
	
	public void setWorkFlowChecker(WorkFlow gateTwo) {
		this.workFlowChecker = gateTwo;
	}

	@Getter
	private final String name;

	public BaseInfrastructureWorkFlowTask(String name) {
		this.name = name;
	}
	
}
