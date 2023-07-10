package com.redhat.parodos.examples.base;

import java.util.Arrays;
import java.util.List;

import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.junit.Before;

import static org.mockito.Mockito.mock;

public abstract class BaseInfrastructureWorkFlowTaskTest {

	protected final static String workflowTestName = "TestName";

	@SuppressWarnings("unused")
	private BaseInfrastructureWorkFlowTask baseInfrastructureWorkFlowTask;

	@Before
	public void setUp() {
		this.baseInfrastructureWorkFlowTask = getConcreteImplementation();
	}

	/**
	 * Each test class based on this abstract class will implement this function
	 * @return new instance of non-abstract class extending BaseInfrastructureWorkFlowTask
	 */
	protected abstract BaseInfrastructureWorkFlowTask getConcreteImplementation();

	public List<WorkFlow> getWorkFlowCheckers() {
		WorkFlow testWorkflow1 = mock(WorkFlow.class);
		WorkFlow testWorkflow2 = mock(WorkFlow.class);
		return Arrays.asList(testWorkflow1, testWorkflow2);
	}

}