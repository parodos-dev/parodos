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
package com.redhat.parodos.infrastructure;

import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import com.redhat.parodos.workflow.BeanWorkFlowRegistryImpl;
import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.WorkFlowEngine;
import com.redhat.parodos.workflow.WorkFlowService;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.WorkFlowExecuteRequestDto;
import com.redhat.parodos.workflows.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.ParallelFlowReport;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes an InfrastructureTask WorkFlow and other related tasks
 * 
 * @author Luke Shannon (Github: lshannon)
 */
@Service
@Slf4j
public class InfrastructureWorkFlowService implements WorkFlowService<WorkFlowExecuteRequestDto> {
	
	
    private static final String INFRASTRUCTURE = "INFRASTRUCTURE";
	private final WorkFlowEngine workFlowEngine;
    private final WorkFlowDelegate workFlowDelegate;

    public InfrastructureWorkFlowService(WorkFlowEngine workFlowEngine, BeanWorkFlowRegistryImpl classPathWorkFlowRegistry, WorkFlowDelegate workFlowDelegate) {
        this.workFlowEngine = workFlowEngine;
        this.workFlowDelegate = workFlowDelegate;
    }

    /**
     * Executes an InfrastructureTaskWorkFlow
     *
     * @param requestDetails arguments that can be passed into the InfrastructureTask tasks
     * @return ExistingInfrastructureDto containing the data from the persisted entity
     */
    @Override
    public WorkReport execute(WorkFlowExecuteRequestDto requestDetails) {
        WorkFlow workflow = workFlowDelegate.getWorkFlowById(requestDetails.getWorkFlowId());
        if (workflow == null) {
            log.error("{} is not a registered InfrastructureTaskWorkFlow. Returning null", requestDetails.getWorkFlowId());
            return new DefaultWorkReport(WorkStatus.FAILED, new WorkContext());
        }
        return executeOptionTasks(workflow, requestDetails);
    }
    
    @Override
	public List<WorkFlowTaskParameter> getWorkFlowParametersForWorkFlow(String id) {
		return workFlowDelegate.getWorkFlowParametersForWorkFlow(id);
	}
	

	/**
	 * 
	 * Executes a Workflow and returns the WorkReport (which contains the WorkContext)
	 * 
	 * @param workFlow the workflow to execute
	 * @param requestDetails map of the arguments to pass into the Task execution (will be specific to the tasks)
	 * @return the WorkReport which contains the WorkContext
	 * 
	 */
    private WorkReport executeOptionTasks(WorkFlow workFlow, WorkFlowExecuteRequestDto requestDetails) {
    	WorkContext workContext = workFlowDelegate.getWorkContextWithParameters(requestDetails);
    	workContext.put(WorkFlowConstants.WORKFLOW_TYPE, INFRASTRUCTURE);
    	WorkReport report = workFlowEngine.executeWorkFlows(workContext, workFlow);
    	//check if its a ParallelWork Flow
    	if (report instanceof ParallelFlowReport) {
    		//check all the reports
    		for (WorkReport innerReport : ((ParallelFlowReport) report).getReports()) {
    			//process each report
    			processWorkReport(workContext, innerReport);
    		}
    	} else {
    		//just process the single report
    		processWorkReport(workContext, report);
    	}
        return report;
    }

    /*
     * Check if the report failed, if it did log it
     */
	private void processWorkReport(WorkContext workContext, WorkReport report) {
		if (report != null && report.getStatus() == WorkStatus.FAILED) {
    		log.error("The Infrastructure Task workflow failed. Check the logs for errors coming for the Tasks in this workflow. Checking is there is a Rollback");
    		//if a rollback WorkFlow is configured, we will run it
    		checkForRollBackWorkFlow(workContext);
    	}
	}
    
	/*
	 * If there is a rollback workflow, run it
	 */
    private void checkForRollBackWorkFlow(WorkContext workContext) {
    	if (workContext.get(WorkFlowConstants.ROLL_BACK_WORKFLOW_NAME) != null && !((String)workContext.get(WorkFlowConstants.ROLL_BACK_WORKFLOW_NAME)).isEmpty()) {
    		workFlowEngine.executeWorkFlows(workContext,workFlowDelegate.getWorkFlowById((String)workContext.get(WorkFlowConstants.ROLL_BACK_WORKFLOW_NAME)));
    	}
    	log.debug("A rollback workflow could not be found the WorkContext: {}", workContext.toString());
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
