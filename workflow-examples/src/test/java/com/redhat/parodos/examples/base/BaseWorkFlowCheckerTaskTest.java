package com.redhat.parodos.examples.base;

import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskType;
import org.junit.Before;

import java.util.List;

public abstract class BaseWorkFlowCheckerTaskTest {

	/**
	 * These are the options this AssessmentTasks can return
	 */
	private List<WorkFlowOption> workflowOptions;

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

}