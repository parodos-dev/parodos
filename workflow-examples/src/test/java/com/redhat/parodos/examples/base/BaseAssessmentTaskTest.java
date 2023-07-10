package com.redhat.parodos.examples.base;

import java.util.List;

import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskType;
import org.junit.Before;

public abstract class BaseAssessmentTaskTest {

	/**
	 * These are the options this AssessmentTasks can return
	 */
	private List<WorkFlowOption> workflowOptions;

	@SuppressWarnings("unused")
	private BaseAssessmentTask baseAssessmentTask;

	@Before
	public void setUp() {
		this.baseAssessmentTask = getConcreteImplementation();
	}

	/**
	 * @return new instance of non-abstract class extending BaseInfrastructureWorkFlowTask
	 */
	protected abstract BaseAssessmentTask getConcreteImplementation();

	public List<WorkFlowOption> getWorkFlowOptions() {
		return workflowOptions;
	}

	public WorkFlowTaskType getType() {
		return WorkFlowTaskType.ASSESSMENT;
	}

}