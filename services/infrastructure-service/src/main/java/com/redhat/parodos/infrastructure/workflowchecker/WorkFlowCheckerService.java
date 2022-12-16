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
package com.redhat.parodos.infrastructure.workflowchecker;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.WorkFlowEngine;
import com.redhat.parodos.workflow.WorkFlowService;
import com.redhat.parodos.workflow.execution.transaction.WorkFlowTransactionDTO;
import com.redhat.parodos.workflow.execution.transaction.WorkTransactionService;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * WorkFlow checkers follow up on @see WorkFlows that have already executed, but have initiated a external process that must complete before subsequent
 * @see Workflow can execute. The service can only execute a WorkFlowChecker by being provided a workFlowTransactionId.
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Service
@Slf4j
public class WorkFlowCheckerService implements WorkFlowService<String> {
	
	private static final String WORKFLOW_CHECKER = "WORKFLOW_CHECKER";
	private final WorkFlowDelegate workFlowDelegate;
	private final WorkTransactionService workTransactionService;
	private final WorkFlowEngine workFlowEngine;

	public WorkFlowCheckerService(WorkFlowDelegate workFlowDelegate, WorkTransactionService workTransactionService, WorkFlowEngine workFlowEngine) {
		super();
		this.workTransactionService = workTransactionService;
		this.workFlowDelegate = workFlowDelegate;
		this.workFlowEngine = workFlowEngine;
	}

	@Override
	public WorkReport execute(String workFlowTransactionId) {
		WorkFlowTransactionDTO workFlowTransaction = workTransactionService.getWorkFlowTransactionEntity(workFlowTransactionId);
		if (workFlowTransaction != null) {
			WorkFlow workFlow = workFlowDelegate.getWorkFlowById(workFlowTransaction.getWorkFlowCheckerId());
			if (workFlow != null) {
				WorkContext workContext = workFlowDelegate.getWorkContextWithParameters(workFlowTransaction);
		    	workContext.put(WorkFlowConstants.WORKFLOW_TYPE, WORKFLOW_CHECKER);
		    	WorkReport report = workFlowEngine.executeWorkFlows(workContext, workFlow);
		    	if (report != null && report.getStatus() == WorkStatus.FAILED) {
		    		log.error("The WorkFlowChecker: {} failed. Check the logs for errors coming for the Tasks in this workflow", workFlowTransactionId);
		    	}
		    	return report;
			}
		}
		log.error("Unable to execute WorkFlowChecker for {} workFlowTransactionId", workFlowTransactionId);
        return null;
	}

	@Override
	public List<WorkFlowTaskParameter> getWorkFlowParametersForWorkFlow(String id) {
		return workFlowDelegate.getWorkFlowParametersForWorkFlow(id);
	}
	
	 /**
     * Gets all the @see Workflow for InfrastructureTasks
     * 
     * @param workFlowType
     * @return
     */
    public Collection<String> getInfrastructureTaskWorkFlows(String workFlowType) {
    	return workFlowDelegate.getWorkFlowIdsByWorkFlowType(workFlowType);
    }

}
