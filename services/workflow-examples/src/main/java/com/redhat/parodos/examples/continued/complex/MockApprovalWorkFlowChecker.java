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
package com.redhat.parodos.examples.continued.complex;

import java.util.HashMap;
import java.util.Map;
import com.redhat.parodos.workflows.BaseWorkFlowChecker;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple example of a WorkFlowChecker that always returns true
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class MockApprovalWorkFlowChecker extends BaseWorkFlowChecker {


	public MockApprovalWorkFlowChecker(String nextWorkFlowId) {
		super(nextWorkFlowId);
	}

	@Override
	public WorkReport checkWorkFlowStatus(WorkContext context) {
		log.info("Mocking a check if the downstream conditions are met before {} can run", getNextWorkFlowId());
		context.put(WorkFlowConstants.NEXT_WORKFLOW_ID, getNextWorkFlowId());
		context.put(WorkFlowConstants.NEXT_WORKFLOW_ARGUMENTS, new HashMap<String,String>(Map.of("ARG_FROM_EXTERNAL_PROCESS", "approvalId")));
		log.info("{} can run", getNextWorkFlowId());
		return new DefaultWorkReport(WorkStatus.COMPLETED, context);
	}


}
