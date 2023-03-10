package com.redhat.parodos.workflow.definition.service;

import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowCheckerDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import com.redhat.parodos.workflow.task.WorkFlowTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

class WorkFlowDefinitionServiceImplTest {

	private static final String _10 = "10 * *";

	private static final String NEXT_WF = "next-wf";

	private static final String TEST_WF = "test-wf";

	private static final String TEST_TASK = "testTask";

	private static final String TEST_DESCRIPTION = "test description";

	private static final String KEY = "key";

	private static final String TEST = "test";

	private WorkFlowDefinitionRepository wfDefinitionRepository;

	private WorkFlowCheckerDefinitionRepository workFlowCheckerDefinitionRepository;

	private WorkFlowTaskDefinitionRepository wfTaskDefinitionRepository;

	private WorkFlowDefinitionServiceImpl wkService;

	@BeforeEach
	public void initEach() {
		this.wfDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		this.wfTaskDefinitionRepository = Mockito.mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowCheckerDefinitionRepository = Mockito.mock(WorkFlowCheckerDefinitionRepository.class);
		this.wkService = getWorkflowService();
	}

	@Test
	public void simpleSaveTest() {
		// given

		WorkFlowDefinition wfDefinition = this.sampleWorkflowDefinition(TEST);

		Mockito.when(this.wfDefinitionRepository.save(any())).thenReturn(wfDefinition);

		WorkFlowTask testTask = Mockito.mock(WorkFlowTask.class);
		WorkFlowTaskParameter taskParameter = WorkFlowTaskParameter.builder().key(KEY).optional(false)
				.type(WorkFlowTaskParameterType.URL).description(KEY).build();
		Mockito.when(testTask.getWorkFlowTaskParameters()).thenReturn(Arrays.asList(taskParameter));

		// when
		@SuppressWarnings("serial")
		WorkFlowDefinitionResponseDTO res = this.wkService.save(TEST, TEST_DESCRIPTION, WorkFlowType.ASSESSMENT,
				new HashMap<>() {
					{
						put(TEST_TASK, testTask);
					}
				});

		// then
		assertNotNull(res);
		assertNotNull(res.getId());
		assertEquals(res.getName(), TEST);

		ArgumentCaptor<WorkFlowDefinition> argument = ArgumentCaptor.forClass(WorkFlowDefinition.class);
		Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).save(argument.capture());
		assertEquals(argument.getValue().getName(), TEST);
		assertEquals(argument.getValue().getDescription(), TEST_DESCRIPTION);
		assertEquals(argument.getValue().getType(), WorkFlowType.ASSESSMENT.toString());
		assertEquals(argument.getValue().getWorkFlowTaskDefinitions().size(), 1);
		assertEquals(argument.getValue().getWorkFlowTaskDefinitions().stream().findFirst().get().getName(), TEST_TASK);
	}

	@Test
	public void getWorkFlowDefinitionByIdWithValidUUIDTest() {
		// given
		WorkFlowDefinition wfDefinition = this.sampleWorkflowDefinition(TEST);

		UUID uuid = UUID.randomUUID();
		Mockito.when(this.wfDefinitionRepository.findById(uuid)).thenReturn(Optional.of(wfDefinition));

		// when
		WorkFlowDefinitionResponseDTO wkDTO = this.wkService.getWorkFlowDefinitionById(uuid);

		// then
		assertNotNull(wkDTO);
		assertEquals(wkDTO.getName(), TEST);

		ArgumentCaptor<UUID> argument = ArgumentCaptor.forClass(UUID.class);
		Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).findById(argument.capture());
		assertEquals(argument.getValue(), uuid);
	}

	@Test
	public void getWorkFlowDefinitionByIdWithInvalidUUIDTest() {

		UUID uuid = UUID.randomUUID();
		Mockito.when(this.wfDefinitionRepository.findById(any())).thenReturn(Optional.empty());

		// when
		Exception exception = assertThrows(RuntimeException.class, () -> {
			this.wkService.getWorkFlowDefinitionById(uuid);
		});

		// then
		assertEquals(exception.getMessage(), String.format("Workflow definition id %s not found", uuid));
	}

	@Test
	public void getWorkFlowDefinitionByNameWithValidNameTest() {
		// given
		Mockito.when(this.wfDefinitionRepository.findByName(any()))
				.thenReturn(Arrays.asList(sampleWorkflowDefinition(TEST), sampleWorkflowDefinition(TEST)));

		// when
		List<WorkFlowDefinitionResponseDTO> resultList = this.wkService.getWorkFlowDefinitionsByName(TEST);

		// then
		assertNotNull(resultList);
		assertEquals(resultList.size(), 2);
		assertEquals(resultList.get(0).getName(), TEST);

		Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).findByName(any());
	}

	@Test
	public void getWorkFlowDefinitionByNameWithInvalidNameTest() {
		// given
		Mockito.when(this.wfDefinitionRepository.findByName(any())).thenReturn(new ArrayList<WorkFlowDefinition>());

		// when
		List<WorkFlowDefinitionResponseDTO> resultList = this.wkService.getWorkFlowDefinitionsByName(TEST);

		// then
		assertNotNull(resultList);
		assertEquals(resultList.size(), 0);

		Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).findByName(any());
	}

	@Test
	public void getWorkFlowDefinitionWithoutData() {
		// given
		Mockito.when(this.wfDefinitionRepository.findAll()).thenReturn(new ArrayList<WorkFlowDefinition>());

		// when
		List<WorkFlowDefinitionResponseDTO> resultList = this.wkService.getWorkFlowDefinitions();

		// then
		assertNotNull(resultList);
		assertEquals(resultList.size(), 0);

		Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void getWorkFlowDefinitionWitData() {
		// given
		Mockito.when(this.wfDefinitionRepository.findAll())
				.thenReturn(Arrays.asList(sampleWorkflowDefinition(TEST), sampleWorkflowDefinition("alice")));

		// when
		List<WorkFlowDefinitionResponseDTO> resultList = this.wkService.getWorkFlowDefinitions();

		// then
		assertNotNull(resultList);
		assertEquals(resultList.size(), 2);

		Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void saveWorkFlowCheckerTestWithValidData() {
		// given
		WorkFlowTaskDefinition taskDefinition = this.sampleWorkflowTaskDefinition(TEST);
		Mockito.when(this.wfTaskDefinitionRepository.findFirstByName(any())).thenReturn(taskDefinition);

		WorkFlowDefinition wfDefinition = this.sampleWorkflowDefinition(TEST_WF);
		Mockito.when(this.wfDefinitionRepository.findByName(TEST_WF)).thenReturn(Arrays.asList(wfDefinition));

		WorkFlowDefinition nextWfDefinition = this.sampleWorkflowDefinition(NEXT_WF);
		Mockito.when(this.wfDefinitionRepository.findByName(NEXT_WF)).thenReturn(Arrays.asList(nextWfDefinition));

		Mockito.when(workFlowCheckerDefinitionRepository.findFirstByCheckWorkFlow(any())).thenReturn(null);

		// when
		this.wkService.saveWorkFlowChecker(TEST, TEST_WF,
				WorkFlowCheckerDTO.builder().nextWorkFlowName(NEXT_WF).cronExpression(_10).build());
		// then

		ArgumentCaptor<WorkFlowTaskDefinition> argument = ArgumentCaptor.forClass(WorkFlowTaskDefinition.class);
		Mockito.verify(this.wfTaskDefinitionRepository, Mockito.times(1)).save(argument.capture());

		assertEquals(argument.getValue().getName(), TEST);
		WorkFlowCheckerDefinition checkerDefinition = argument.getValue().getWorkFlowCheckerDefinition();
		assertNotNull(checkerDefinition);

		assertNotNull(checkerDefinition.getCheckWorkFlow());
		assertEquals(checkerDefinition.getCheckWorkFlow().getName(), wfDefinition.getName());
		assertEquals(checkerDefinition.getCheckWorkFlow().getId(), wfDefinition.getId());

		assertNotNull(checkerDefinition.getNextWorkFlow());
		assertEquals(checkerDefinition.getNextWorkFlow().getName(), nextWfDefinition.getName());
		assertEquals(checkerDefinition.getNextWorkFlow().getId(), nextWfDefinition.getId());

		assertEquals(checkerDefinition.getCheckWorkFlow().getId(), wfDefinition.getId());
		assertEquals(checkerDefinition.getTasks().get(0).getId(), taskDefinition.getId());
	}

	@Test
	public void saveWorkFlowCheckerTestWithInvalidData() {
		// @TODO not sure if not doing anything is the right thing to do here
		// given
		Mockito.when(this.wfTaskDefinitionRepository.findFirstByName(any())).thenReturn(null);

		// when
		this.wkService.saveWorkFlowChecker(TEST, TEST_WF,
				WorkFlowCheckerDTO.builder().nextWorkFlowName(NEXT_WF).cronExpression(_10).build());
		// then
		Mockito.verify(this.wfTaskDefinitionRepository, Mockito.times(0)).save(any());
	}

	private WorkFlowTaskDefinition sampleWorkflowTaskDefinition(String name) {
		WorkFlowTaskDefinition taskDefinition = WorkFlowTaskDefinition.builder().name(name).build();
		taskDefinition.setId(UUID.randomUUID());
		assertNotNull(taskDefinition.getId());
		return taskDefinition;
	}

	private WorkFlowDefinition sampleWorkflowDefinition(String name) {
		WorkFlowDefinition wf = WorkFlowDefinition.builder().name(name).build();
		wf.setId(UUID.randomUUID());
		return wf;
	}

	private WorkFlowDefinitionServiceImpl getWorkflowService() {
		return new WorkFlowDefinitionServiceImpl(this.wfDefinitionRepository, this.wfTaskDefinitionRepository,
				this.workFlowCheckerDefinitionRepository, new ModelMapper());
	}

}
