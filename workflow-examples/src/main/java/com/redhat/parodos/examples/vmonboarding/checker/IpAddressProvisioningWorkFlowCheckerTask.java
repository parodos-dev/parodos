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
package com.redhat.parodos.examples.vmonboarding.checker;

import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

/**
 * An example of a task that checks for IP address in a ticket
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class IpAddressProvisioningWorkFlowCheckerTask extends BaseWorkFlowCheckerTask {

	public IpAddressProvisioningWorkFlowCheckerTask(WorkFlow serviceNowTicketFulfillmentEscalationWorkFlowTask,
			long sla) {
		super(serviceNowTicketFulfillmentEscalationWorkFlowTask, sla);
	}

	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 */
	@Override
	public WorkReport checkWorkFlowStatus(WorkContext workContext) {
		log.info("Start IpAddressProvisioningWorkFlowCheckerTask...");
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

}
