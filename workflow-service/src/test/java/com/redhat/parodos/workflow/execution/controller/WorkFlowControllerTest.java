package com.redhat.parodos.workflow.execution.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.ControllerMockClient;
import com.redhat.parodos.workflow.enums.ParodosWorkStatus;
import com.redhat.parodos.workflow.enums.WorkFlowStatus;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkFlowCheckerTaskRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkStatusResponseDTO;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("local")
class WorkFlowControllerTest extends ControllerMockClient {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private WorkFlowServiceImpl workFlowService;

	@Test
	public void ExecuteWithoutAuth() throws Exception {
		// when
		this.mockMvc.perform(this.getRequestWithInValidCredentials("/api/v1/workflows"))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		// then
		Mockito.verify(this.workFlowService, Mockito.never()).execute(Mockito.any());
	}

	@Test
	public void ExecuteWithValidData() throws Exception {

		// given
		WorkFlowRequestDTO workFlowRequestDTO = WorkFlowRequestDTO.builder().projectId(UUID.randomUUID().toString())
				.workFlowName("FooWorkFlow").build();

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(workFlowRequestDTO);

		WorkReport report = Mockito.mock(WorkReport.class);
		WorkContext workContext = new WorkContext();
		workContext.put("WORKFLOW_EXECUTION_ID", UUID.randomUUID().toString());
		Mockito.when(report.getWorkContext()).thenReturn(workContext);
		Mockito.when(this.workFlowService.execute(Mockito.any())).thenReturn(report);

		// when
		this.mockMvc.perform(this.postRequestWithValidCredentials("/api/v1/workflows").content(json))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.workFlowExecutionId",
						Matchers.is(workContext.get("WORKFLOW_EXECUTION_ID"))));

		// then
		Mockito.verify(this.workFlowService, Mockito.times(1)).execute(Mockito.any());
	}

	@Test
	public void ServiceExecuteFails() throws Exception {
		// given
		WorkFlowRequestDTO workFlowRequestDTO = WorkFlowRequestDTO.builder().projectId(UUID.randomUUID().toString())
				.workFlowName("FooWorkFlow").build();

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(workFlowRequestDTO);

		Mockito.when(this.workFlowService.execute(Mockito.any())).thenReturn(null);

		// when
		this.mockMvc.perform(this.postRequestWithValidCredentials("/api/v1/workflows").content(json))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError());

		// then
		Mockito.verify(this.workFlowService, Mockito.times(1)).execute(Mockito.any());
	}

	@Test
	public void TestGetStatusWithoutAuth() throws Exception {
		// when
		this.mockMvc
				.perform(this.getRequestWithInValidCredentials(
						String.format("/api/v1/workflows/%s/status", UUID.randomUUID().toString())))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	@Test
	public void testGetStatus() throws Exception {
		// given
		UUID masterWorkFlowExecutionId = UUID.randomUUID();
		String testMasterWorkFlow = "testMasterWorkFlow";
		String testSubWorkFlow1 = "testSubWorkFlow1";
		String testSubWorkFlowTask1 = "testSubWorkFlowTask1";
		String testWorkFlowTask1 = "testWorkFlowTask1";

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowStatusResponseDTO.builder()
				.workFlowExecutionId(masterWorkFlowExecutionId.toString()).status(WorkFlowStatus.IN_PROGRESS.name())
				.workFlowName(testMasterWorkFlow)
				.works(List.of(
						WorkStatusResponseDTO.builder().name(testSubWorkFlow1).status(ParodosWorkStatus.PENDING)
								.type(WorkType.WORKFLOW)
								.works(List.of(WorkStatusResponseDTO.builder().name(testSubWorkFlowTask1)
										.status(ParodosWorkStatus.PENDING).type(WorkType.TASK).build()))
								.build(),
						WorkStatusResponseDTO.builder().name(testWorkFlowTask1).status(ParodosWorkStatus.COMPLETED)
								.type(WorkType.TASK).build()))
				.build();
		Mockito.when(workFlowService.getWorkFlowStatus(masterWorkFlowExecutionId))
				.thenReturn(workFlowStatusResponseDTO);

		// when
		this.mockMvc
				.perform(this.getRequestWithValidCredentials(
						String.format("/api/v1/workflows/%s/status", masterWorkFlowExecutionId)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.workFlowExecutionId",
						Matchers.is(masterWorkFlowExecutionId.toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.workFlowName", Matchers.is(testMasterWorkFlow)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(WorkFlowStatus.IN_PROGRESS.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].name", Matchers.is(testSubWorkFlow1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].type", Matchers.is(WorkType.WORKFLOW.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].status",
						Matchers.is(ParodosWorkStatus.PENDING.name())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.works[0].works[0].name", Matchers.is(testSubWorkFlowTask1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].works[0].status",
						Matchers.is(ParodosWorkStatus.PENDING.name())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.works[0].works[0].type", Matchers.is(WorkType.TASK.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[1].name", Matchers.is(testWorkFlowTask1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[1].type", Matchers.is(WorkType.TASK.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[1].status",
						Matchers.is(ParodosWorkStatus.COMPLETED.name())));

		// then
		Mockito.verify(this.workFlowService, Mockito.times(1)).getWorkFlowStatus(masterWorkFlowExecutionId);
	}

	@Test
	public void updateWorkFlowCheckerTaskStatusWithValidData() throws Exception {
		// given
		UUID workFlowExecutionId = UUID.randomUUID();
		String workFlowCheckerTaskName = "testWorkflowCheckerTaskName";

		// when
		doNothing().when(this.workFlowService).updateWorkFlowCheckerTaskStatus(Mockito.any(), Mockito.any(),
				Mockito.any());

		// then
		String pathUrl = String.format("/api/v1/workflows/%s/checkers/%s", workFlowExecutionId,
				workFlowCheckerTaskName);
		String jsonPayload = getWorkFlowCheckerTaskRequestDTOJsonPayload();
		this.mockMvc.perform(this.postRequestWithValidCredentials(pathUrl).content(jsonPayload))
				.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(this.workFlowService, Mockito.times(1)).updateWorkFlowCheckerTaskStatus(Mockito.any(),
				Mockito.any(), Mockito.any());
	}

	@Test
	public void updateWorkFlowCheckerTaskStatusWithInvalidData() throws Exception {
		// given
		UUID workFlowExecutionId = UUID.randomUUID();
		String workFlowCheckerTaskName = "testWorkflowCheckerTaskName";

		// when
		doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(this.workFlowService)
				.updateWorkFlowCheckerTaskStatus(Mockito.any(), Mockito.anyString(),
						Mockito.any(WorkFlowTaskStatus.class));

		// then
		String pathUrl = String.format("/api/v1/workflows/%s/checkers/%s", workFlowExecutionId,
				workFlowCheckerTaskName);
		String jsonPayload = getWorkFlowCheckerTaskRequestDTOJsonPayload();
		this.mockMvc.perform(this.postRequestWithValidCredentials(pathUrl).content(jsonPayload))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	public void updateWorkFlowCheckerTaskStatusWithNotFoundData() throws Exception {
		// given
		UUID workFlowExecutionId = UUID.randomUUID();
		String workFlowCheckerTaskName = "testWorkflowCheckerTaskName";

		// when
		doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(this.workFlowService)
				.updateWorkFlowCheckerTaskStatus(Mockito.any(), Mockito.anyString(),
						Mockito.any(WorkFlowTaskStatus.class));

		// then
		String pathUrl = String.format("/api/v1/workflows/%s/checkers/%s", workFlowExecutionId,
				workFlowCheckerTaskName);
		String jsonPayload = getWorkFlowCheckerTaskRequestDTOJsonPayload();
		this.mockMvc.perform(this.postRequestWithValidCredentials(pathUrl).content(jsonPayload))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void TestGetStatusWithValidData() throws Exception {
		// @TODO this test should be completed when the API is implemented
		// when
		this.mockMvc
				.perform(this.getRequestWithValidCredentials(
						String.format("/api/v1/workflows/%s/status", UUID.randomUUID().toString())))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	private String getWorkFlowCheckerTaskRequestDTOJsonPayload() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		WorkFlowCheckerTaskRequestDTO workFlowCheckerTaskRequestDTO = WorkFlowCheckerTaskRequestDTO.builder()
				.status(WorkFlowTaskStatus.COMPLETED).build();
		return objectMapper.writeValueAsString(workFlowCheckerTaskRequestDTO);
	}

}