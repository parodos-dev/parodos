package com.redhat.parodos.workflow.execution.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.ControllerMockClient;
import com.redhat.parodos.workflow.execution.dto.WorkFlowCheckerTaskRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
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
		WorkFlowRequestDTO workFlowRequestDTO = WorkFlowRequestDTO.builder().build();
		workFlowRequestDTO.setProjectId(UUID.randomUUID().toString());
		workFlowRequestDTO.setWorkFlowName("FooWorkFlow");

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
		WorkFlowRequestDTO workFlowRequestDTO = WorkFlowRequestDTO.builder().build();
		workFlowRequestDTO.setProjectId(UUID.randomUUID().toString());
		workFlowRequestDTO.setWorkFlowName("FooWorkFlow");

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
	public void updateWorkFlowCheckerTaskStatusWithValidData() throws Exception {
		// given
		UUID workFlowExecutionId = UUID.randomUUID();
		String workFlowCheckerTaskName = "testWorkflowCheckerTaskName";

		// when
		doNothing().when(this.workFlowService).updateWorkFlowCheckerTaskStatus(Mockito.any(), Mockito.any(),
				Mockito.any());

		// then
		this.mockMvc
				.perform(this
						.postRequestWithValidCredentials(String.format("/api/v1/workflows/%s/checkers/%s",
								workFlowExecutionId, workFlowCheckerTaskName))
						.content(getWorkFlowCheckerTaskRequestDTOJsonPayload()))
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
		this.mockMvc
				.perform(this
						.postRequestWithValidCredentials(String.format("/api/v1/workflows/%s/checkers/%s",
								workFlowExecutionId, workFlowCheckerTaskName))
						.content(getWorkFlowCheckerTaskRequestDTOJsonPayload()))
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
		this.mockMvc
				.perform(this
						.postRequestWithValidCredentials(String.format("/api/v1/workflows/%s/checkers/%s",
								workFlowExecutionId, workFlowCheckerTaskName))
						.content(getWorkFlowCheckerTaskRequestDTOJsonPayload()))
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