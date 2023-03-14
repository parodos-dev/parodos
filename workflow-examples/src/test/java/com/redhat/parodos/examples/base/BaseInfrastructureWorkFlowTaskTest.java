package com.redhat.parodos.examples.base;

import com.redhat.parodos.workflow.task.enums.WorkFlowTaskType;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.junit.Before;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public abstract class BaseInfrastructureWorkFlowTaskTest {

	protected final static String workflowTestName = "TestName";

	private BaseInfrastructureWorkFlowTask baseInfrastructureWorkFlowTask;

	@Before
	public void setUp() {
		this.baseInfrastructureWorkFlowTask = getConcretePersonImplementation();
	}

	/**
	 * @return new instance of non-abstract class extending BaseInfrastructureWorkFlowTask
	 */
	protected abstract BaseInfrastructureWorkFlowTask getConcretePersonImplementation();

	private WorkFlowTaskType type = WorkFlowTaskType.INFRASTRUCTURE;

	public WorkFlow getWorkFlowChecker() {
		WorkFlow testWorkflow = Mockito.mock(WorkFlow.class);
		when(testWorkflow.getName()).thenReturn(workflowTestName);
		return testWorkflow;
	}

}