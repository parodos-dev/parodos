package com.redhat.parodos.examples.vmonboarding.task;

import com.redhat.parodos.examples.base.BaseAssessmentTaskTest;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class OnboardingVmAssessmentTaskTest extends BaseAssessmentTaskTest {

	private OnboardingVmAssessmentTask onboardingVmAssessmentTask;

	@Before
	public void setUp() {
		this.onboardingVmAssessmentTask = spy((OnboardingVmAssessmentTask) getConcretePersonImplementation());
	}

	@Override
	protected BaseAssessmentTask getConcretePersonImplementation() {
		return new OnboardingVmAssessmentTask(getWorkFlowOptions());
	}

	@Test
	public void executeSuccess() {
		WorkContext workContext = mock(WorkContext.class);
		WorkReport workReport = onboardingVmAssessmentTask.execute(workContext);
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());
	}

}
