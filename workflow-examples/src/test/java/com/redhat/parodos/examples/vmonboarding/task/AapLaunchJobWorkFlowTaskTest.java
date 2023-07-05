package com.redhat.parodos.examples.vmonboarding.task;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.vmonboarding.dto.AapGetJobResponseDTO;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

public class AapLaunchJobWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static final String AAP_URL_TEST = "aap-url-test";

	private static final String WINDOWS_JOB_TEMPLATE_ID_TEST = "windows-job-template-id-test";

	private static final String RHEL_JOB_TEMPLATE_ID_TEST = "rhel-job-template-id-test";

	private static final String USERNAME_TEST = "username-test";

	public static final String PASSWORD_TEST = "password-test";

	public static final String VM_TYPE_PARAMETER_NAME = "VM_TYPE";

	public static final String VM_TYPE_PARAMETER_VALUE_TEST = "vm-type-test";

	public static final String AAP_GET_JOB_RESPONSE_DTO_JOB_ID_TEST = "job-id-test";

	private AapLaunchJobWorkFlowTask aapLaunchJobWorkFlowTask;

	@Before
	public void setUp() {
		this.aapLaunchJobWorkFlowTask = spy((AapLaunchJobWorkFlowTask) getConcretePersonImplementation());
		try {
			doReturn(VM_TYPE_PARAMETER_VALUE_TEST).when(this.aapLaunchJobWorkFlowTask)
					.getRequiredParameterValue(eq(VM_TYPE_PARAMETER_NAME));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getConcretePersonImplementation() {
		return new AapLaunchJobWorkFlowTask(AAP_URL_TEST, WINDOWS_JOB_TEMPLATE_ID_TEST, RHEL_JOB_TEMPLATE_ID_TEST,
				USERNAME_TEST, PASSWORD_TEST);
	}

	@Test
	public void executeSuccess() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		aapLaunchJobWorkFlowTask.setBeanName("test");
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic
					.when(() -> RestUtils.executePost(any(), any(String.class), any(), any(String.class),
							any(String.class), any()))
					.thenReturn(ResponseEntity
							.ok(AapGetJobResponseDTO.builder().jobId(AAP_GET_JOB_RESPONSE_DTO_JOB_ID_TEST).build()));
			try (MockedStatic<WorkContextUtils> workContextUtilsMockedStatic = mockStatic(WorkContextUtils.class)) {
				workContextUtilsMockedStatic.when(() -> WorkContextUtils.addParameter(any(WorkContext.class),
						any(String.class), any(String.class))).thenAnswer((Answer<Void>) invocation -> null);
				// when
				WorkReport workReport = aapLaunchJobWorkFlowTask.execute(workContext);

				// then
				assertEquals(WorkStatus.COMPLETED, workReport.getStatus());

			}
		}
	}

	@Test
	public void executeFail() {
		// given
		WorkContext workContext = mock(WorkContext.class);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(() -> RestUtils.executePost(any(), any(String.class), any(), any(String.class),
					any(String.class), any())).thenReturn(ResponseEntity.internalServerError().build());

			// when
			WorkReport workReport = aapLaunchJobWorkFlowTask.execute(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

}
