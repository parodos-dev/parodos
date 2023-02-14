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
package com.redhat.parodos.workflow.task.checker;

import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflow.task.WorkFlowTaskType;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;

/**
 * Basic Contract for checking if a manual process initiated by a @see WorkFlowTask has
 * been completed
 *
 * @author Luke Shannon (Github: lshannon)
 */
public abstract class BaseWorkFlowCheckerTask extends BaseWorkFlowTask {

	private WorkFlowTaskType type = WorkFlowTaskType.CHECKER;

	/**
	 * Method to check if a WorkFlow that is in a holding status, i.e: waiting for an
	 * external process to occur, has achieved its status and can trigger the next
	 * WorkFlow
	 * @param context
	 * @return
	 */
	protected abstract WorkReport checkWorkFlowStatus(WorkContext context);

	/**
	 * By default, if no execute method is defined, the checkWorkFlowStatus method will be
	 * executed by the WorkFlow engine
	 */
	@Override
	public WorkReport execute(WorkContext workContext) {
		return checkWorkFlowStatus(workContext);
	}

	public WorkFlowTaskType getType() {
		return type;
	}

}
