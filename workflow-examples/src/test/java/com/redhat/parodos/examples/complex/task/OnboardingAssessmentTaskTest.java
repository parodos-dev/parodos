package com.redhat.parodos.examples.complex.task;

import java.util.List;

import com.redhat.parodos.examples.base.BaseAssessmentTaskTest;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
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

	@BeforeEach
	public void setUp() {

		workflowOption = new WorkFlowOption.Builder("identifier", "workflowName")
				.setDescription("a test workflow option").displayName("WorkflowOption_A").addToDetails("Other details")
				.build();
		onboardingAssessmentTask = spy((OnboardingAssessmentTask) getTaskUnderTest());
	}

	@Override
	protected BaseAssessmentTask getTaskUnderTest() {
		return new OnboardingAssessmentTask(workflowOption);
	}

	@Test
	public void execute() {
		// given
		WorkContext workContext = mock(WorkContext.class);
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
