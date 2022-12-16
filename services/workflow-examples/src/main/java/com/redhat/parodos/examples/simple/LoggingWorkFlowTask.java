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
package com.redhat.parodos.examples.simple;

import com.redhat.parodos.workflows.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple WorkFlowTask that writes a static log
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class LoggingWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Writing a message to the logs from: {}", getName());
		if (getGetWorkFlowChecker() != null) {
			workContext.put(WorkFlowConstants.WORKFLOW_CHECKER_ID, getGetWorkFlowChecker().getName());
		}
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

}
