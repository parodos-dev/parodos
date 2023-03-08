package com.redhat.parodos.examples.base;

import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.task.WorkFlowTaskType;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.junit.Before;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.when;

public abstract class BaseAssessmentTaskTest {

	/**
	 * These are the options this AssessmentTasks can return
	 */
	private List<WorkFlowOption> workflowOptions;

	private BaseAssessmentTask baseAssessmentTask;

	@Before
	public void setUp() {
		this.baseAssessmentTask = getConcretePersonImplementation();
	}

	/**
	 * @return new instance of non-abstract class extending BaseInfrastructureWorkFlowTask
	 */
	protected abstract BaseAssessmentTask getConcretePersonImplementation();

	public List<WorkFlowOption> getWorkFlowOptions() {
		return workflowOptions;
	}

	public WorkFlowTaskType getType() {
		return WorkFlowTaskType.ASSESSMENT;
	}

}