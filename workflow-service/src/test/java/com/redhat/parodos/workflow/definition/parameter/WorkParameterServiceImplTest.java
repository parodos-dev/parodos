package com.redhat.parodos.workflow.definition.parameter;

import java.util.List;
import java.util.Map;

import com.redhat.parodos.workflow.definition.dto.WorkParameterValueRequestDTO;
import com.redhat.parodos.workflow.definition.dto.WorkParameterValueResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionService;
import com.redhat.parodos.workflow.parameter.WorkParameterValueProvider;
import com.redhat.parodos.workflow.parameter.WorkParameterValueRequest;
import com.redhat.parodos.workflow.parameter.WorkParameterValueResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
class WorkParameterServiceImplTest {

	private static final String DATA_PROVIDER_NAME = "test-data-provider";

	private static final String INVALID_DATA_PROVIDER = "invalid-data-provider";

	private static final String TEST_KEY = "test-key";

	private static final String TEST_VALUE = "test-value";

	private static final String TEST_WORK = "test-work";

	private static final String TEST_RESPONSE_KEY = "test-response-key";

	private static final String TEST_INVALID_RESPONSE_KEY = "invalid-test-response-key";

	private static final String OPTION_1 = "option1";

	private static final String OPTION_2 = "option2";

	public static final String WORKFLOW_NAME = "workflow-name";

	public static final String SUB_WORKFLOW_NAME = "sub-workflow-name";

	@Mock
	private WorkParameterValueProvider workParameterValueProvider;

	@Mock
	private WorkFlowDefinitionService workFlowDefinitionService;

	private WorkParameterServiceImpl workFlowParameterService;

	private static List<WorkParameterValueRequestDTO> workParameterValueRequestDTOs;

	private static List<WorkParameterValueResponseDTO> workParameterValueResponseDTOs;

	@BeforeEach
	void beforeEach() {
		workFlowParameterService = new WorkParameterServiceImpl(Map.of(DATA_PROVIDER_NAME, workParameterValueProvider),
				workFlowDefinitionService);
		List<WorkParameterValueRequest> workParameterValueRequests = getSampleParameterValueRequests();
		List<WorkParameterValueResponse> workParameterValueResponses = getSampleParameterValueResponses();
		workParameterValueRequestDTOs = getSampleParameterValueRequestDTOs();
		workParameterValueResponseDTOs = getSampleParameterValueResponseDTOs();
		Mockito.when(workParameterValueProvider.getValuesForWorkflow(anyString(), eq(workParameterValueRequests)))
				.thenReturn(workParameterValueResponses);
	}

	@Test
	void updateValue_when_dataProviderIsFound_then_returnModifiedOptions() {
		Mockito.when(workFlowDefinitionService.getWorkParametersByWorkName(TEST_WORK))
				.thenReturn(Map.of(TEST_RESPONSE_KEY, "test-value"));
		Mockito.when(workFlowDefinitionService.getParentWorkFlowByWorkName(TEST_WORK))
				.thenReturn(WorkFlowDefinition.builder().name(SUB_WORKFLOW_NAME).build());
		Mockito.when(workFlowDefinitionService.getParentWorkFlowByWorkName(SUB_WORKFLOW_NAME))
				.thenReturn(WorkFlowDefinition.builder().name(WORKFLOW_NAME).build());
		Mockito.when(workFlowDefinitionService.getParentWorkFlowByWorkName(WORKFLOW_NAME)).thenReturn(null);
		assertEquals(workParameterValueResponseDTOs,
				workFlowParameterService.getValues(WORKFLOW_NAME, DATA_PROVIDER_NAME, workParameterValueRequestDTOs));
	}

	@Test
	void updateValue_when_dataProviderIsNotFound_then_returnEmptyList() {
		assertThat(
				workFlowParameterService.getValues(WORKFLOW_NAME, INVALID_DATA_PROVIDER, workParameterValueRequestDTOs))
						.isEmpty();
	}

	@Test
	void updateValue_when_workIsNotFound_then_returnEmptyList() {
		Mockito.when(workFlowDefinitionService.getWorkParametersByWorkName(anyString())).thenReturn(null);
		assertThat(
				workFlowParameterService.getValues(WORKFLOW_NAME, INVALID_DATA_PROVIDER, workParameterValueRequestDTOs))
						.isEmpty();
	}

	@Test
	void updateValue_when_parameterIsNotFound_then_returnEmptyList() {
		Mockito.when(workFlowDefinitionService.getWorkParametersByWorkName(anyString()))
				.thenReturn(Map.of(TEST_INVALID_RESPONSE_KEY, "test-value"));
		assertThat(
				workFlowParameterService.getValues(WORKFLOW_NAME, INVALID_DATA_PROVIDER, workParameterValueRequestDTOs))
						.isEmpty();
	}

	private static List<WorkParameterValueRequestDTO> getSampleParameterValueRequestDTOs() {
		return List
				.of(WorkParameterValueRequestDTO.builder().key(TEST_KEY).value(TEST_VALUE).workName(TEST_WORK).build());

	}

	private static List<WorkParameterValueRequest> getSampleParameterValueRequests() {
		return List.of(WorkParameterValueRequest.builder().key(TEST_KEY).value(TEST_VALUE).workName(TEST_WORK).build());

	}

	private static List<WorkParameterValueResponse> getSampleParameterValueResponses() {
		return List.of(WorkParameterValueResponse.builder().key(TEST_RESPONSE_KEY).options(List.of(OPTION_1, OPTION_2))
				.value(OPTION_1).workName(TEST_WORK).build());
	}

	private static List<WorkParameterValueResponseDTO> getSampleParameterValueResponseDTOs() {
		return List.of(WorkParameterValueResponseDTO.builder().key(TEST_RESPONSE_KEY)
				.options(List.of(OPTION_1, OPTION_2)).value(OPTION_1)
				.propertyPath(String.join(".", WORKFLOW_NAME, SUB_WORKFLOW_NAME, TEST_WORK)).build());
	}

}
