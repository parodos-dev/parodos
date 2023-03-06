package com.redhat.parodos.examples.continued.complex;

import com.redhat.parodos.examples.base.BaseAssessmentTaskTest;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.task.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;

/**
 * Onboarding Assessment Task execution test
 *
 * @author Gloria Ciavarrini (GitHub: gciavarrini)
 */
public class OnboardingAssessmentTaskTest extends BaseAssessmentTaskTest {

	private OnboardingAssessmentTask onboardingAssessmentTask;

	private WorkFlowOption workflowOption;

	private static final String INPUT = "INPUT";

	@Before
	public void setUp() {

		workflowOption = new WorkFlowOption.Builder("identifier", "workflowName")
				.setDescription("a test workflow option").displayName("WorkflowOption_A").addToDetails("Other details")
				.build();
		onboardingAssessmentTask = spy((OnboardingAssessmentTask) getConcretePersonImplementation());
	}

	@Override
	protected BaseAssessmentTask getConcretePersonImplementation() {
		return new OnboardingAssessmentTask(workflowOption);
	}

	@Test
	public void execute() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);
		WorkReport workReport = onboardingAssessmentTask.execute(workContext);

		// then
		assertNotNull(workReport);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
		assertNull(workReport.getError());
	}

	@Test
	public void getWorkFlowTaskParameters() {
		// when
		List<WorkFlowTaskParameter> workFlowTaskParameters = onboardingAssessmentTask.getWorkFlowTaskParameters();

		// then
		assertNotNull(workFlowTaskParameters);
		assertEquals(1, workFlowTaskParameters.size());
		assertEquals(INPUT, workFlowTaskParameters.get(0).getKey());
		assertEquals("Enter some information to use for the Assessment to determine if they can onboard",
				workFlowTaskParameters.get(0).getDescription());
		assertEquals(WorkFlowTaskParameterType.TEXT, workFlowTaskParameters.get(0).getType());
	}

	@Test
	public void getWorkFlowTaskOutputs() {
		// when
		List<WorkFlowTaskOutput> workFlowTaskOutputs = onboardingAssessmentTask.getWorkFlowTaskOutputs();

		// then
		assertNotNull(workFlowTaskOutputs);
		assertEquals(0, workFlowTaskOutputs.size());
	}

}