package com.redhat.parodos.examples.complex.task;

import com.redhat.parodos.examples.base.BaseAssessmentTaskTest;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
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
		onboardingAssessmentTask = Mockito.spy((OnboardingAssessmentTask) getConcretePersonImplementation());
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
		List<WorkParameter> workParameters = onboardingAssessmentTask.getWorkFlowTaskParameters();

		// then
		assertNotNull(workParameters);
		assertEquals(3, workParameters.size());
		assertEquals(INPUT, workParameters.get(0).getKey());
		assertEquals("Enter some information to use for the Assessment to determine if they can onboard",
				workParameters.get(0).getDescription());
		assertEquals(WorkParameterType.TEXT, workParameters.get(0).getType());
		assertEquals(WorkParameterType.SELECT, workParameters.get(1).getType());
		assertEquals(WorkParameterType.MULTI_SELECT, workParameters.get(2).getType());
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