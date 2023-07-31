package com.redhat.parodos.examples.base;

import java.util.Arrays;
import java.util.List;

import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.mock;

public abstract class BaseInfrastructureWorkFlowTaskTest {

	protected final static String workflowTestName = "TestName";

	@SuppressWarnings("unused")
	private BaseInfrastructureWorkFlowTask baseInfrastructureWorkFlowTask;

	@BeforeEach
	public void setUp() {
		this.baseInfrastructureWorkFlowTask = getTaskUnderTest();
	}

	/**
	 * Provides a real implementation of the class to be tested. This method should be
	 * used when extending {@link BaseInfrastructureWorkFlowTaskTest} to create a test
	 * class, such as
	 * {@link com.redhat.parodos.examples.simple.task.LoggingWorkFlowTaskTest}. Example
	 * usage: <pre> {@code
	 *     private MyTaskClass myTaskClass;
	 *
	 *     &#64;Before
	 *     public void setUp() {
	 *         this.myTaskClass = spy((MyTaskClass) getTaskUnderTest());
	 *         try {
	 *              // Set up a mocked return value for the 'getRequiredParameterValue' method
	 * 				doReturn("a_value").when(this.myTaskClass).getRequiredParameterValue(eq("a_param"));
	 *         }
	 * 		   catch (MissingParameterException e) {
	 * 			throw new RuntimeException(e);
	 *        }
	 *     }
	 *
	 *     &#64;Test
	 *     public void exampleTest() {
	 *         // Test implementation here
	 *         // ...
	 *
	 *         // Invoke the real implementation of 'checkWorkFlowStatus' method
	 *         WorkReport workReport = myTaskClass.checkWorkFlowStatus(workContext);
	 *     }
	 * }</pre>
	 **/
	protected abstract BaseInfrastructureWorkFlowTask getTaskUnderTest();

	public List<WorkFlow> getWorkFlowCheckers() {
		WorkFlow testWorkflow1 = mock(WorkFlow.class);
		WorkFlow testWorkflow2 = mock(WorkFlow.class);
		return Arrays.asList(testWorkflow1, testWorkflow2);
	}

}
