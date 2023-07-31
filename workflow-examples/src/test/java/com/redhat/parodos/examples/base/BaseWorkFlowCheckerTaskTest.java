package com.redhat.parodos.examples.base;

import java.util.List;

import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.task.checker.BaseWorkFlowCheckerTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskType;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseWorkFlowCheckerTaskTest {

	/**
	 * These are the options this AssessmentTasks can return
	 */
	private List<WorkFlowOption> workflowOptions;

	@SuppressWarnings("unused")
	private BaseWorkFlowCheckerTask baseWorkFlowCheckerTask;

	@BeforeEach
	public void setUp() {
		this.baseWorkFlowCheckerTask = getTaskUnderTest();
	}

	/**
	 * Provides a real implementation of the class to be tested. This method should be
	 * used when extending {@link BaseWorkFlowCheckerTaskTest} to create a test class,
	 * such as
	 * {@link com.redhat.parodos.examples.complex.checker.NamespaceApprovalWorkFlowCheckerTaskTest}.
	 * Example usage: <pre> {@code
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
	protected abstract BaseWorkFlowCheckerTask getTaskUnderTest();

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
