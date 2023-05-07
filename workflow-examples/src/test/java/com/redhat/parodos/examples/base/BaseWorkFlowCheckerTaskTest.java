package com.redhat.parodos.examples.base;

import java.util.List;

import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskType;
import org.junit.Before;

public abstract class BaseWorkFlowCheckerTaskTest {

	/**
	 * These are the options this AssessmentTasks can return
	 */
	private List<WorkFlowOption> workflowOptions;

	@SuppressWarnings("unused")
	private BaseWorkFlowCheckerTask baseWorkFlowCheckerTask;

	@Before
	public void setUp() {
		this.baseWorkFlowCheckerTask = getConcretePersonImplementation();
	}

	/**
	 * @return new instance of non-abstract class extending BaseWorkFlowCheckerTaskTest
	 */
	protected abstract BaseWorkFlowCheckerTask getConcretePersonImplementation();

	public WorkFlowTaskType getType() {
		return WorkFlowTaskType.CHECKER;
	}

	public List<WorkFlowOption> getWorkflowOptions() {
		return workflowOptions;
	}

	public void setWorkflowOptions(List<WorkFlowOption> workflowOptions) {
		this.workflowOptions = workflowOptions;
	}

}