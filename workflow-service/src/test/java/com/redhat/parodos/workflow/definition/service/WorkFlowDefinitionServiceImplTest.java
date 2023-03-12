package com.redhat.parodos.workflow.definition.service;

import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowCheckerMappingDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.parameter.WorkFlowParameter;
import com.redhat.parodos.workflow.parameter.WorkFlowParameterType;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import com.redhat.parodos.workflow.task.WorkFlowTask;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class WorkFlowDefinitionServiceImplTest {

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowWorkRepository workFlowWorkRepository;

	private WorkFlowCheckerMappingDefinitionRepository workFlowCheckerMappingDefinitionRepository;

	private WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	@BeforeEach
	public void initEach() {
		this.workFlowDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = Mockito.mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowWorkRepository = Mockito.mock(WorkFlowWorkRepository.class);
		this.workFlowCheckerMappingDefinitionRepository = Mockito
				.mock(WorkFlowCheckerMappingDefinitionRepository.class);
		this.workFlowDefinitionService = getWorkflowService();
	}

	@Test
	public void simpleSaveTest() {
		String workFlowName = "test";
		String workFlowTaskName = "testTask";
		// given
		WorkFlowDefinition workFlowDefinition = this.sampleWorkFlowDefinition(workFlowName);
		WorkFlowTask workFlowTask = Mockito.mock(WorkFlowTask.class);
		WorkFlowTaskParameter workFlowTaskParameter = WorkFlowTaskParameter.builder().key("key").description("the key")
				.optional(false).type(WorkFlowTaskParameterType.URL).build();
		Mockito.when(workFlowTask.getName()).thenReturn(workFlowTaskName);
		Mockito.when(workFlowTask.getWorkFlowTaskParameters()).thenReturn(List.of(workFlowTaskParameter));
		WorkFlowTaskDefinition workFlowTaskDefinition = this.sampleWorkFlowTaskDefinition(workFlowDefinition,
				workFlowTaskName, workFlowTaskParameter);
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(workFlowTaskDefinition));
		Mockito.when(this.workFlowTaskDefinitionRepository.save(any())).thenReturn(workFlowTaskDefinition);
		Mockito.when(this.workFlowDefinitionRepository.save(any())).thenReturn(workFlowDefinition);

		// when
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = this.workFlowDefinitionService.save(workFlowName,
				WorkFlowType.ASSESSMENT, Collections.emptyList(), List.of(workFlowTask),
				WorkFlowProcessingType.SEQUENTIAL);

		// then
		assertNotNull(workFlowDefinitionResponseDTO);
		assertNotNull(workFlowDefinitionResponseDTO.getId());
		assertEquals(workFlowDefinitionResponseDTO.getName(), workFlowName);

		ArgumentCaptor<WorkFlowDefinition> argument = ArgumentCaptor.forClass(WorkFlowDefinition.class);
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(2)).save(argument.capture());
		assertEquals(argument.getValue().getName(), workFlowName);
		assertEquals(argument.getValue().getType(), WorkFlowType.ASSESSMENT.toString());
		assertEquals(argument.getValue().getProcessingType(), WorkFlowProcessingType.SEQUENTIAL.toString());
		assertEquals(argument.getValue().getNumberOfWorks(), 1);
		assertEquals(argument.getValue().getWorkFlowTaskDefinitions().size(), 1);
		assertEquals(argument.getValue().getWorkFlowTaskDefinitions().stream().findFirst().get().getName(), "testTask");
	}

	@Test
	public void getWorkFlowDefinitionByIdWithValidUUIDTest() {
		// given
		WorkFlowDefinition wfDefinition = this.sampleWorkFlowDefinition("test");

		UUID uuid = UUID.randomUUID();
		Mockito.when(this.workFlowDefinitionRepository.findById(uuid)).thenReturn(Optional.of(wfDefinition));

		// when
		WorkFlowDefinitionResponseDTO wkDTO = this.workFlowDefinitionService.getWorkFlowDefinitionById(uuid);

		// then
		assertNotNull(wkDTO);
		assertEquals(wkDTO.getName(), "test");

		ArgumentCaptor<UUID> argument = ArgumentCaptor.forClass(UUID.class);
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1)).findById(argument.capture());
		assertEquals(argument.getValue(), uuid);
	}

	@Test
	public void getWorkFlowDefinitionByIdWithInvalidUUIDTest() {
		// given
		WorkFlowDefinition wfDefinition = this.sampleWorkFlowDefinition("test");

		UUID uuid = UUID.randomUUID();
		Mockito.when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.empty());

		// when
		Exception exception = assertThrows(RuntimeException.class, () -> {
			// WorkFlowDefinitionResponseDTO wkDTO =
			// wkService.getWorkFlowDefinitionById(uuid);
			this.workFlowDefinitionService.getWorkFlowDefinitionById(uuid);
		});

		// then
		assertEquals(exception.getMessage(), String.format("Workflow definition id %s not found", uuid));
	}

	@Test
	public void getWorkFlowDefinitionByNameWithValidNameTest() {
		// given
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(any()))
				.thenReturn(sampleWorkFlowDefinition("test"));

		// when
		WorkFlowDefinitionResponseDTO result = this.workFlowDefinitionService.getWorkFlowDefinitionByName("test");

		// then
		assertNotNull(result);
		assertEquals(result.getName(), "test");

		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1)).findFirstByName(any());
	}

	@Test
	public void getWorkFlowDefinitionByNameWithInvalidNameTest() {
		// given
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(null);

		Exception exception = assertThrows(RuntimeException.class, () -> {
			this.workFlowDefinitionService.getWorkFlowDefinitionByName("test");
		});

		// // when
		// WorkFlowDefinitionResponseDTO result = ;
		//
		// // then
		// assertNull(result);

		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1)).findFirstByName(any());
	}

	@Test
	public void getWorkFlowDefinitionWithoutData() {
		// given
		Mockito.when(this.workFlowDefinitionRepository.findByTypeIsNot(any())).thenReturn(new ArrayList<>());

		// when
		List<WorkFlowDefinitionResponseDTO> resultList = this.workFlowDefinitionService.getWorkFlowDefinitions();

		// then
		assertNotNull(resultList);
		assertEquals(resultList.size(), 0);

		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1)).findByTypeIsNot(any());
	}

	@Test
	public void getWorkFlowDefinitionsWithData() {
		// given
		Mockito.when(this.workFlowDefinitionRepository.findByTypeIsNot(WorkFlowType.CHECKER.name())).thenReturn(
				Arrays.asList(sampleWorkFlowDefinition("workFLowOne"), sampleWorkFlowDefinition("workFLowTwo")));

		// when
		List<WorkFlowDefinitionResponseDTO> workFlowDefinitionResponseDTOs = this.workFlowDefinitionService
				.getWorkFlowDefinitions();

		// then
		assertNotNull(workFlowDefinitionResponseDTOs);
		assertEquals(workFlowDefinitionResponseDTOs.size(), 2);
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1))
				.findByTypeIsNot(WorkFlowType.CHECKER.name());
	}

	@Test
	public void saveWorkFlowCheckerTestWithValidData() {
		// given
		WorkFlowTaskDefinition taskDefinition = this.sampleWorkflowTaskDefinition();
		Mockito.when(this.workFlowTaskDefinitionRepository.findFirstByName(any())).thenReturn(taskDefinition);

		WorkFlowDefinition wfDefinition = this.sampleWorkFlowDefinition("test-wf");
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName("test-wf")).thenReturn(wfDefinition);

		// WorkFlowDefinition nextWfDefinition = this.sampleWorkflowDefinition("next-wf");
		// Mockito.when(this.workFlowDefinitionRepository.findByName("next-wf"))
		// .thenReturn(Arrays.asList(nextWfDefinition));

		Mockito.when(workFlowCheckerMappingDefinitionRepository.findFirstByCheckWorkFlow(any())).thenReturn(null);

		// when
		this.workFlowDefinitionService.saveWorkFlowChecker("test", "test-wf",
				WorkFlowCheckerDTO.builder().cronExpression("10 * *").build());
		// then

		ArgumentCaptor<WorkFlowTaskDefinition> argument = ArgumentCaptor.forClass(WorkFlowTaskDefinition.class);
		Mockito.verify(this.workFlowTaskDefinitionRepository, Mockito.times(1)).save(argument.capture());

		assertEquals(argument.getValue().getName(), "test");
		WorkFlowCheckerMappingDefinition checkerDefinition = argument.getValue().getWorkFlowCheckerMappingDefinition();
		assertNotNull(checkerDefinition);

		assertNotNull(checkerDefinition.getCheckWorkFlow());
		assertEquals(checkerDefinition.getCheckWorkFlow().getName(), wfDefinition.getName());
		assertEquals(checkerDefinition.getCheckWorkFlow().getId(), wfDefinition.getId());

		assertEquals(checkerDefinition.getCheckWorkFlow().getId(), wfDefinition.getId());
		assertEquals(checkerDefinition.getTasks().get(0).getId(), taskDefinition.getId());
	}

	@Test
	public void saveWorkFlowCheckerTestWithInvalidData() {
		// @TODO not sure if not doing anything is the right thing to do here
		// given
		WorkFlowTaskDefinition taskDefinition = this.sampleWorkflowTaskDefinition();
		Mockito.when(this.workFlowTaskDefinitionRepository.findFirstByName(any())).thenReturn(null);

		// when
		this.workFlowDefinitionService.saveWorkFlowChecker("test", "test-wf",
				WorkFlowCheckerDTO.builder().cronExpression("10 * *").build());
		// then
		Mockito.verify(this.workFlowTaskDefinitionRepository, Mockito.times(0)).save(any());
	}

	private WorkFlowTaskDefinition sampleWorkflowTaskDefinition() {
		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder().name("test").build();
		workFlowTaskDefinition.setId(UUID.randomUUID());
		return workFlowTaskDefinition;
	}

	private WorkFlowDefinition sampleWorkFlowDefinition(String name) {
		WorkFlowParameter workFlowParameter = WorkFlowParameter.builder().key("key").description("the key")
				.optional(false).type(WorkFlowParameterType.TEXT).build();
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(name)
				.type(WorkFlowType.ASSESSMENT.name()).processingType(WorkFlowProcessingType.SEQUENTIAL.name())
				.parameters(WorkFlowDTOUtil.writeObjectValueAsString(List.of(workFlowParameter))).numberOfWorks(1)
				.build();
		workFlowDefinition.setId(UUID.randomUUID());
		return workFlowDefinition;
	}

	private WorkFlowTaskDefinition sampleWorkFlowTaskDefinition(WorkFlowDefinition workFlowDefinition,
			String workFlowTaskName, WorkFlowTaskParameter workFlowTaskParameter) {
		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder()
				.workFlowDefinition(workFlowDefinition).name(workFlowTaskName)
				.parameters(WorkFlowDTOUtil.writeObjectValueAsString(List.of(workFlowTaskParameter))).build();
		workFlowTaskDefinition.setId(UUID.randomUUID());
		return workFlowTaskDefinition;
	}

	private WorkFlowDefinitionServiceImpl getWorkflowService() {
		return new WorkFlowDefinitionServiceImpl(this.workFlowDefinitionRepository,
				this.workFlowTaskDefinitionRepository, this.workFlowCheckerMappingDefinitionRepository,
				this.workFlowWorkRepository, new ModelMapper());
	}

}
