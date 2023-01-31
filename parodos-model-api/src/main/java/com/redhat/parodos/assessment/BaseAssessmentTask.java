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
package com.redhat.parodos.assessment;

import com.redhat.parodos.infrastructure.option.InfrastructureOption;
import com.redhat.parodos.workflows.execution.task.WorkFlowTask;

/**
 *
 * Base Class for Assessment WorkFlowTasks
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public abstract class BaseAssessmentTask implements WorkFlowTask {

	/**
	 * These are the options this AssessmentTasks can return
	 */
	InfrastructureOption infrastructureOptions;

	public BaseAssessmentTask(InfrastructureOption infrastructureOptions) {
		this.infrastructureOptions = infrastructureOptions;
	}

	public InfrastructureOption getInfrastructureOptions() {
		return infrastructureOptions;
	}
}
