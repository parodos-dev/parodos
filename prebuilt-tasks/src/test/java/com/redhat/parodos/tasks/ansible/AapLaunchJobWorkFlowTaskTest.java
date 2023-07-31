package com.redhat.parodos.tasks.ansible;

import java.util.UUID;

import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

public class AapLaunchJobWorkFlowTaskTest {

	private static final String AAP_URL_TEST = "aap-url-test";

	private static final String USERNAME_TEST = "username-test";

	private static final String PASSWORD_TEST = "password-test";

	private static final String VM_TYPE_PARAMETER_NAME = "VM_TYPE";

	private static final String VM_TYPE_PARAMETER_VALUE_TEST = "vm-type-test";

	private static final String AAP_GET_JOB_RESPONSE_DTO_JOB_ID_TEST = "job-id-test";

	private AapLaunchJobWorkFlowTask aapLaunchJobWorkFlowTask;

	@BeforeEach
	public void setUp() {
		this.aapLaunchJobWorkFlowTask = spy(new AapLaunchJobWorkFlowTask(AAP_URL_TEST, USERNAME_TEST, PASSWORD_TEST));
		try {
			doReturn(VM_TYPE_PARAMETER_VALUE_TEST).when(this.aapLaunchJobWorkFlowTask)
					.getRequiredParameterValue(eq(VM_TYPE_PARAMETER_NAME));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void executeSuccess() {
		// given
		WorkContext workContext = mock(WorkContext.class);
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
		try (MockedStatic<RestUtils> restUtilsMockedStatic = mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(() -> RestUtils.executePost(any(), any(String.class), any(), any(String.class),
					any(String.class), any())).thenReturn(ResponseEntity.internalServerError().build());

			// when
			WorkContext workContext = new WorkContext();
			WorkContextUtils.setMainExecutionId(workContext, UUID.randomUUID());
			aapLaunchJobWorkFlowTask.setBeanName("test");
			aapLaunchJobWorkFlowTask.preExecute(workContext);

			WorkReport workReport = aapLaunchJobWorkFlowTask.execute(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

}
