package com.redhat.parodos.workflow.execution.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.redhat.parodos.common.exceptions.IllegalWorkFlowStateException;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.common.exceptions.UnregisteredWorkFlowException;
import com.redhat.parodos.common.exceptions.WorkFlowNotFoundException;
import com.redhat.parodos.common.exceptions.WorkFlowWrongTypeException;
import com.redhat.parodos.project.dto.response.ProjectResponseDTO;
import com.redhat.parodos.project.service.ProjectService;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.service.UserService;
import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkFlowContextResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkStatusResponseDTO;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionContext;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class WorkFlowServiceImplTest {

	private static final String TEST_WORKFLOW_NAME = "test-workflow";

	private static final String TEST_WORKFLOW_TASK_NAME = "test-workflow-task";

	public static final String TEST_ADDITIONAL_INFO_KEY = "test-additional-info-key";

	public static final String TEST_ADDITIONAL_INFO_VALUE = "test-additional-info-value";

	@Mock
	private ProjectService projectService;

	@Mock
	private UserService userService;

	@Mock
	private WorkFlowDelegate workFlowDelegate;

	@Mock
	private WorkFlowServiceDelegate workFlowServiceDelegate;

	@Mock
	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	@Mock
	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	@Mock
	private WorkFlowRepository workFlowRepository;

	@Mock
	private WorkFlowTaskRepository workFlowTaskRepository;

	@Mock
	private WorkFlowWorkRepository workFlowWorkRepository;

	@Mock
	private WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	private final MeterRegistry metricRegistry = new SimpleMeterRegistry();

	private WorkFlowServiceImpl workFlowService;

	@BeforeEach
	void initEach() {
		WorkFlowExecutor workFlowExecutor = new WorkFlowExecutorImpl(this.workFlowDelegate, workFlowRepository);
		this.workFlowService = new WorkFlowServiceImpl(this.projectService, this.userService,
				this.workFlowDefinitionService, this.workFlowDelegate, this.workFlowServiceDelegate,
				this.workFlowDefinitionRepository, this.workFlowTaskDefinitionRepository, this.workFlowRepository,
				this.workFlowTaskRepository, this.workFlowWorkRepository, this.metricRegistry, workFlowExecutor);
	}

	@Test
	@WithMockUser(username = "test-user")
	void executeTestWithNoValidWorkflow() {
		// given
		when(this.workFlowDelegate.getWorkFlowByName(any())).thenReturn(null);

		// when
		assertThat(
				assertThrows(WorkFlowNotFoundException.class,
						() -> this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId(UUID.randomUUID())
								.works(List.of()).workFlowName(TEST_WORKFLOW_NAME).build())).getMessage(),
				equalTo("workflow '%s' cannot be found!".formatted(TEST_WORKFLOW_NAME)));

		verify(this.workFlowDelegate, times(1)).getWorkFlowByName(any());
		verify(this.workFlowDelegate, times(0)).initWorkFlowContext(any(), any());
		verify(this.workFlowDefinitionRepository, times(0)).findFirstByName(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void executeWithDTOWithValidData() {
		// given
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		when(work.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
			{
				put("foo", "bar");
			}
		}));
		when(this.workFlowDefinitionRepository.findFirstByName(any()))
				.thenReturn(this.sampleWorkflowDefinition("test"));
		when(this.workFlowWorkRepository.findFirstByWorkDefinitionId(any())).thenReturn(null);
		when(this.workFlowDelegate.initWorkFlowContext(any(), any())).thenReturn(new WorkContext());
		when(this.workFlowDelegate.getWorkFlowByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.IN_PROGRESS).user(user)
				.build();
		workFlowExecution.setId(UUID.randomUUID());
		when(this.workFlowRepository.save(any())).thenReturn(workFlowExecution);
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = WorkFlowDefinitionResponseDTO.builder()
				.name(TEST_WORKFLOW_NAME).works(Set.of()).build();
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST_WORKFLOW_NAME))
				.thenReturn(workFlowDefinitionResponseDTO);
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);

		// when
		WorkReport report = this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId(UUID.randomUUID())
				.works(List.of()).workFlowName(TEST_WORKFLOW_NAME).build());

		// then
		assertNotNull(report);
		assertEquals("IN_PROGRESS", report.getStatus().toString());
		assertNull(report.getError());

		assertNotNull(report.getWorkContext());

		verify(this.workFlowDelegate, times(2)).getWorkFlowByName(any());
		verify(this.workFlowDelegate, times(1)).initWorkFlowContext(any(), any());
		verify(this.workFlowDefinitionRepository, times(2)).findFirstByName(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void executeWithDTOWithMissingInvokingExecutionContext() {
		// given
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		when(work.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
			{
				put("foo", "bar");
			}
		}));
		when(this.workFlowDefinitionRepository.findFirstByName(any()))
				.thenReturn(this.sampleWorkflowDefinition("test"));
		when(this.workFlowWorkRepository.findFirstByWorkDefinitionId(any())).thenReturn(null);
		when(this.workFlowDelegate.initWorkFlowContext(any(), any())).thenReturn(new WorkContext());
		when(this.workFlowDelegate.getWorkFlowByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		UUID invokingExecutionId = UUID.randomUUID();
		when(this.workFlowRepository.findById(invokingExecutionId)).thenReturn(Optional.empty());
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = WorkFlowDefinitionResponseDTO.builder()
				.name(TEST_WORKFLOW_NAME).works(Set.of()).build();
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST_WORKFLOW_NAME))
				.thenReturn(workFlowDefinitionResponseDTO);
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);

		// when
		// @formatter:off
		assertThat(assertThrows(ResourceNotFoundException.class,
				() -> this.workFlowService.execute(WorkFlowRequestDTO.builder()
					.projectId(UUID.randomUUID())
					.works(List.of())
					.workFlowName(TEST_WORKFLOW_NAME)
					.invokingExecutionId(invokingExecutionId)
					.build())).getMessage(), equalTo("Workflow execution with ID: " + invokingExecutionId + " not found"));
		// @formatter:on

		// then
		verify(this.workFlowDelegate, times(1)).getWorkFlowByName(any());
		verify(this.workFlowDelegate, times(1)).initWorkFlowContext(any(), any());
		verify(this.workFlowDefinitionRepository, times(1)).findFirstByName(any());
		verify(this.workFlowRepository, times(1)).findById(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void executeWithDTOWithEmptyInvokingExecutionContext() {
		// given
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		when(work.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
			{
				put("foo", "bar");
			}
		}));
		when(this.workFlowDefinitionRepository.findFirstByName(any()))
				.thenReturn(this.sampleWorkflowDefinition("test"));
		when(this.workFlowWorkRepository.findFirstByWorkDefinitionId(any())).thenReturn(null);
		when(this.workFlowDelegate.initWorkFlowContext(any(), any())).thenReturn(new WorkContext());
		when(this.workFlowDelegate.getWorkFlowByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.IN_PROGRESS).user(user)
				.build();
		workFlowExecution.setId(UUID.randomUUID());
		when(this.workFlowRepository.save(any())).thenReturn(workFlowExecution);
		UUID invokingExecutionId = UUID.randomUUID();

		WorkContext invokingWorkContext = new WorkContext();
		// @formatter:off
		WorkFlowExecution invokingWorkFlowExecution = WorkFlowExecution.builder()
				.status(WorkStatus.COMPLETED)
				.user(user)
				.workFlowExecutionContext(WorkFlowExecutionContext.builder().workContext(invokingWorkContext).build())
				.build();
		// @formatter:on
		when(this.workFlowRepository.findById(invokingExecutionId)).thenReturn(Optional.of(invokingWorkFlowExecution));
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = WorkFlowDefinitionResponseDTO.builder()
				.name(TEST_WORKFLOW_NAME).works(Set.of()).build();
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST_WORKFLOW_NAME))
				.thenReturn(workFlowDefinitionResponseDTO);
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);

		// when
		// @formatter:off
		WorkReport report = this.workFlowService.execute(WorkFlowRequestDTO.builder()
			.projectId(UUID.randomUUID())
			.works(List.of())
			.workFlowName(TEST_WORKFLOW_NAME)
			.invokingExecutionId(invokingExecutionId)
			.build());
		// @formatter:on

		// then
		assertThat(report, is(notNullValue()));
		assertThat(report.getStatus(), equalTo(WorkStatus.IN_PROGRESS));
		assertThat(report.getError(), is(nullValue()));
		assertThat(report.getWorkContext(), is(notNullValue()));
		assertThat(WorkContextUtils.getMainExecutionId(report.getWorkContext()), equalTo(workFlowExecution.getId()));

		verify(this.workFlowDelegate, times(2)).getWorkFlowByName(any());
		verify(this.workFlowDelegate, times(1)).initWorkFlowContext(any(), any());
		verify(this.workFlowDefinitionRepository, times(2)).findFirstByName(any());
		verify(this.workFlowRepository, times(2)).save(any());
		verify(this.workFlowRepository, times(2)).findById(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	@SuppressWarnings("unchecked")
	void executeWithDTOWithInvokingExecutionContext() {
		// given
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		when(work.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
			{
				put("foo", "bar");
			}
		}));
		when(this.workFlowDefinitionRepository.findFirstByName(any()))
				.thenReturn(this.sampleWorkflowDefinition("test"));
		when(this.workFlowWorkRepository.findFirstByWorkDefinitionId(any())).thenReturn(null);
		when(this.workFlowDelegate.initWorkFlowContext(any(), any())).thenReturn(new WorkContext());
		when(this.workFlowDelegate.getWorkFlowByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.IN_PROGRESS).user(user)
				.build();
		workFlowExecution.setId(UUID.randomUUID());
		when(this.workFlowRepository.save(any())).thenReturn(workFlowExecution);
		UUID invokingExecutionId = UUID.randomUUID();
		WorkContext invokingWorkContext = new WorkContext();
		Map<String, String> invokingArguments = Map.of("key1", "value1", "key2", "value2");
		WorkContextDelegate.write(invokingWorkContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ARGUMENTS, invokingArguments);

		// @formatter:off
		WorkFlowExecution invokingWorkFlowExecution = WorkFlowExecution.builder()
				.status(WorkStatus.COMPLETED)
				.user(user)
				.workFlowExecutionContext(WorkFlowExecutionContext.builder().workContext(invokingWorkContext).build())
				.build();
		// @formatter:on
		when(this.workFlowRepository.findById(invokingExecutionId)).thenReturn(Optional.of(invokingWorkFlowExecution));
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = WorkFlowDefinitionResponseDTO.builder()
				.name(TEST_WORKFLOW_NAME).works(Set.of()).build();
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST_WORKFLOW_NAME))
				.thenReturn(workFlowDefinitionResponseDTO);
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);

		// when
		// @formatter:off
		WorkReport report = this.workFlowService.execute(WorkFlowRequestDTO.builder()
				.projectId(UUID.randomUUID())
				.works(List.of())
				.workFlowName(TEST_WORKFLOW_NAME)
				.invokingExecutionId(invokingExecutionId)
				.build());
		// @formatter:on

		// then
		assertNotNull(report);
		assertEquals("IN_PROGRESS", report.getStatus().toString());
		assertNull(report.getError());
		assertThat(report.getWorkContext(), is(notNullValue()));
		assertThat(report.getWorkContext().getContext(), is(notNullValue()));
		assertThat((Map<String, String>) report.getWorkContext().getContext().get("WORKFLOW_EXECUTION_ARGUMENTS"),
				allOf(hasEntry("key1", "value1"), hasEntry("key2", "value2")));

		verify(this.workFlowDelegate, times(2)).getWorkFlowByName(any());
		verify(this.workFlowDelegate, times(1)).initWorkFlowContext(any(), any());
		verify(this.workFlowDefinitionRepository, times(2)).findFirstByName(any());
		verify(this.workFlowRepository, times(2)).save(any());
		verify(this.workFlowRepository, times(2)).findById(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void executeWithDTOWithNoMainWorkFlow() {
		// given
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		when(this.workFlowDefinitionRepository.findFirstByName(any()))
				.thenReturn(this.sampleWorkflowDefinition("test"));
		when(this.workFlowWorkRepository.findFirstByWorkDefinitionId(any()))
				.thenReturn(WorkFlowWorkDefinition.builder().build());
		when(this.workFlowDelegate.getWorkFlowByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);

		// when
		assertThat(
				assertThrows(WorkFlowWrongTypeException.class,
						() -> this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId(UUID.randomUUID())
								.works(List.of()).workFlowName(TEST_WORKFLOW_NAME).build())).getMessage(),
				equalTo("workflow '%s' is not main workflow!".formatted(TEST_WORKFLOW_NAME)));
		// then

		verify(this.workFlowDelegate, times(1)).getWorkFlowByName(any());
		verify(this.workFlowDelegate, times(0)).initWorkFlowContext(any(), any());
		verify(this.workFlowDefinitionRepository, times(1)).findFirstByName(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void executeWithDTOWithNoWorkFlowDefinition() {
		// given
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(null);
		when(this.workFlowDelegate.getWorkFlowByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);

		// when
		assertThat(
				assertThrows(UnregisteredWorkFlowException.class,
						() -> this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId(UUID.randomUUID())
								.works(List.of()).workFlowName(TEST_WORKFLOW_NAME).build())).getMessage(),
				equalTo("workflow '%s' is not registered!".formatted(TEST_WORKFLOW_NAME)));

		verify(this.workFlowDelegate, times(1)).getWorkFlowByName(any());
		verify(this.workFlowDelegate, never()).initWorkFlowContext(any(), any());
		verify(this.workFlowDefinitionRepository, times(1)).findFirstByName(any());
		verify(this.workFlowWorkRepository, never()).findFirstByWorkDefinitionId(any());
	}

	@Test
	void getWorkFlowByIDTestWithValidData() {
		// given
		UUID id = UUID.randomUUID();

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED).build();
		workFlowExecution.setId(id);
		when(this.workFlowRepository.findById(id)).thenReturn(Optional.of(workFlowExecution));

		// when
		WorkFlowExecution res = this.workFlowService.getWorkFlowById(id);

		// then
		assertNotNull(res);
		assertEquals(res.getId().toString(), id.toString());
		assertEquals(res.getStatus().toString(), "COMPLETED");
	}

	@Test
	void getWorkFlowByIDTestWithInvalidData() {
		// given
		UUID id = UUID.randomUUID();

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED).build();
		workFlowExecution.setId(id);
		when(this.workFlowRepository.findById(id)).thenReturn(Optional.empty());

		// when
		WorkFlowExecution res = this.workFlowService.getWorkFlowById(id);
		// then
		assertNull(res);
	}

	@Test
	void testSaveWorkflow() {
		// given
		UUID userId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		UUID workflowDefID = UUID.randomUUID();
		WorkFlowDefinition workflowDef = WorkFlowDefinition.builder().build();
		workflowDef.setId(workflowDefID);

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED).build();
		workFlowExecution.setId(UUID.randomUUID());

		WorkFlowExecution mainWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED).build();
		mainWorkFlowExecution.setId(UUID.randomUUID());

		when(this.workFlowRepository.save(any())).thenReturn(workFlowExecution);

		// when
		WorkFlowExecution res = this.workFlowService.saveWorkFlow(projectId, userId, workflowDef, WorkStatus.COMPLETED,
				mainWorkFlowExecution, "{}");

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowExecution> argument = ArgumentCaptor.forClass(WorkFlowExecution.class);
		verify(this.workFlowRepository, times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getProjectId().toString(), projectId.toString());
		assertEquals(argument.getValue().getWorkFlowDefinition().getId().toString(), workflowDefID.toString());
	}

	@Test
	void testUpdateWorkflowWithValidData() {
		// given
		UUID projectId = UUID.randomUUID();
		UUID workflowDefID = UUID.randomUUID();
		WorkFlowDefinition workflowDef = WorkFlowDefinition.builder().build();
		workflowDef.setId(workflowDefID);

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.projectId(projectId).workFlowDefinition(workflowDef).build();
		workFlowExecution.setId(UUID.randomUUID());
		when(this.workFlowRepository.save(any())).thenReturn(workFlowExecution);

		// when
		WorkFlowExecution res = this.workFlowService.updateWorkFlow(workFlowExecution);

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowExecution> argument = ArgumentCaptor.forClass(WorkFlowExecution.class);
		verify(this.workFlowRepository, times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getProjectId().toString(), projectId.toString());
		assertEquals(argument.getValue().getWorkFlowDefinition().getId().toString(), workflowDefID.toString());

		assertEquals(this.metricRegistry.get("workflow.executions").tag("status", "COMPLETED").counter().count(), 1);
		// No other tags are created under workflow.executions metrics
		assertEquals(this.metricRegistry.get("workflow.executions").counter().count(), 1);
		// check that IN_PROGRESS tag was not added
		assertThrows(MeterNotFoundException.class, () -> {
			this.metricRegistry.get("workflow.executions").tag("status", "IN_PROGRESS").counter();
		});
	}

	@Test
	void getWorkFlowTaskTestWithValidData() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDefID).workFlowExecutionId(wfExecutionID)
				.build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID,
				wfTaskDefID)).thenReturn(List.of(workFlowTaskExecution));
		// when

		WorkFlowTaskExecution res = this.workFlowService.getWorkFlowTask(wfExecutionID, wfTaskDefID);
		// then
		assertNotNull(res);
		assertEquals(res.getStatus().toString(), workFlowTaskExecution.getStatus().toString());
		assertEquals(res.getWorkFlowExecutionId().toString(), wfExecutionID.toString());
		assertEquals(res.getWorkFlowTaskDefinitionId().toString(),
				workFlowTaskExecution.getWorkFlowTaskDefinitionId().toString());
		assertEquals(res.getArguments(), workFlowTaskExecution.getArguments());
		assertEquals(res.getResults(), workFlowTaskExecution.getResults());
	}

	@Test
	void getWorkFlowTaskTestWithInvalidData() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();
		when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID,
				wfTaskDefID)).thenReturn(null);

		// when
		WorkFlowTaskExecution res = this.workFlowService.getWorkFlowTask(wfExecutionID, wfTaskDefID);
		// then
		assertNull(res);
		verify(this.workFlowTaskRepository, times(1))
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID, wfTaskDefID);
	}

	@Test
	void getWorkFlowTaskTestWithEmptyArray() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();
		when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID,
				wfTaskDefID)).thenReturn(new LinkedList<>());

		// when
		WorkFlowTaskExecution res = this.workFlowService.getWorkFlowTask(wfExecutionID, wfTaskDefID);
		// then
		assertNull(res);
		verify(this.workFlowTaskRepository, times(1))
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID, wfTaskDefID);
	}

	@Test
	void testSaveWorkFlowTaskExecution() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDefID).workFlowExecutionId(wfExecutionID)
				.build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		when(this.workFlowTaskRepository.save(any())).thenReturn(workFlowTaskExecution);
		// when
		WorkFlowTaskExecution res = this.workFlowService.saveWorkFlowTask("arguments", wfTaskDefID, wfExecutionID,
				WorkStatus.COMPLETED);

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowTaskExecution> argument = ArgumentCaptor.forClass(WorkFlowTaskExecution.class);
		verify(this.workFlowTaskRepository, times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getWorkFlowExecutionId().toString(), wfExecutionID.toString());
		assertEquals(argument.getValue().getWorkFlowTaskDefinitionId().toString(), wfTaskDefID.toString());
		assertEquals(argument.getValue().getArguments(), "arguments");
		assertNull(argument.getValue().getResults());
	}

	@Test
	void testUpdateWorkFlowTask() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDefID).workFlowExecutionId(wfExecutionID)
				.build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		when(this.workFlowTaskRepository.save(any())).thenReturn(workFlowTaskExecution);
		// when
		WorkFlowTaskExecution res = this.workFlowService.updateWorkFlowTask(workFlowTaskExecution);

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowTaskExecution> argument = ArgumentCaptor.forClass(WorkFlowTaskExecution.class);
		verify(this.workFlowTaskRepository, times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getWorkFlowExecutionId().toString(), wfExecutionID.toString());
		assertEquals(argument.getValue().getWorkFlowTaskDefinitionId().toString(), wfTaskDefID.toString());
		assertEquals(argument.getValue().getArguments(), "test");
		assertNotNull(argument.getValue().getResults(), "res");
	}

	@Test
	void testGetWorkStatusWithValidData() {
		String WORKFLOW_NAME = "testWorkFlow";
		UUID workFlowExecutionId = UUID.randomUUID();
		UUID workFlowDefinitionId = UUID.randomUUID();

		// given
		// workflow (main)
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(WORKFLOW_NAME).numberOfWorks(2)
				.build();
		workFlowDefinition.setId(workFlowDefinitionId);

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().workFlowDefinition(workFlowDefinition)
				.status(WorkStatus.IN_PROGRESS).build();
		workFlowExecution.setId(workFlowExecutionId);

		// subWorkflow1
		String SUB_WORKFLOW_1_NAME = "testSubWorkFlow1";
		UUID testSubWorkFlow1DefinitionId = UUID.randomUUID();
		UUID testSubWorkFlow1ExecutionId = UUID.randomUUID();
		// subWorkflow1Definition
		WorkFlowDefinition subWorkFlow1Definition = WorkFlowDefinition.builder().name(SUB_WORKFLOW_1_NAME)
				.numberOfWorks(1).build();
		subWorkFlow1Definition.setId(testSubWorkFlow1DefinitionId);
		// subWorkflow1Execution
		WorkFlowExecution subWorkFlow1Execution = WorkFlowExecution.builder().projectId(UUID.randomUUID())
				.status(WorkStatus.IN_PROGRESS).workFlowDefinition(subWorkFlow1Definition)
				.mainWorkFlowExecution(workFlowExecution).build();
		subWorkFlow1Execution.setId(testSubWorkFlow1ExecutionId);

		// subWorkflow1Task1
		String SUB_WORKFLOW_1_TASK_1_NAME = "testSubWorkFlow1Task1";
		UUID subWorkFlow1Task1DefinitionId = UUID.randomUUID();
		UUID subWorkFlow1Task1ExecutionId = UUID.randomUUID();
		// subWorkflow1Task1Definition
		WorkFlowTaskDefinition subWorkFlow1Task1Definition = WorkFlowTaskDefinition.builder()
				.name(SUB_WORKFLOW_1_TASK_1_NAME).build();
		subWorkFlow1Task1Definition.setId(subWorkFlow1Task1DefinitionId);
		// link subWorkflow1Task1Definition to subWorkflow1Definition
		subWorkFlow1Definition.setWorkFlowTaskDefinitions(List.of(subWorkFlow1Task1Definition));
		// subWorkflow1TaskExecution1
		WorkFlowTaskExecution subWorkFlow1Task1Execution = WorkFlowTaskExecution.builder()
				.status(WorkStatus.IN_PROGRESS).workFlowExecutionId(testSubWorkFlow1ExecutionId)
				.workFlowTaskDefinitionId(subWorkFlow1Task1DefinitionId).build();
		subWorkFlow1Task1Execution.setId(subWorkFlow1Task1ExecutionId);

		// workflowTask1
		String WORKFLOW_TASK_1_NAME = "testWorkFlowTask1";
		UUID workFlowTask1DefinitionId = UUID.randomUUID();
		UUID workFlowTask1ExecutionId = UUID.randomUUID();
		// workflowTask1Definition
		WorkFlowTaskDefinition workFlowTask1Definition = WorkFlowTaskDefinition.builder().name(WORKFLOW_TASK_1_NAME)
				.build();
		workFlowTask1Definition.setId(workFlowTask1DefinitionId);
		// workflowTask1Execution
		WorkFlowTaskExecution workFlowTask1Execution = WorkFlowTaskExecution.builder().status(WorkStatus.COMPLETED)
				.workFlowExecutionId(workFlowExecutionId).workFlowTaskDefinitionId(workFlowTask1DefinitionId).build();
		workFlowTask1Execution.setId(workFlowTask1ExecutionId);

		// link workflowTask1Definition to workFlowDefinition
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(workFlowTask1Definition));

		// when
		when(this.workFlowDefinitionRepository.findById(eq(workFlowDefinitionId)))
				.thenReturn(Optional.of(workFlowDefinition));

		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		when(this.workFlowDefinitionRepository.findById(eq(testSubWorkFlow1DefinitionId)))
				.thenReturn(Optional.of(subWorkFlow1Definition));

		when(this.workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(
				eq(testSubWorkFlow1DefinitionId), eq(workFlowExecution))).thenReturn(subWorkFlow1Execution);

		when(this.workFlowTaskDefinitionRepository.findById(eq(subWorkFlow1Task1DefinitionId)))
				.thenReturn(Optional.of(subWorkFlow1Task1Definition));

		when(this.workFlowTaskDefinitionRepository.findById(eq(workFlowTask1DefinitionId)))
				.thenReturn(Optional.of(workFlowTask1Definition));

		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(
				eq(testSubWorkFlow1ExecutionId), eq(subWorkFlow1Task1DefinitionId)))
						.thenReturn(List.of(subWorkFlow1Task1Execution));

		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(eq(workFlowExecutionId),
				eq(workFlowTask1DefinitionId))).thenReturn(List.of(workFlowTask1Execution));

		when(this.workFlowServiceDelegate.getWorkFlowAndWorksStatus(eq(workFlowExecution), eq(workFlowDefinition)))
				.thenReturn(List.of(
						WorkStatusResponseDTO.builder().name(SUB_WORKFLOW_1_NAME).type(WorkType.WORKFLOW)
								.status(WorkStatus.PENDING)
								.works(List.of(WorkStatusResponseDTO.builder().name(SUB_WORKFLOW_1_TASK_1_NAME)
										.type(WorkType.TASK).status(WorkStatus.PENDING).build()))
								.workExecution(subWorkFlow1Execution).numberOfWorks(1).build(),
						WorkStatusResponseDTO.builder().name(WORKFLOW_TASK_1_NAME).type(WorkType.TASK)
								.status(WorkStatus.COMPLETED).build()));
		// then
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = this.workFlowService
				.getWorkFlowStatus(workFlowExecutionId);

		// workflow (main)
		assertNotNull(workFlowStatusResponseDTO);
		assertEquals(workFlowExecution.getId(), workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(workFlowStatusResponseDTO.getWorkFlowName(), workFlowDefinition.getName());
		assertEquals(workFlowStatusResponseDTO.getStatus(), WorkStatus.IN_PROGRESS);
		assertEquals(workFlowStatusResponseDTO.getWorks().size(), 2);

		// subWorkflow1
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getType(), WorkType.WORKFLOW);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getName(), subWorkFlow1Definition.getName());
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getStatus(), WorkStatus.PENDING);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getWorks().size(), 1);

		// subWorkflow1Task1
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getWorks().get(0).getType(), WorkType.TASK);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getWorks().get(0).getName(),
				subWorkFlow1Task1Definition.getName());
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getWorks().get(0).getStatus(), WorkStatus.PENDING);
		assertNull(workFlowStatusResponseDTO.getWorks().get(0).getWorks().get(0).getWorks());

		// workflowTask1
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getType(), WorkType.TASK);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getName(), workFlowTask1Definition.getName());
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getStatus(), WorkStatus.COMPLETED);
		assertNull(workFlowStatusResponseDTO.getWorks().get(1).getWorks());
	}

	@Test
	void testGetWorkFlowStatusWithInvalidExecutionData() {
		// workflow (main)
		UUID workFlowExecutionId = UUID.randomUUID();

		// when
		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			this.workFlowService.getWorkFlowStatus(workFlowExecutionId);
		});

		verify(this.workFlowRepository, times(1)).findById(any());
		verify(this.workFlowWorkRepository, never()).findByWorkFlowDefinitionIdOrderByCreateDateAsc(any());
	}

	@Test
	void testGetWorkFlowStatusWithInvalidDefinitionData() {
		// workflow (main)
		UUID workFlowExecutionId = UUID.randomUUID();
		UUID workFlowDefinitionId = UUID.randomUUID();
		WorkFlowExecution workFlowExecution = mock(WorkFlowExecution.class);

		// when
		when(workFlowExecution.getId()).thenReturn(workFlowExecutionId);
		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		when(this.workFlowDefinitionRepository.findById(eq(workFlowDefinitionId))).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			this.workFlowService.getWorkFlowStatus(workFlowExecutionId);
		});

		verify(this.workFlowRepository, times(1)).findById(any());
		verify(this.workFlowServiceDelegate, never()).getWorkFlowAndWorksStatus(eq(workFlowExecution), any());
	}

	@Test
	void testGetWorkFlowStatusWithNonMainWorkFlowData() {
		// workflow
		UUID workFlowExecutionId = UUID.randomUUID();
		UUID workFlowDefinitionId = UUID.randomUUID();
		WorkFlowExecution workFlowExecution = mock(WorkFlowExecution.class);
		WorkFlowDefinition workFlowDefinition = mock(WorkFlowDefinition.class);

		// when
		when(workFlowExecution.getId()).thenReturn(workFlowExecutionId);
		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		when(this.workFlowDefinitionRepository.findById(eq(workFlowDefinitionId)))
				.thenReturn(Optional.of(workFlowDefinition));

		when(workFlowExecution.getMainWorkFlowExecution()).thenReturn(null);

		assertThrows(ResourceNotFoundException.class, () -> {
			this.workFlowService.getWorkFlowStatus(workFlowExecutionId);
		});

		verify(this.workFlowRepository, times(1)).findById(any());
		verify(this.workFlowServiceDelegate, never()).getWorkFlowAndWorksStatus(eq(workFlowExecution),
				eq(workFlowDefinition));
	}

	@Test
	void testGetWorkFlowStatusWhenSubWorkflowNotExecutedWithValidData() {
		String workFlowName = "testWorkFlow";
		UUID workFlowExecutionId = UUID.randomUUID();
		UUID workFlowDefinitionId = UUID.randomUUID();

		// workflow (main)
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(workFlowName).numberOfWorks(2)
				.build();
		workFlowDefinition.setId(workFlowDefinitionId);

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().workFlowDefinition(workFlowDefinition)
				.status(WorkStatus.IN_PROGRESS).build();
		workFlowExecution.setId(workFlowExecutionId);

		// subWorkflow1
		String SUB_WORKFLOW_1_NAME = "testSubWorkFlow1";
		UUID subWorkFlow1DefinitionId = UUID.randomUUID();
		UUID subWorkFlow1ExecutionId = UUID.randomUUID();
		// subWorkflow1Definition
		WorkFlowDefinition subWorkFlow1Definition = WorkFlowDefinition.builder().name(SUB_WORKFLOW_1_NAME)
				.numberOfWorks(1).build();
		subWorkFlow1Definition.setId(subWorkFlow1DefinitionId);
		// subWorkflow1Task1
		String SUB_WORKFLOW_1_TASK_1_NAME = "testSubWorkFlow1Task1";
		UUID subWorkFlow1Task1DefinitionId = UUID.randomUUID();
		// subWorkflow1Task1Definition
		WorkFlowTaskDefinition subWorkFlow1Task1Definition = WorkFlowTaskDefinition.builder()
				.name(SUB_WORKFLOW_1_TASK_1_NAME).build();
		subWorkFlow1Task1Definition.setId(subWorkFlow1Task1DefinitionId);
		// link subWorkflow1Task1 to subWorkflow1
		subWorkFlow1Definition.setWorkFlowTaskDefinitions(List.of(subWorkFlow1Task1Definition));

		// workflowTask1
		String WORKFLOW_TASK_1_NAME = "testWorkFlowTask1";
		UUID workFlowTask1DefinitionId = UUID.randomUUID();
		UUID workFlowTask1ExecutionId = UUID.randomUUID();
		// workflowTask1Definition
		WorkFlowTaskDefinition workFlowTask1Definition = WorkFlowTaskDefinition.builder().name(WORKFLOW_TASK_1_NAME)
				.build();
		workFlowTask1Definition.setId(workFlowTask1DefinitionId);
		// workflowTask1Execution
		WorkFlowTaskExecution workFlowTask1Execution = WorkFlowTaskExecution.builder().status(WorkStatus.COMPLETED)
				.workFlowExecutionId(workFlowExecutionId).workFlowTaskDefinitionId(workFlowTask1DefinitionId).build();
		workFlowTask1Execution.setId(workFlowTask1ExecutionId);
		// link workflow task definition 2 to main workFlow
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(workFlowTask1Definition));

		// when
		when(this.workFlowDefinitionRepository.findById(eq(workFlowDefinitionId)))
				.thenReturn(Optional.of(workFlowDefinition));

		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		when(this.workFlowDefinitionRepository.findById(eq(subWorkFlow1DefinitionId)))
				.thenReturn(Optional.of(subWorkFlow1Definition));

		when(this.workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(
				eq(subWorkFlow1DefinitionId), eq(workFlowExecution))).thenReturn(null);

		when(this.workFlowTaskDefinitionRepository.findById(eq(subWorkFlow1Task1DefinitionId)))
				.thenReturn(Optional.of(subWorkFlow1Task1Definition));

		when(this.workFlowTaskDefinitionRepository.findById(eq(workFlowTask1DefinitionId)))
				.thenReturn(Optional.of(workFlowTask1Definition));

		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(eq(subWorkFlow1ExecutionId),
				eq(subWorkFlow1Task1DefinitionId))).thenReturn(List.of());

		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(eq(workFlowExecutionId),
				eq(workFlowTask1DefinitionId))).thenReturn(List.of());

		when(this.workFlowServiceDelegate.getWorkFlowAndWorksStatus(eq(workFlowExecution), eq(workFlowDefinition)))
				.thenReturn(List.of(
						WorkStatusResponseDTO.builder().name(SUB_WORKFLOW_1_NAME).type(WorkType.WORKFLOW)
								.status(WorkStatus.PENDING).works(Collections.emptyList()).numberOfWorks(1).build(),
						WorkStatusResponseDTO.builder().name(WORKFLOW_TASK_1_NAME).type(WorkType.TASK)
								.status(WorkStatus.COMPLETED).build()));

		// then
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = this.workFlowService
				.getWorkFlowStatus(workFlowExecutionId);

		// main workflow
		assertNotNull(workFlowStatusResponseDTO);
		assertEquals(workFlowExecution.getId(), workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(workFlowStatusResponseDTO.getWorkFlowName(), workFlowDefinition.getName());
		assertEquals(workFlowStatusResponseDTO.getStatus(), WorkStatus.IN_PROGRESS);
		assertEquals(workFlowStatusResponseDTO.getWorks().size(), 2);

		// sub workflow 1
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getType(), WorkType.WORKFLOW);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getName(), subWorkFlow1Definition.getName());
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getStatus(), WorkStatus.PENDING);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getWorks().size(), 0);

		// workflow task 1
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getType(), WorkType.TASK);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getName(), workFlowTask1Definition.getName());
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getStatus(), WorkStatus.COMPLETED);
		assertNull(workFlowStatusResponseDTO.getWorks().get(1).getWorks());
	}

	@Test
	void testUpdateWorkFlowCheckerTaskStatusWithValidData() {
		// given
		// main workflow execution
		UUID workFlowExecutionId = UUID.randomUUID();
		// workflow checker task
		String workFlowCheckerTaskName = "testWorkFlowTask";
		String workFlowCheckerName = "testWorkFlowCheckerName";
		UUID workFlowCheckerDefinitionId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();

		// when
		// main workflow execution
		WorkFlowExecution mainWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.FAILED)
				.projectId(projectId).workFlowDefinition(WorkFlowDefinition.builder().build()).build();
		mainWorkFlowExecution.setId(workFlowExecutionId);
		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(mainWorkFlowExecution));

		// workflow checker definition
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(workFlowCheckerName).build();
		workFlowDefinition.setId(workFlowCheckerDefinitionId);
		// workflow checker task definition
		WorkFlowTaskDefinition workFlowCheckerTaskDefinition = WorkFlowTaskDefinition.builder()
				.workFlowDefinition(workFlowDefinition).name(workFlowCheckerTaskName).build();
		workFlowCheckerTaskDefinition.setId(UUID.randomUUID());
		when(this.workFlowTaskDefinitionRepository.findFirstByNameAndWorkFlowDefinitionType(any(), any()))
				.thenReturn(workFlowCheckerTaskDefinition);

		// workflow checker task execution
		WorkFlowExecution workFlowCheckerExecution = WorkFlowExecution.builder().status(WorkStatus.IN_PROGRESS)
				.projectId(projectId).workFlowDefinition(workFlowDefinition).build();
		workFlowCheckerExecution.setId(UUID.randomUUID());
		when(this.workFlowRepository.findByMainWorkFlowExecution(any())).thenReturn(List.of(workFlowCheckerExecution));

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkStatus.FAILED).workFlowTaskDefinitionId(workFlowCheckerTaskDefinition.getId())
				.workFlowExecutionId(workFlowCheckerExecution.getId()).build();
		workFlowTaskExecution.setId(UUID.randomUUID());
		when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(any(), any()))
				.thenReturn(List.of(workFlowTaskExecution));

		this.workFlowService.updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName,
				WorkStatus.COMPLETED);

		// then
		ArgumentCaptor<WorkFlowTaskExecution> argument = ArgumentCaptor.forClass(WorkFlowTaskExecution.class);
		verify(this.workFlowTaskRepository, times(1)).save(argument.capture());
		assertEquals(argument.getValue().getWorkFlowTaskDefinitionId(), workFlowCheckerTaskDefinition.getId());
		assertEquals(argument.getValue().getWorkFlowExecutionId(), workFlowCheckerExecution.getId());
		assertEquals(argument.getValue().getStatus(), WorkStatus.COMPLETED);
	}

	@Test
	void testUpdateWorkFlowCheckerTaskStatusWithInvalidExecutionData() {
		// given
		// main workflow execution
		UUID workFlowExecutionId = UUID.randomUUID();
		// workflow checker task
		String workFlowCheckerTaskName = "testWorkFlowTask";

		// when
		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			this.workFlowService.updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName,
					WorkStatus.COMPLETED);
		});

		verify(this.workFlowTaskRepository, times(0)).save(any());
	}

	@Test
	void testUpdateWorkFlowCheckerTaskStatusWithInvalidTaskData() {
		// given
		// main workflow execution
		UUID workFlowExecutionId = UUID.randomUUID();
		// workflow checker task
		String workFlowCheckerTaskName = "testWorkFlowTask";

		// when
		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(
				Optional.of(WorkFlowExecution.builder().status(WorkStatus.FAILED).projectId(UUID.randomUUID())
						.workFlowDefinition(WorkFlowDefinition.builder().build()).build()));

		when(this.workFlowTaskDefinitionRepository.findFirstByNameAndWorkFlowDefinitionType(any(), any()))
				.thenReturn(null);

		// then
		assertThrows(ResourceNotFoundException.class, () -> {
			this.workFlowService.updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName,
					WorkStatus.COMPLETED);
		});

		verify(this.workFlowTaskRepository, never()).save(any());

	}

	@Test
	public void testGetWorkflowParametersWithWorkflowOptions() {
		// given
		UUID workFlowExecutionId = UUID.randomUUID();
		WorkFlowExecution workFlowExecution = mock(WorkFlowExecution.class);
		when(workFlowExecution.getId()).thenReturn(workFlowExecutionId);
		WorkFlowExecutionContext executionContext = mock(WorkFlowExecutionContext.class);
		when(workFlowExecution.getWorkFlowExecutionContext()).thenReturn(executionContext);
		WorkContext workContext = new WorkContext();
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.WORKFLOW_OPTIONS,
				Map.of("newOptions", List.of(new WorkFlowOption.Builder("test-id", "test-workflow").build())));
		when(executionContext.getWorkContext()).thenReturn(workContext);

		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		// when
		WorkFlowContextResponseDTO workflowParameters = this.workFlowService.getWorkflowParameters(workFlowExecutionId,
				List.of(WorkContextDelegate.Resource.WORKFLOW_OPTIONS));

		// then
		verify(this.workFlowRepository, times(1)).findById(any());
		assertNotNull(workflowParameters);
		assertNotNull(workflowParameters.getWorkFlowOptions());
		assertEquals(workFlowExecutionId, workflowParameters.getWorkFlowExecutionId());
		List<WorkFlowOption> newOptions = workflowParameters.getWorkFlowOptions().getNewOptions();
		assertNotNull(newOptions);
		assertNull(workflowParameters.getWorkFlowOptions().getUpgradeOptions());
		assertNull(workflowParameters.getWorkFlowOptions().getCurrentVersion());
		assertEquals(1, newOptions.size());
		assertEquals("test-workflow", newOptions.get(0).getWorkFlowName());
	}

	@Test
	public void testGetWorkflowParametersWithoutWorkflowOptions() {
		// given
		UUID workFlowExecutionId = UUID.randomUUID();
		WorkFlowExecution workFlowExecution = mock(WorkFlowExecution.class);
		when(workFlowExecution.getId()).thenReturn(workFlowExecutionId);
		WorkFlowExecutionContext executionContext = mock(WorkFlowExecutionContext.class);
		when(workFlowExecution.getWorkFlowExecutionContext()).thenReturn(executionContext);
		when(executionContext.getWorkContext()).thenReturn(new WorkContext());

		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		// when
		WorkFlowContextResponseDTO workflowParameters = this.workFlowService.getWorkflowParameters(workFlowExecutionId,
				List.of(WorkContextDelegate.Resource.WORKFLOW_OPTIONS));

		// then
		verify(this.workFlowRepository, times(1)).findById(any());
		assertNotNull(workflowParameters);
		assertNotNull(workflowParameters.getWorkFlowOptions());
		assertEquals(workFlowExecutionId, workflowParameters.getWorkFlowExecutionId());
		assertNull(workflowParameters.getWorkFlowOptions().getNewOptions());
	}

	@Test
	@WithMockUser(username = "test-user")
	void getWorkFlowsByProjectId_when_projectIsFound_then_returnWorkFlowStatus() {
		String workName = "test-workflow";
		UUID projectId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		User user = User.builder().username("test-user").build();
		user.setId(userId);
		UUID workflowExecutionId = UUID.randomUUID();
		WorkFlowDefinition workFlowDefinition = sampleWorkflowDefinition(workName);
		WorkContext workContext = new WorkContext();
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ADDITIONAL_INFO,
				Map.of(TEST_ADDITIONAL_INFO_KEY, TEST_ADDITIONAL_INFO_VALUE));
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().projectId(projectId).user(user)
				.status(WorkStatus.COMPLETED).workFlowDefinition(workFlowDefinition)
				.workFlowExecutionContext(WorkFlowExecutionContext.builder().workContext(workContext).build()).build();
		workFlowExecution.setId(workflowExecutionId);
		WorkFlowExecution originalWorkFlowExecution = new WorkFlowExecution();
		originalWorkFlowExecution.setId(UUID.randomUUID());
		workFlowExecution.setOriginalWorkFlowExecution(originalWorkFlowExecution);

		when(workFlowRepository.findAllByProjectId(projectId)).thenReturn(List.of(workFlowExecution));
		when(projectService.getProjectByIdAndUserId(eq(projectId), eq(userId)))
				.thenReturn(List.of(ProjectResponseDTO.builder().id(projectId).name("test-project").build()));
		when(userService.getUserEntityByUsername("test-user")).thenReturn(user);
		when(workFlowDefinitionService.getWorkFlowDefinitionById(any()))
				.thenReturn(WorkFlowDefinitionResponseDTO.builder().name("test").build());

		List<WorkFlowResponseDTO> workFlowsByProjectId = workFlowService.getWorkFlowsByProjectId(projectId);

		assertThat(workFlowsByProjectId, hasSize(1));
		assertThat(workFlowsByProjectId.get(0), hasProperty("additionalInfos", hasSize(1)));
		assertThat(workFlowsByProjectId.get(0).getAdditionalInfos().get(0),
				equalTo(new WorkFlowResponseDTO.AdditionalInfo(TEST_ADDITIONAL_INFO_KEY, TEST_ADDITIONAL_INFO_VALUE)));
		assertThat(workFlowsByProjectId.get(0), hasProperty("workStatus", equalTo(WorkStatus.COMPLETED)));

	}

	@Test
	@WithMockUser(username = "test-user")
	void getWorkFlows_when_projectIsFound_then_returnWorkFlowStatus() {
		UUID userId = UUID.randomUUID();
		User user = User.builder().username("test-user").build();
		user.setId(userId);
		UUID project1Id = UUID.randomUUID();
		UUID project2Id = UUID.randomUUID();
		UUID workflowExecution1Id = UUID.randomUUID();
		UUID workflowExecution2Id = UUID.randomUUID();

		WorkFlowExecution workFlowExecution1 = WorkFlowExecution.builder().projectId(project1Id).user(user)
				.workFlowDefinition(WorkFlowDefinition.builder().build()).status(WorkStatus.COMPLETED).build();
		workFlowExecution1.setId(workflowExecution1Id);
		WorkFlowExecution workFlowExecution2 = WorkFlowExecution.builder().projectId(project2Id).user(user)
				.workFlowDefinition(WorkFlowDefinition.builder().build()).status(WorkStatus.FAILED).build();
		workFlowExecution2.setId(workflowExecution2Id);

		when(workFlowRepository.findAllByProjectId(project1Id)).thenReturn(List.of(workFlowExecution1));
		when(workFlowRepository.findAllByProjectId(project2Id)).thenReturn(List.of(workFlowExecution2));
		when(projectService.getProjectsByUserId(userId))
				.thenReturn(List.of(ProjectResponseDTO.builder().id(project1Id).name("test-project1").build(),
						ProjectResponseDTO.builder().id(project2Id).name("test-project2").build()));
		when(userService.getUserEntityByUsername("test-user")).thenReturn(user);
		when(workFlowDefinitionService.getWorkFlowDefinitionById(any()))
				.thenReturn(WorkFlowDefinitionResponseDTO.builder().name("test").build());

		List<WorkFlowResponseDTO> workFlows = workFlowService.getWorkFlows();

		assertThat(workFlows, hasSize(2));
		assertThat(workFlows, contains(hasProperty("workStatus", equalTo(WorkStatus.COMPLETED)),
				hasProperty("workStatus", equalTo(WorkStatus.FAILED))));
	}

	@Test
	void getWorkFlowsByProjectId_when_projectIsNotFound_then_returnException() {
		UUID projectId = UUID.randomUUID();

		when(projectService.getProjectById(projectId)).thenReturn(null);

		assertThrows(RuntimeException.class, () -> workFlowService.getWorkFlowsByProjectId(projectId));
	}

	/////////////////////////////////////////////////////////

	@Test
	@WithMockUser(username = "test-user")
	void restartWithNotExistingExecution() {
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		// when
		when(this.workFlowRepository.findById(any())).thenReturn(Optional.empty());
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);

		// then
		UUID executionID = UUID.randomUUID();
		assertThat(assertThrows(ResourceNotFoundException.class, () -> this.workFlowService.restart(executionID))
				.getMessage(), equalTo("Workflow execution with ID: " + executionID + " not found"));

		verify(this.workFlowRepository, times(0)).save(any());
		verify(this.workFlowRepository, times(1)).findById(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void restartNestedWorkflowExecution() {
		// given
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		UUID executionID = UUID.randomUUID();
		WorkContext invokingWorkContext = new WorkContext();
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(TEST_WORKFLOW_NAME).build();
		// when
		// @formatter:off
		WorkFlowExecution invokingWorkFlowExecution = WorkFlowExecution.builder()
				.status(WorkStatus.COMPLETED)
				.user(user)
				.workFlowDefinition(workFlowDefinition)
				.mainWorkFlowExecution(WorkFlowExecution.builder().build())
				.workFlowExecutionContext(WorkFlowExecutionContext.builder().workContext(invokingWorkContext).build())
				.build();
		invokingWorkFlowExecution.setId(executionID);
		// @formatter:on
		when(this.workFlowRepository.findById(executionID)).thenReturn(Optional.of(invokingWorkFlowExecution));
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);
		// then
		assertThat(
				assertThrows(IllegalWorkFlowStateException.class, () -> this.workFlowService.restart(executionID))
						.getMessage(),
				equalTo((String.format("workflow id: %s from workflow name: %s is an inner workflow!", executionID,
						workFlowDefinition.getName()))));

		verify(this.workFlowRepository, times(0)).save(any());
		verify(this.workFlowRepository, times(1)).findById(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void restartWorkflowExecutionWithNullWorkflowExecutionContext() {
		// given
		Work work = mock(Work.class);
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		UUID executionID = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(TEST_WORKFLOW_NAME)
				.fallbackWorkFlowDefinition(WorkFlowDefinition.builder().name("Fallback_" + TEST_WORKFLOW_NAME).build())
				.build();

		WorkFlowExecution restartedWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).projectId(projectId).build();
		restartedWorkFlowExecution.setId(UUID.randomUUID());
		WorkFlowExecution invokingWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).workFlowDefinition(workFlowDefinition).workFlowExecutionContext(null).build();
		invokingWorkFlowExecution.setId(executionID);

		// when

		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);
		when(this.workFlowRepository.findById(executionID)).thenReturn(Optional.of(invokingWorkFlowExecution));
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST_WORKFLOW_NAME))
				.thenReturn(WorkFlowDefinitionResponseDTO.builder().fallbackWorkflow("fallback").build());
		assertThat(
				assertThrows(IllegalWorkFlowStateException.class, () -> this.workFlowService.restart(executionID))
						.getMessage(),
				equalTo((String.format(
						"workflow id: %s from workflow name: %s has not Workflow Execution Context saved in the database, cannot restart it!",
						executionID, workFlowDefinition.getName()))));

		verify(this.workFlowRepository, times(0)).save(any());
		verify(this.workFlowRepository, times(1)).findById(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void restartWorkflowExecutionWithEmptyWorkContext() {
		// given
		Work work = mock(Work.class);
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		UUID executionID = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(TEST_WORKFLOW_NAME)
				.fallbackWorkFlowDefinition(WorkFlowDefinition.builder().name("Fallback_" + TEST_WORKFLOW_NAME).build())
				.build();

		WorkContext invokingWorkContext = new WorkContext();
		WorkFlowExecution restartedWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).projectId(projectId).build();
		restartedWorkFlowExecution.setId(UUID.randomUUID());
		WorkFlowExecution invokingWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).workFlowDefinition(workFlowDefinition)
				.workFlowExecutionContext(WorkFlowExecutionContext.builder().workContext(invokingWorkContext).build())
				.build();
		invokingWorkFlowExecution.setId(executionID);

		// when
		when(work.execute(invokingWorkContext))
				.thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
					{
						put("foo", "bar");
					}
				}));
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);
		when(this.workFlowRepository.save(any())).thenReturn(restartedWorkFlowExecution);
		when(this.workFlowRepository.findById(executionID)).thenReturn(Optional.of(invokingWorkFlowExecution));
		when(this.workFlowDelegate.getWorkFlowByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST_WORKFLOW_NAME))
				.thenReturn(WorkFlowDefinitionResponseDTO.builder().fallbackWorkflow("fallback").build());
		WorkReport report = this.workFlowService.restart(executionID);

		// then
		assertNotNull(report);
		assertEquals("IN_PROGRESS", report.getStatus().toString());
		assertNull(report.getError());
		assertNotNull(report.getWorkContext());
		// getWorkFlowByName is called in the execute method of the workflowExecutor
		verify(this.workFlowDelegate, times(1)).getWorkFlowByName(any());
		verify(this.workFlowRepository, times(2)).save(any());
		verify(this.workFlowRepository, times(2)).findById(any());
		verify(work, times(1)).execute(invokingWorkContext);
	}

	@Test
	@WithMockUser(username = "test-user")
	void restartWorkflowExecutionWithNullWorkContext() {
		// given
		Work work = mock(Work.class);
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		UUID executionID = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(TEST_WORKFLOW_NAME)
				.fallbackWorkFlowDefinition(WorkFlowDefinition.builder().name("Fallback_" + TEST_WORKFLOW_NAME).build())
				.build();

		WorkFlowExecution restartedWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).projectId(projectId).build();
		restartedWorkFlowExecution.setId(UUID.randomUUID());
		WorkFlowExecution invokingWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).workFlowDefinition(workFlowDefinition)
				.workFlowExecutionContext(WorkFlowExecutionContext.builder().workContext(null).build()).build();
		invokingWorkFlowExecution.setId(executionID);

		// when
		when(work.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
			{
				put("foo", "bar");
			}
		}));
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);
		when(this.workFlowRepository.save(any())).thenReturn(restartedWorkFlowExecution);
		when(this.workFlowRepository.findById(executionID)).thenReturn(Optional.of(invokingWorkFlowExecution));
		when(this.workFlowDelegate.getWorkFlowByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST_WORKFLOW_NAME))
				.thenReturn(WorkFlowDefinitionResponseDTO.builder().fallbackWorkflow("fallback").build());
		WorkReport report = this.workFlowService.restart(executionID);

		// then
		assertNotNull(report);
		assertEquals("IN_PROGRESS", report.getStatus().toString());
		assertNull(report.getError());
		assertNotNull(report.getWorkContext());
		// getWorkFlowByName is called in the execute method of the workflowExecutor
		verify(this.workFlowDelegate, times(1)).getWorkFlowByName(any());
		verify(this.workFlowRepository, times(2)).save(any());
		verify(this.workFlowRepository, times(2)).findById(any());
		verify(work, times(1)).execute(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void restartWithExistingExecutionArguments() {
		// given
		Work work = mock(Work.class);
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		UUID executionID = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(TEST_WORKFLOW_NAME)
				.fallbackWorkFlowDefinition(WorkFlowDefinition.builder().name("Fallback_" + TEST_WORKFLOW_NAME).build())
				.build();

		WorkContext invokingWorkContext = new WorkContext();
		WorkContextDelegate.write(invokingWorkContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				TEST_WORKFLOW_NAME, WorkContextDelegate.Resource.ARGUMENTS, Map.of("WORKFLOW_ARG", "argWorkflow"));
		WorkContextDelegate.write(invokingWorkContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
				TEST_WORKFLOW_TASK_NAME, WorkContextDelegate.Resource.ARGUMENTS,
				Map.of("WORKFLOW_TASK_ARG", "argWorkflowTASK"));
		WorkFlowExecution restartedWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).projectId(projectId).build();
		restartedWorkFlowExecution.setId(UUID.randomUUID());
		WorkFlowExecution invokingWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).workFlowDefinition(workFlowDefinition)
				.workFlowExecutionContext(WorkFlowExecutionContext.builder().workContext(invokingWorkContext).build())
				.build();
		invokingWorkFlowExecution.setId(executionID);

		// when
		when(work.execute(invokingWorkContext))
				.thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
					{
						put("foo", "bar");
					}
				}));
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);
		when(this.workFlowRepository.save(any())).thenReturn(restartedWorkFlowExecution);
		when(this.workFlowRepository.findById(executionID)).thenReturn(Optional.of(invokingWorkFlowExecution));
		when(this.workFlowDelegate.getWorkFlowByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST_WORKFLOW_NAME))
				.thenReturn(WorkFlowDefinitionResponseDTO.builder().fallbackWorkflow("fallback").build());
		WorkReport report = this.workFlowService.restart(executionID);

		// then
		assertNotNull(report);
		assertEquals("IN_PROGRESS", report.getStatus().toString());
		assertNull(report.getError());
		assertNotNull(report.getWorkContext());
		// getWorkFlowByName is called in the execute method of the workflowExecutor
		verify(this.workFlowDelegate, times(1)).getWorkFlowByName(any());
		verify(this.workFlowRepository, times(2)).save(any());
		verify(this.workFlowRepository, times(2)).findById(any());
		verify(work, times(1)).execute(invokingWorkContext);
	}

	///////

	/////////////////////////////////////////////////////////

	@Test
	@WithMockUser(username = "test-user")
	void executeFallbackWithNotExistingExecution() {
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		// when
		when(this.workFlowRepository.findById(any())).thenReturn(Optional.empty());
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);

		// then
		UUID executionID = UUID.randomUUID();
		assertThat(
				assertThrows(ResourceNotFoundException.class,
						() -> this.workFlowService.executeFallbackWorkFlow("", executionID)).getMessage(),
				equalTo(("Workflow execution with ID: " + executionID + " not found")));

		verify(this.workFlowRepository, times(0)).save(any());
		verify(this.workFlowRepository, times(1)).findById(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void executeFallbackWorkflowExecutionWithNullWorkflowExecutionContext() {
		// given
		Work work = mock(Work.class);
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		UUID executionID = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name("Fallback_" + TEST_WORKFLOW_NAME)
				.build();

		WorkContext invokingWorkContext = new WorkContext();
		WorkFlowExecution invokingWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).workFlowDefinition(workFlowDefinition).workFlowExecutionContext(null).build();
		invokingWorkFlowExecution.setId(executionID);
		WorkFlowExecution fallbackWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).projectId(projectId).build();
		fallbackWorkFlowExecution.setId(UUID.randomUUID());

		// when
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);
		when(this.workFlowRepository.save(any())).thenReturn(fallbackWorkFlowExecution);
		when(this.workFlowRepository.findById(executionID)).thenReturn(Optional.of(invokingWorkFlowExecution));
		when(this.workFlowDelegate.getWorkFlowByName("Fallback_" + TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		when(this.workFlowDefinitionRepository.findFirstByName(workFlowDefinition.getName()))
				.thenReturn(workFlowDefinition);
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName("Fallback_" + TEST_WORKFLOW_NAME))
				.thenReturn(WorkFlowDefinitionResponseDTO.builder().fallbackWorkflow("fallback").build());
		WorkReport report = this.workFlowService.executeFallbackWorkFlow(workFlowDefinition.getName(), executionID);

		// then
		assertNotNull(report);
		assertEquals("IN_PROGRESS", report.getStatus().toString());
		assertNull(report.getError());
		assertNotNull(report.getWorkContext());
		// getWorkFlowByName is called in the execute method of the workflowExecutor
		verify(this.workFlowDelegate, times(1)).getWorkFlowByName(any());
		verify(this.workFlowRepository, times(2)).save(any());
		verify(this.workFlowRepository, times(2)).findById(any());
		verify(work, times(1)).execute(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void executeFallbackWorkflowExecutionWithNullWorkContext() {
		// given
		Work work = mock(Work.class);
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		UUID executionID = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name("Fallback_" + TEST_WORKFLOW_NAME)
				.build();

		WorkFlowExecution fallbackWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).projectId(projectId).build();
		fallbackWorkFlowExecution.setId(UUID.randomUUID());
		WorkFlowExecution invokingWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).workFlowDefinition(workFlowDefinition)
				.workFlowExecutionContext(WorkFlowExecutionContext.builder().workContext(null).build()).build();
		invokingWorkFlowExecution.setId(executionID);

		// when
		when(work.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
			{
				put("foo", "bar");
			}
		}));
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);
		when(this.workFlowRepository.save(any())).thenReturn(fallbackWorkFlowExecution);
		when(this.workFlowRepository.findById(executionID)).thenReturn(Optional.of(invokingWorkFlowExecution));
		when(this.workFlowDelegate.getWorkFlowByName("Fallback_" + TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		when(this.workFlowDefinitionRepository.findFirstByName(workFlowDefinition.getName()))
				.thenReturn(workFlowDefinition);
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName("Fallback_" + TEST_WORKFLOW_NAME))
				.thenReturn(WorkFlowDefinitionResponseDTO.builder().fallbackWorkflow("fallback").build());
		WorkReport report = this.workFlowService.executeFallbackWorkFlow(workFlowDefinition.getName(), executionID);

		// then
		assertNotNull(report);
		assertEquals("IN_PROGRESS", report.getStatus().toString());
		assertNull(report.getError());
		assertNotNull(report.getWorkContext());
		// getWorkFlowByName is called in the execute method of the workflowExecutor
		verify(this.workFlowDelegate, times(1)).getWorkFlowByName(any());
		verify(this.workFlowRepository, times(2)).save(any());
		verify(this.workFlowRepository, times(2)).findById(any());
		verify(work, times(1)).execute(any());
	}

	@Test
	@WithMockUser(username = "test-user")
	void executeFallbackWithExistingExecutionArguments() {
		// given
		Work work = mock(Work.class);
		User user = User.builder().username("test-user").build();
		user.setId(UUID.randomUUID());
		UUID executionID = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name("Fallback_" + TEST_WORKFLOW_NAME)
				.build();

		WorkContext invokingWorkContext = new WorkContext();
		WorkContextDelegate.write(invokingWorkContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				TEST_WORKFLOW_NAME, WorkContextDelegate.Resource.ARGUMENTS, Map.of("WORKFLOW_ARG", "argWorkflow"));
		WorkContextDelegate.write(invokingWorkContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
				TEST_WORKFLOW_TASK_NAME, WorkContextDelegate.Resource.ARGUMENTS,
				Map.of("WORKFLOW_TASK_ARG", "argWorkflowTASK"));
		WorkFlowExecution fallbackWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).projectId(projectId).build();
		fallbackWorkFlowExecution.setId(UUID.randomUUID());
		WorkFlowExecution invokingWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.user(user).workFlowDefinition(workFlowDefinition)
				.workFlowExecutionContext(WorkFlowExecutionContext.builder().workContext(invokingWorkContext).build())
				.build();
		invokingWorkFlowExecution.setId(executionID);

		// when
		when(work.execute(invokingWorkContext))
				.thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
					{
						put("foo", "bar");
					}
				}));
		when(this.userService.getUserEntityByUsername("test-user")).thenReturn(user);
		when(this.workFlowRepository.save(any())).thenReturn(fallbackWorkFlowExecution);
		when(this.workFlowRepository.findById(executionID)).thenReturn(Optional.of(invokingWorkFlowExecution));
		when(this.workFlowDelegate.getWorkFlowByName("Fallback_" + TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName("Fallback_" + TEST_WORKFLOW_NAME))
				.thenReturn(WorkFlowDefinitionResponseDTO.builder().fallbackWorkflow("fallback").build());
		when(workFlowDefinitionRepository.findFirstByName(workFlowDefinition.getName())).thenReturn(workFlowDefinition);
		WorkReport report = this.workFlowService.executeFallbackWorkFlow(workFlowDefinition.getName(), executionID);

		// then
		assertNotNull(report);
		assertEquals("IN_PROGRESS", report.getStatus().toString());
		assertNull(report.getError());
		assertNotNull(report.getWorkContext());
		// getWorkFlowByName is called in the execute method of the workflowExecutor
		verify(this.workFlowDelegate, times(1)).getWorkFlowByName(any());
		verify(this.workFlowRepository, times(2)).save(any());
		verify(this.workFlowRepository, times(2)).findById(any());
		verify(work, times(1)).execute(invokingWorkContext);
	}

	///////

	private WorkFlowDefinition sampleWorkflowDefinition(String name) {
		WorkFlowDefinition wf = WorkFlowDefinition.builder().name(name).build();
		wf.setId(UUID.randomUUID());
		return wf;
	}

}
