package com.redhat.parodos.examples.base;

import java.util.List;

import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskType;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseAssessmentTaskTest {

	/**
	 * These are the options this AssessmentTasks can return
	 */
	private List<WorkFlowOption> workflowOptions;

	@SuppressWarnings("unused")
	private BaseAssessmentTask baseAssessmentTask;

	@BeforeEach
	public void setUp() {
		this.baseAssessmentTask = getTaskUnderTest();
	}

	/**
	 * Provides a real implementation of the class to be tested. This method should be
	 * used when extending {@link BaseAssessmentTaskTest} to create a test class, such as
	 * {@link com.redhat.parodos.examples.complex.task.OnboardingAssessmentTaskTest}.
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
	protected abstract BaseAssessmentTask getTaskUnderTest();

	public List<WorkFlowOption> getWorkFlowOptions() {
		return workflowOptions;
	}

	public WorkFlowTaskType getType() {
		return WorkFlowTaskType.ASSESSMENT;
	}

}
