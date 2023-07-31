package com.redhat.parodos.workflow.execution.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.enums.WorkFlowLogLevel;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecutionLog;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.task.log.dto.WorkFlowTaskLog;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class WorkFlowLogServiceImplTest {

	@Mock
	private WorkFlowRepository workFlowRepository;

	@Mock
	private WorkFlowTaskRepository workFlowTaskRepository;

	@Mock
	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowLogServiceImpl workFlowLogService;

	@BeforeEach
	public void init() {
		workFlowLogService = new WorkFlowLogServiceImpl(workFlowTaskRepository, workFlowTaskDefinitionRepository,
				workFlowRepository);
	}

	@Test
	void getLog_when_workflowExecutionLogIsFound_then_return_log() {
		UUID mainWorkflowExecutionId = UUID.randomUUID();
		WorkFlowDefinition workFlowDefinition = createTestWorkflowDefinition();
		WorkFlowTaskExecutionLog workFlowTaskExecutionLog = createTestTaskExecutionLog();
		when(workFlowTaskDefinitionRepository.findFirstByName(anyString()))
				.thenReturn(createTestTaskDefinition(workFlowDefinition));
		when(workFlowRepository.findFirstByMainWorkFlowExecutionIdAndWorkFlowDefinitionId(any(), any()))
				.thenReturn(createTestExecution(workFlowDefinition));
		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(any(), any())).thenReturn(
				List.of(createTestTaskExecution(UUID.randomUUID(), UUID.randomUUID(), workFlowTaskExecutionLog)));
		assertEquals("test-log", workFlowLogService.getLog(mainWorkflowExecutionId, "test-task"));
	}

	@Test
	void getLog_when_workflowTaskExecutionIsNotFound_then_throwException() {
		UUID mainWorkflowExecutionId = UUID.randomUUID();
		WorkFlowDefinition workFlowDefinition = createTestWorkflowDefinition();
		when(workFlowTaskDefinitionRepository.findFirstByName(anyString()))
				.thenReturn(createTestTaskDefinition(workFlowDefinition));
		when(workFlowRepository.findFirstByMainWorkFlowExecutionIdAndWorkFlowDefinitionId(any(), any()))
				.thenReturn(createTestExecution(workFlowDefinition));
		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(any(), any()))
				.thenReturn(List.of());
		assertThrows(ResourceNotFoundException.class,
				() -> workFlowLogService.getLog(mainWorkflowExecutionId, "test-task"));
	}

	@Test
	void getLog_when_workflowExecutionLogIsNotFound_then_returnEmpty() {
		UUID mainWorkflowExecutionId = UUID.randomUUID();
		WorkFlowDefinition workFlowDefinition = createTestWorkflowDefinition();
		when(workFlowTaskDefinitionRepository.findFirstByName(anyString()))
				.thenReturn(createTestTaskDefinition(workFlowDefinition));
		when(workFlowRepository.findFirstByMainWorkFlowExecutionIdAndWorkFlowDefinitionId(any(), any()))
				.thenReturn(createTestExecution(workFlowDefinition));
		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(any(), any()))
				.thenReturn(List.of(createTestTaskExecution(UUID.randomUUID(), UUID.randomUUID(), null)));
		assertEquals("", workFlowLogService.getLog(mainWorkflowExecutionId, "test-task"));
	}

	@Test
	void writeLog_when_workflowExecutionLogIsFound_then_return_save() {
		UUID mainWorkflowExecutionId = UUID.randomUUID();
		WorkFlowDefinition workFlowDefinition = createTestWorkflowDefinition();
		WorkFlowTaskExecutionLog workFlowTaskExecutionLog = createTestTaskExecutionLog();
		when(workFlowTaskDefinitionRepository.findFirstByName(anyString()))
				.thenReturn(createTestTaskDefinition(workFlowDefinition));
		when(workFlowRepository.findFirstByMainWorkFlowExecutionIdAndWorkFlowDefinitionId(any(), any()))
				.thenReturn(createTestExecution(workFlowDefinition));
		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(any(), any())).thenReturn(
				List.of(createTestTaskExecution(UUID.randomUUID(), UUID.randomUUID(), workFlowTaskExecutionLog)));
		workFlowLogService.writeLog(mainWorkflowExecutionId, "test-task",
				WorkFlowTaskLog.builder().workFlowLoglevel(WorkFlowLogLevel.INFO).logText("test-log1").build());

		ArgumentCaptor<WorkFlowTaskExecution> argument = ArgumentCaptor.forClass(WorkFlowTaskExecution.class);
		verify(workFlowTaskRepository, times(1)).save(argument.capture());
		assertThat(argument.getValue().getWorkFlowTaskExecutionLog().getLog(), startsWith("test-log\n"));
		assertThat(argument.getValue().getWorkFlowTaskExecutionLog().getLog(),
				endsWith("\u001B[32mINFO\u001B[39m test-log1"));

	}

	@Test
	void writeLog_when_workflowIsMain_then_return_save() {
		UUID mainWorkflowExecutionId = UUID.randomUUID();
		WorkFlowDefinition workFlowDefinition = createTestWorkflowDefinition();
		WorkFlowTaskExecutionLog workFlowTaskExecutionLog = createTestTaskExecutionLog();
		when(workFlowTaskDefinitionRepository.findFirstByName(anyString()))
				.thenReturn(createTestTaskDefinition(workFlowDefinition));
		when(workFlowRepository.findFirstByMainWorkFlowExecutionIdAndWorkFlowDefinitionId(any(), any()))
				.thenReturn(null);
		when(workFlowRepository.findById(mainWorkflowExecutionId))
				.thenReturn(Optional.of(createTestExecution(workFlowDefinition)));
		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(any(), any())).thenReturn(
				List.of(createTestTaskExecution(UUID.randomUUID(), UUID.randomUUID(), workFlowTaskExecutionLog)));
		workFlowLogService.writeLog(mainWorkflowExecutionId, "test-task",
				WorkFlowTaskLog.builder().workFlowLoglevel(WorkFlowLogLevel.INFO).logText("test-log1").build());

		ArgumentCaptor<WorkFlowTaskExecution> argument = ArgumentCaptor.forClass(WorkFlowTaskExecution.class);
		verify(workFlowTaskRepository, times(1)).save(argument.capture());
		assertThat(argument.getValue().getWorkFlowTaskExecutionLog().getLog(), startsWith("test-log\n"));
		assertThat(argument.getValue().getWorkFlowTaskExecutionLog().getLog(),
				endsWith("\u001B[32mINFO\u001B[39m test-log1"));

	}

	private WorkFlowDefinition createTestWorkflowDefinition() {
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name("test-workflow").build();
		workFlowDefinition.setId(UUID.randomUUID());
		return workFlowDefinition;
	}

	private WorkFlowTaskDefinition createTestTaskDefinition(WorkFlowDefinition workFlowDefinition) {
		return WorkFlowTaskDefinition.builder().workFlowDefinition(workFlowDefinition).name("test-task").build();
	}

	private WorkFlowTaskExecution createTestTaskExecution(UUID workflowExecutionId, UUID taskDefinitionId,
			WorkFlowTaskExecutionLog workFlowTaskExecutionLog) {
		return WorkFlowTaskExecution.builder().workFlowExecutionId(workflowExecutionId)
				.workFlowTaskDefinitionId(taskDefinitionId).status(WorkStatus.COMPLETED)
				.workFlowTaskExecutionLog(workFlowTaskExecutionLog).build();
	}

	private WorkFlowExecution createTestExecution(WorkFlowDefinition workFlowDefinition) {
		return WorkFlowExecution.builder().workFlowDefinition(workFlowDefinition).build();
	}

	private WorkFlowTaskExecutionLog createTestTaskExecutionLog() {
		return WorkFlowTaskExecutionLog.builder().workFlowTaskExecution(null).log("test-log").build();
	}

}
