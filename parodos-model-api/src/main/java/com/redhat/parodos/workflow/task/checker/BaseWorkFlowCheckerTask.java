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

import java.util.Date;

import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskType;
import com.redhat.parodos.workflows.engine.WorkFlowEngineBuilder;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * Basic Contract for checking if a manual process initiated by a @see WorkFlowTask has
 * been completed
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardW98)
 */
public abstract class BaseWorkFlowCheckerTask extends BaseWorkFlowTask {

	private WorkFlowTaskType type = WorkFlowTaskType.CHECKER;

	/**
	 * A Workflow that runs when the Checking logic extends a specified SLA (i.e: run
	 * escalation is Checking exceeds 48 hrs)
	 */
	private WorkFlow escalationWorkflow;

	private long expectedCompletionDate;

	protected BaseWorkFlowCheckerTask(WorkFlow escalationWorkflow, long expectedSlaBeforeEscalationInSeconds) {
		super();
		this.expectedCompletionDate = expectedSlaBeforeEscalationInSeconds;
		this.escalationWorkflow = escalationWorkflow;
	}

	protected BaseWorkFlowCheckerTask() {
		super();
	}

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
		// run the checker
		WorkReport report = checkWorkFlowStatus(workContext);
		// determine if there is an escalation path for a failing checker
		if (escalationWorkflow != null && report.getStatus() == WorkStatus.FAILED
				&& new Date().getTime() > expectedCompletionDate) {
			// run escalation if SLA is exceeded
			return WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(escalationWorkflow, workContext);
		}
		return report;
	}

	public WorkFlowTaskType getType() {
		return type;
	}

}
