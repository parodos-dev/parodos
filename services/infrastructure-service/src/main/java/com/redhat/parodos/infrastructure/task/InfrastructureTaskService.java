package com.redhat.parodos.infrastructure.task;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.redhat.parodos.infrastructure.existing.ExistingInfrastructureDto;
import com.redhat.parodos.infrastructure.existing.ExistingInfrastructureEntity;
import com.redhat.parodos.infrastructure.existing.ExistingInfrastructureService;
import com.redhat.parodos.infrastructure.existing.ExistingInfrastructureTypes;
import com.redhat.parodos.infrastructure.existing.InfrastructureTaskStatus;
import com.redhat.parodos.infrastructure.existing.TaskExecutionLog;
import com.redhat.parodos.security.SecurityUtils;
import com.redhat.parodos.workflow.BeanWorkflowRegistryImpl;
import com.redhat.parodos.workflow.WorkFlowEngine;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import lombok.extern.slf4j.Slf4j;

/**
 * Executes the InfrastructureTask workflow for a InfrastructureOption
 */
@Service
@Slf4j
public class InfrastructureTaskService implements InfrastructureTaskAware {
    
    private final ExistingInfrastructureService existingInfrastructureService;
    private final SecurityUtils securityUtil;
    private final BeanWorkflowRegistryImpl classPathWorkFlowRegistry;
    private final WorkFlowEngine workFlowEngine;

    public InfrastructureTaskService(WorkFlowEngine workFlowEngine, SecurityUtils securityUtil, BeanWorkflowRegistryImpl classPathWorkFlowRegistry, ExistingInfrastructureService existingInfrastructureService) {
        this.classPathWorkFlowRegistry = classPathWorkFlowRegistry;
        this.securityUtil = securityUtil;
        this.existingInfrastructureService = existingInfrastructureService;
        this.workFlowEngine = workFlowEngine;
    }

    /**
     * Executes an InfrastrcutureTaskWorkflow
     *
     * @param workFlowName   name of the Workflow to run
     * @param requestDetails arguments that can be passed into the InfrastructureTask tasks
     * @return ExistingInfrastructureDto containing the data from the persisted entity
     */
    public ExistingInfrastructureEntity executeInfrastructureTasks(String workFlowName, Map<String, String> requestDetails) {
        WorkFlow workflow = classPathWorkFlowRegistry.getWorkFlowById(workFlowName);
        if (workflow == null) {
            log.error("{} is not a registered InfrastructureTaskWorkFlow. Returning null", workFlowName);
            return null;
        }
        WorkReport report = executeOptionTasks(workflow, requestDetails);
        if (report != null && report.getStatus() == WorkStatus.COMPLETED) {
            return existingInfrastructureService.createExistingInfrastructureEntity((ExistingInfrastructureDto) report.getWorkContext().get(EXISTING_INFRASTRUCTURE_DETAILS));
        }
        log.error("Unable to persist the reference for the Existing Infrastructure. Check the logs for ExistingInfrastructureService");
        return null;
    }

	/**
	 * Executes a Workflow and returns the WorkReport (which contains the WorkContext)
	 * 
	 * @param workFlow the workflow to execute
	 * @param requestDetails map of the arguments to pass into the Task execution (will be specific to the tasks)
	 * @return the WorkReport which contains the WorkContext
	 * 
	 */
    public WorkReport executeOptionTasks(WorkFlow workFlow, Map<String, String> requestDetails) {
    	WorkContext workContext = new WorkContext();
    	workContext.put(INFRASTRUCTURE_TASK_WORKFLOW_DETAILS, requestDetails);
    	WorkReport report = workFlowEngine.executeWorkFlows(workContext, workFlow);
    	if (report != null && report.getStatus() == WorkStatus.FAILED) {
    		log.error("The Infrastructure Task workflow failed. Check the logs for errors coming for the Tasks in this workflow. Checking is there is a Rollback");
    		checkForRollBackWorkFlow(workContext);
    	}
    	ExistingInfrastructureDto existingInfraDto = createExistingInfrastructureDto(requestDetails, report);
    	report.getWorkContext().put(EXISTING_INFRASTRUCTURE_DETAILS, existingInfraDto);
        return report;
    }
    
    private WorkReport checkForRollBackWorkFlow(WorkContext workContext) {
    	if (workContext.get(ROLL_BACK_WORKFLOW_NAME) != null && !((String)workContext.get(ROLL_BACK_WORKFLOW_NAME)).isEmpty()) {
    		workFlowEngine.executeWorkFlows(workContext,classPathWorkFlowRegistry.getWorkFlowById((String)workContext.get(ROLL_BACK_WORKFLOW_NAME)));
    	}
    	log.debug("A rollback workflow could not be found the WorkContext: {}", workContext.toString());
    	return null;
    }
    
    public Collection<String> getInfraStructureTaskWorkFlows() {
    	return classPathWorkFlowRegistry.getRegisteredWorkFlowNamesByWorkType(INFRASTRUCTURE_TASK_WORKFLOW);
    }

    /*
     * Creates the DTO based on the arguments and results of the Tasks
     * 
     * @param requestDetails
     * @param report
     * @return
     */
	private ExistingInfrastructureDto createExistingInfrastructureDto(Map<String, String> requestDetails, WorkReport report) {
		ExistingInfrastructureDto dto = new ExistingInfrastructureDto();
		dto.setCreatedAt(OffsetDateTime.now());
		dto.setInfrastructureOptionDisplayName(requestDetails.get(INFRASTRUCTURE_DISPLAY_VALUE)); 
		dto.setProjectName(requestDetails.get(PROJECT_NAME));
		dto.setOnboardedBy(securityUtil.getUsername());
		dto.setStatus(InfrastructureTaskStatus.SUBMITTED);
		//TO DO - this should be passed in
		dto.setWorkflowType(ExistingInfrastructureTypes.CREATE_NEW);
		dto.getTaskLog().add(TaskExecutionLog.builder().commentDate(new Date()).comment("First comment").build());
		return dto;
	}
}
