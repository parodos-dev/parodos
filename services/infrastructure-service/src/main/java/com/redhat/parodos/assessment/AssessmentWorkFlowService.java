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
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

/**
 * This class will detect all WorkFlow beans on the classpath. As Parodos uses the same WorkFlow type for multiple work streams, this class will only all those with AssessmentService.ASSESSMENT_WORKFLOW in the name
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
@Service
public class AssessmentWorkFlowService implements WorkFlowService<WorkFlowExecuteRequestDto> {
	
    private static final String ASSESSMENT = "ASSESSMENT";
	private final WorkFlowEngine workFlowEngine;
    private final WorkFlowDelegate workFlowDelegate;
    
    public AssessmentWorkFlowService(BeanWorkFlowRegistryImpl workFlowRegistry, WorkFlowEngine workFlowEngine, WorkFlowDelegate workFlowDelegate) {
    	this.workFlowDelegate = workFlowDelegate;
        this.workFlowEngine = workFlowEngine;
    }
    
    @Override
    public WorkReport execute(WorkFlowExecuteRequestDto workFlowRequestDto) {
        WorkContext context = workFlowDelegate.getWorkContextWithParameters(workFlowRequestDto);
        context.put(WorkFlowConstants.WORKFLOW_TYPE, ASSESSMENT);
        WorkFlow assessmentWorkFlow = workFlowDelegate.getWorkFlowById(workFlowRequestDto.getWorkFlowId());
        if (assessmentWorkFlow != null) {
            return executeAssessments(context, assessmentWorkFlow);
        }
        else {
            log.error("{} is not a registered Assessment Workflow. Please check your Workflow configuration", workFlowRequestDto.getWorkFlowId());
            return new DefaultWorkReport(WorkStatus.FAILED, new WorkContext());
        }
    }
    
    @Override
	public List<WorkFlowTaskParameter> getWorkFlowParametersForWorkFlow(String id) {
		return workFlowDelegate.getWorkFlowParametersForWorkFlow(id);
	}
    
    private WorkReport executeAssessments(WorkContext workContext, WorkFlow assessmentWorkFlow) {
	    WorkReport report = workFlowEngine.executeWorkFlows(workContext, assessmentWorkFlow);
	    // each Assessment (unit of work) puts the InfrastuctureOptions reference into the WorkContext using the label specified in this class
		if (report != null && report.getWorkContext().get(WorkFlowConstants.RESULTING_INFRASTRUCTURE_OPTIONS) != null) {
		    return report;
		}
		log.error("The Workflow did not successfully run. Returning an empty InfrastructureOptions options reference. Please check the logs of the indivual Work Units in the workflow");
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}
    
    /**
     * Get all the Ids of the registered WorkFlows that have the ASSESSMENT_WORKFLOW value in their Id
     */
    public Collection<String> getAssessmentWorkFlowIds() {
    	return workFlowDelegate.getWorkFlowIdsByWorkFlowType(WorkFlowConstants.ASSESSMENT_WORKFLOW);
    }
	   
    
}
