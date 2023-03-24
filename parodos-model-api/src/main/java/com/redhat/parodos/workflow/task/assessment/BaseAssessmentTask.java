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
package com.redhat.parodos.workflow.task.assessment;

import java.util.List;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskType;

/**
 * Base Class for Assessment WorkFlowTasks. An implementation of this class would do an
 * Assessment and return options
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardW98)
 */
public abstract class BaseAssessmentTask extends BaseWorkFlowTask {

	private WorkFlowTaskType type = WorkFlowTaskType.ASSESSMENT;

	/**
	 * These are the options this AssessmentTasks can return
	 */
	private List<WorkFlowOption> workflowOptions;

	protected BaseAssessmentTask(List<WorkFlowOption> workflowOptions) {
		this.workflowOptions = workflowOptions;
	}

	public List<WorkFlowOption> getWorkFlowOptions() {
		return workflowOptions;
	}

	public WorkFlowTaskType getType() {
		return type;
	}

}
