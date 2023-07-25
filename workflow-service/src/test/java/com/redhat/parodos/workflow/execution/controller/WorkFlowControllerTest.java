package com.redhat.parodos.workflow.execution.controller;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.ControllerMockClient;
import com.redhat.parodos.common.exceptions.WorkFlowNotFoundException;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkFlowCheckerTaskRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkStatusResponseDTO;
import com.redhat.parodos.workflow.execution.service.WorkFlowLogServiceImpl;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.version.WorkFlowVersionService;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

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

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("local")
class WorkFlowControllerTest extends ControllerMockClient {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private WorkFlowServiceImpl workFlowService;

	@MockBean
	private WorkFlowLogServiceImpl workFlowLogService;

	@MockBean
	private WorkFlowVersionService workFlowVersionService;

	@Test
	public void ExecuteWithoutAuth() throws Exception {
		// when
		this.mockMvc.perform(this.getRequestWithInValidCredentials("/api/v1/workflows"))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		// then
		verify(this.workFlowService, never()).execute(any());
	}

	@Test
	public void ExecuteWithValidData() throws Exception {

		// given
		WorkFlowRequestDTO workFlowRequestDTO = WorkFlowRequestDTO.builder().projectId(UUID.randomUUID())
				.workFlowName("FooWorkFlow").build();

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(workFlowRequestDTO);

		WorkReport report = mock(WorkReport.class);
		WorkContext workContext = new WorkContext();
		workContext.put("WORKFLOW_EXECUTION_ID", UUID.randomUUID().toString());
		when(report.getWorkContext()).thenReturn(workContext);
		when(report.getStatus()).thenReturn(WorkStatus.IN_PROGRESS);
		when(this.workFlowService.execute(any())).thenReturn(report);

		// when
		this.mockMvc.perform(this.postRequestWithValidCredentials("/api/v1/workflows").content(json))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.workFlowExecutionId",
						Matchers.is(workContext.get("WORKFLOW_EXECUTION_ID"))));

		// then
		verify(this.workFlowService, times(1)).execute(any());
	}

	@Test
	public void ServiceExecuteFails() throws Exception {
		// given
		WorkFlowRequestDTO workFlowRequestDTO = WorkFlowRequestDTO.builder().projectId(UUID.randomUUID())
				.workFlowName("FooWorkFlow").build();

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(workFlowRequestDTO);

		// Mock the validation of the WorkFlow: not found error is thrown
		when(this.workFlowService.execute(any())).thenThrow(new WorkFlowNotFoundException("Test"));

		// when
		this.mockMvc.perform(this.postRequestWithValidCredentials("/api/v1/workflows").content(json))
				.andExpect(MockMvcResultMatchers.status().isNotFound());

		// then
		verify(this.workFlowService, times(1)).execute(any());
	}

	@Test
	public void TestGetStatusWithoutAuth() throws Exception {
		// when
		this.mockMvc
				.perform(this.getRequestWithInValidCredentials(
						String.format("/api/v1/workflows/%s/status", UUID.randomUUID())))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	@Test
	public void testGetStatus() throws Exception {
		// given
		UUID mainWorkFlowExecutionId = UUID.randomUUID();
		String testMainWorkFlow = "testMainWorkFlow";
		String testSubWorkFlow1 = "testSubWorkFlow1";
		String testSubWorkFlowTask1 = "testSubWorkFlowTask1";
		String testWorkFlowTask1 = "testWorkFlowTask1";

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowStatusResponseDTO.builder()
				.workFlowExecutionId(mainWorkFlowExecutionId).status(WorkStatus.IN_PROGRESS)
				.workFlowName(testMainWorkFlow)
				.works(List.of(
						WorkStatusResponseDTO.builder().name(testSubWorkFlow1).status(WorkStatus.PENDING)
								.type(WorkType.WORKFLOW)
								.works(List.of(WorkStatusResponseDTO.builder().name(testSubWorkFlowTask1)
										.status(WorkStatus.PENDING).type(WorkType.TASK).build()))
								.build(),
						WorkStatusResponseDTO.builder().name(testWorkFlowTask1).status(WorkStatus.COMPLETED)
								.type(WorkType.TASK).build()))
				.build();
		when(workFlowService.getWorkFlowStatus(mainWorkFlowExecutionId)).thenReturn(workFlowStatusResponseDTO);

		// when
		this.mockMvc
				.perform(this.getRequestWithValidCredentials(
						String.format("/api/v1/workflows/%s/status", mainWorkFlowExecutionId)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.workFlowExecutionId",
						Matchers.is(mainWorkFlowExecutionId.toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.workFlowName", Matchers.is(testMainWorkFlow)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(WorkStatus.IN_PROGRESS.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].name", Matchers.is(testSubWorkFlow1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].type", Matchers.is(WorkType.WORKFLOW.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].status", Matchers.is(WorkStatus.PENDING.name())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.works[0].works[0].name", Matchers.is(testSubWorkFlowTask1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].works[0].status",
						Matchers.is(WorkStatus.PENDING.name())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.works[0].works[0].type", Matchers.is(WorkType.TASK.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[1].name", Matchers.is(testWorkFlowTask1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[1].type", Matchers.is(WorkType.TASK.name())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.works[1].status", Matchers.is(WorkStatus.COMPLETED.name())));

		// then
		verify(this.workFlowService, times(1)).getWorkFlowStatus(mainWorkFlowExecutionId);
	}

	@Test
	public void testGetStatusWithFailedWorkflowAndTask() throws Exception {
		// given
		UUID mainWorkFlowExecutionId = UUID.randomUUID();
		String testMainWorkFlow = "testMainWorkFlow";
		String testSubWorkFlow1 = "testSubWorkFlow1";
		String testSubWorkFlowTask1 = "testSubWorkFlowTask1";
		String testWorkFlowTask1 = "testWorkFlowTask1";
		String testFailedWorkFlowTask1 = "testFailedWorkFlowTask1";
		String errorMessageMainWorkflow = "Main workflow error message";
		String errorMessage = "Error message";

		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowStatusResponseDTO.builder()
				.workFlowExecutionId(mainWorkFlowExecutionId).status(WorkStatus.FAILED).workFlowName(testMainWorkFlow)
				.message(errorMessageMainWorkflow)
				.works(List.of(
						WorkStatusResponseDTO.builder().name(testSubWorkFlow1).status(WorkStatus.PENDING)
								.type(WorkType.WORKFLOW).message("")
								.works(List.of(WorkStatusResponseDTO.builder().name(testSubWorkFlowTask1)
										.status(WorkStatus.PENDING).type(WorkType.TASK).build()))
								.build(),
						WorkStatusResponseDTO.builder().name(testWorkFlowTask1).status(WorkStatus.COMPLETED)
								.type(WorkType.TASK).message("").build(),
						WorkStatusResponseDTO.builder().name(testFailedWorkFlowTask1).status(WorkStatus.FAILED)
								.message(errorMessage).type(WorkType.TASK).build()))
				.build();
		when(workFlowService.getWorkFlowStatus(mainWorkFlowExecutionId)).thenReturn(workFlowStatusResponseDTO);

		// when
		this.mockMvc
				.perform(this.getRequestWithValidCredentials(
						String.format("/api/v1/workflows/%s/status", mainWorkFlowExecutionId)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.workFlowExecutionId",
						Matchers.is(mainWorkFlowExecutionId.toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.workFlowName", Matchers.is(testMainWorkFlow)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(WorkStatus.FAILED.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is(errorMessageMainWorkflow)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].name", Matchers.is(testSubWorkFlow1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].type", Matchers.is(WorkType.WORKFLOW.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].status", Matchers.is(WorkStatus.PENDING.name())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.works[0].works[0].name", Matchers.is(testSubWorkFlowTask1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[0].works[0].status",
						Matchers.is(WorkStatus.PENDING.name())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.works[0].works[0].type", Matchers.is(WorkType.TASK.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[1].name", Matchers.is(testWorkFlowTask1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[1].type", Matchers.is(WorkType.TASK.name())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.works[1].status", Matchers.is(WorkStatus.COMPLETED.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[1].message", Matchers.is("")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[2].name", Matchers.is(testFailedWorkFlowTask1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[2].type", Matchers.is(WorkType.TASK.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[2].status", Matchers.is(WorkStatus.FAILED.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.works[2].message", Matchers.is(errorMessage)));

		// then
		verify(this.workFlowService, times(1)).getWorkFlowStatus(mainWorkFlowExecutionId);
	}

	@Test
	public void updateWorkFlowCheckerTaskStatusWithValidData() throws Exception {
		// given
		UUID workFlowExecutionId = UUID.randomUUID();
		String workFlowCheckerTaskName = "testWorkflowCheckerTaskName";

		// when
		doNothing().when(this.workFlowService).updateWorkFlowCheckerTaskStatus(any(), any(), any());

		// then
		String pathUrl = String.format("/api/v1/workflows/%s/checkers/%s", workFlowExecutionId,
				workFlowCheckerTaskName);
		String jsonPayload = getWorkFlowCheckerTaskRequestDTOJsonPayload();
		this.mockMvc.perform(this.postRequestWithValidCredentials(pathUrl).content(jsonPayload))
				.andExpect(MockMvcResultMatchers.status().isOk());

		verify(this.workFlowService, times(1)).updateWorkFlowCheckerTaskStatus(any(), any(), any());
	}

	@Test
	public void updateWorkFlowCheckerTaskStatusWithInvalidData() throws Exception {
		// given
		UUID workFlowExecutionId = UUID.randomUUID();
		String workFlowCheckerTaskName = "testWorkflowCheckerTaskName";

		// when
		doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(this.workFlowService)
				.updateWorkFlowCheckerTaskStatus(any(), anyString(), any(WorkStatus.class));

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
				.updateWorkFlowCheckerTaskStatus(any(), anyString(), any(WorkStatus.class));

		// then
		String pathUrl = String.format("/api/v1/workflows/%s/checkers/%s", workFlowExecutionId,
				workFlowCheckerTaskName);
		String jsonPayload = getWorkFlowCheckerTaskRequestDTOJsonPayload();
		this.mockMvc.perform(this.postRequestWithValidCredentials(pathUrl).content(jsonPayload))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void TestGetStatusWithValidData() throws Exception {
		UUID mainWorkFlowExecutionId = UUID.randomUUID();
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowStatusResponseDTO.builder()
				.workFlowExecutionId(mainWorkFlowExecutionId).status(WorkStatus.IN_PROGRESS).workFlowName("test")
				.works(List.of(
						WorkStatusResponseDTO.builder().name("test").status(WorkStatus.PENDING).type(WorkType.WORKFLOW)
								.works(List.of(WorkStatusResponseDTO.builder().name("test").status(WorkStatus.PENDING)
										.type(WorkType.TASK).build()))
								.build(),
						WorkStatusResponseDTO.builder().name("test").status(WorkStatus.COMPLETED).type(WorkType.TASK)
								.build()))
				.build();
		when(workFlowService.getWorkFlowStatus(mainWorkFlowExecutionId)).thenReturn(workFlowStatusResponseDTO);

		// when
		this.mockMvc
				.perform(this.getRequestWithValidCredentials(
						String.format("/api/v1/workflows/%s/status", mainWorkFlowExecutionId)))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	void TestGetWorkFlowByProjectIdWithValidData() throws Exception {
		UUID workFlowExecutionId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		when(workFlowService.getWorkFlowsByProjectId(projectId)).thenReturn(List.of(WorkFlowResponseDTO.builder()
				.workFlowExecutionId(workFlowExecutionId).workStatus(WorkStatus.COMPLETED).build()));
		// when
		this.mockMvc
				.perform(this.getRequestWithValidCredentials("/api/v1/workflows").param("projectId",
						projectId.toString()))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1))).andExpect(MockMvcResultMatchers
						.jsonPath("$[0].workStatus", equalToIgnoringCase(WorkStatus.COMPLETED.name())));
	}

	@Test
	void TestGetWorkFlowTaskLogWithValidData() throws Exception {
		UUID workFlowExecutionId = UUID.randomUUID();
		String taskName = "test-task";
		String taskLog = "test-log";
		when(workFlowLogService.getLog(workFlowExecutionId, taskName)).thenReturn(taskLog);
		// when
		this.mockMvc
				.perform(this.getRequestWithValidCredentials("/api/v1/workflows/" + workFlowExecutionId + "/log")
						.param("taskName", taskName))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("text/plain;charset=UTF-8"))
				.andExpect(MockMvcResultMatchers.content().string(taskLog));
	}

	private String getWorkFlowCheckerTaskRequestDTOJsonPayload() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		WorkFlowCheckerTaskRequestDTO workFlowCheckerTaskRequestDTO = WorkFlowCheckerTaskRequestDTO.builder()
				.status(WorkStatus.COMPLETED).build();
		return objectMapper.writeValueAsString(workFlowCheckerTaskRequestDTO);
	}

}
