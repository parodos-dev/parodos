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

import org.springframework.stereotype.Service;

import com.redhat.parodos.infrastructure.option.InfrastructureOptions;
import com.redhat.parodos.workflow.BeanWorkflowRegistryImpl;
import com.redhat.parodos.workflow.WorkFlowEngine;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
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
public class AssessmentService implements AssessmentWorkFlowAware {
	
    private final WorkFlowEngine workFlowEngine;
    private final BeanWorkflowRegistryImpl workFlowRegistry;
    
    public AssessmentService(BeanWorkflowRegistryImpl workFlowRegistry, WorkFlowEngine workFlowEngine) {
        this.workFlowRegistry = workFlowRegistry;
        this.workFlowEngine = workFlowEngine;
    }
    
    public InfrastructureOptions getInfrastructureOptions(AssessmentRequestDto assessmentRequest) {
        WorkContext context = new WorkContext();
        context.put(ASSESSMENT_REQUEST, assessmentRequest);
        WorkFlow assessmentWorkFlow = workFlowRegistry.getWorkFlowById(assessmentRequest.getWorkflowName());
        if (assessmentWorkFlow != null) {
            return executeAssessments(context, assessmentWorkFlow);
        }
        else {
            log.error("{} is not a registered Assessment Workflow. Please check your Workflow configuration", assessmentRequest.getWorkflowName());
            return new InfrastructureOptions.Builder().build();
        }
    }
    
    private InfrastructureOptions executeAssessments(WorkContext workContext, WorkFlow assessmentWorkFlow) {
	    WorkReport report = workFlowEngine.executeWorkFlows(workContext, assessmentWorkFlow);
	    // each Assessment (unit of work) puts the InfrastuctureOptions reference into the WorkContext using the label specified in this class
		if (report != null && report.getWorkContext().get(AssessmentWorkFlowAware.RESULTING_INFRASTRUCTURE_OPTIONS) != null) {
		    return (InfrastructureOptions) report.getWorkContext().get(AssessmentWorkFlowAware.RESULTING_INFRASTRUCTURE_OPTIONS);
		}
		log.error("The Workflow did not successfully run. Returning an empty InfrastructureOptions options reference. Please check the logs of the indivual Work Units in the workflow");
		return new InfrastructureOptions.Builder().build();
	}

   
    
    public Collection<String> getAssessmentWorkFlowIds() {
    	return workFlowRegistry.getRegisteredWorkFlowNamesByWorkType(ASSESSMENT_WORKFLOW);
    }
}
