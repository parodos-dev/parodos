/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.workflow.definition.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.workflow.definition.dto.WorkDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowPropertiesDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowCheckerMappingDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflows.workflow.WorkFlowPropertiesMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * unit test for WorkFlowDefinitionService
 *
 * @author Annel Ketcha (Github: anludke)
 * @author Richard Wang (Github: richardW98)
 */
class WorkFlowDefinitionServiceImplTest {

	private static final String _10 = "10 * *";

	private static final String TEST = "test";

	private static final String TEST_WF = "test-wf";

	private static final String TEST_TASK = "testTask";

	private static final String KEY = "key";

	private static final String KEY_DESCRIPTION = "The key";

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowWorkRepository workFlowWorkRepository;

	private WorkFlowCheckerMappingDefinitionRepository workFlowCheckerMappingDefinitionRepository;

	private WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	@BeforeEach
	public void initEach() {
		this.workFlowDefinitionRepository = mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowWorkRepository = mock(WorkFlowWorkRepository.class);
		this.workFlowCheckerMappingDefinitionRepository = mock(WorkFlowCheckerMappingDefinitionRepository.class);
		this.workFlowDefinitionService = getWorkflowDefinitionService();
	}

	@Test
	public void simpleSaveTest() {
		String workFlowName = "test";
		String workFlowTaskName = "testTask";
		// given
		WorkFlowDefinition workFlowDefinition = this.sampleWorkFlowDefinition(workFlowName);
		WorkFlowTask workFlowTask = mock(WorkFlowTask.class);
		WorkParameter workParameter = WorkParameter.builder().key("key").description("the key").optional(false)
				.type(WorkParameterType.URI).build();
		when(workFlowTask.getName()).thenReturn(workFlowTaskName);
		when(workFlowTask.getWorkFlowTaskParameters()).thenReturn(List.of(workParameter));
		WorkFlowTaskDefinition workFlowTaskDefinition = this.sampleWorkFlowTaskDefinition(workFlowDefinition,
				workFlowTaskName, workParameter);
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(workFlowTaskDefinition));
		when(this.workFlowTaskDefinitionRepository.save(any())).thenReturn(workFlowTaskDefinition);
		when(this.workFlowDefinitionRepository.save(any())).thenReturn(workFlowDefinition);
		WorkFlowPropertiesMetadata properties = WorkFlowPropertiesMetadata.builder().version("1.0.0").build();

		// when
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = this.workFlowDefinitionService.save(workFlowName,
				WorkFlowType.ASSESSMENT, properties, Collections.emptyList(), List.of(workFlowTask),
				WorkFlowProcessingType.SEQUENTIAL, null);

		// then
		assertNotNull(workFlowDefinitionResponseDTO);
		assertNotNull(workFlowDefinitionResponseDTO.getId());
		assertEquals(workFlowDefinitionResponseDTO.getName(), workFlowName);
		assertEquals(workFlowDefinitionResponseDTO.getProperties().getVersion(), "1.0.0");

		ArgumentCaptor<WorkFlowDefinition> argument = ArgumentCaptor.forClass(WorkFlowDefinition.class);
		verify(this.workFlowDefinitionRepository, times(2)).save(argument.capture());
		assertEquals(argument.getValue().getName(), workFlowName);
		assertEquals(argument.getValue().getType(), WorkFlowType.ASSESSMENT);
		assertEquals(argument.getValue().getProcessingType(), WorkFlowProcessingType.SEQUENTIAL);
		assertEquals(argument.getValue().getNumberOfWorks(), 1);
		assertEquals(argument.getValue().getWorkFlowTaskDefinitions().size(), 1);
		assertEquals(argument.getValue().getWorkFlowTaskDefinitions().stream().findFirst().get().getName(), TEST_TASK);
	}

	@Test
	public void simpleSaveTest_skipSave_when_sameWorkflowFoundInDB() {
		String workFlowName = "test";
		String workFlowTaskName = "testTask";
		// given
		WorkFlowDefinition workFlowDefinition = this.sampleWorkFlowDefinition(workFlowName);
		workFlowDefinition.setParameters("{}");
		WorkFlowTask workFlowTask = mock(WorkFlowTask.class);
		WorkParameter workParameter = WorkParameter.builder().key("key").description("the key").optional(false)
				.type(WorkParameterType.URI).build();
		when(workFlowTask.getName()).thenReturn(workFlowTaskName);
		when(workFlowTask.getWorkFlowTaskParameters()).thenReturn(List.of(workParameter));
		WorkFlowTaskDefinition workFlowTaskDefinition = this.sampleWorkFlowTaskDefinition(workFlowDefinition,
				workFlowTaskName, workParameter);
		workFlowTaskDefinition.setParameters("{}");
		workFlowTaskDefinition.setOutputs("[]");
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(workFlowTaskDefinition));
		when(this.workFlowTaskDefinitionRepository.save(any())).thenReturn(workFlowTaskDefinition);
		when(this.workFlowDefinitionRepository.save(any())).thenReturn(workFlowDefinition);
		when(this.workFlowDefinitionRepository.findFirstByName(anyString())).thenReturn(workFlowDefinition);
		when(this.workFlowTaskDefinitionRepository.findFirstByName(anyString())).thenReturn(workFlowTaskDefinition);
		// when
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = this.workFlowDefinitionService.save(workFlowName,
				WorkFlowType.ASSESSMENT, null, Collections.emptyList(), List.of(workFlowTask),
				WorkFlowProcessingType.SEQUENTIAL, null);

		// then
		assertNotNull(workFlowDefinitionResponseDTO);
		assertNotNull(workFlowDefinitionResponseDTO.getId());
		assertEquals(workFlowDefinitionResponseDTO.getName(), workFlowName);

		verify(this.workFlowDefinitionRepository, times(2)).save(workFlowDefinition);
		verify(this.workFlowTaskDefinitionRepository, never()).save(any());
	}

	@Test
	public void getWorkFlowDefinitionByIdWithValidUUIDTest() {
		// given
		WorkFlowDefinition workFlowDefinition = this.sampleWorkFlowDefinition(TEST);

		UUID uuid = UUID.randomUUID();
		when(this.workFlowDefinitionRepository.findById(uuid)).thenReturn(Optional.of(workFlowDefinition));

		// when
		WorkFlowDefinitionResponseDTO wkDTO = this.workFlowDefinitionService.getWorkFlowDefinitionById(uuid);

		// then
		assertNotNull(wkDTO);
		assertEquals(wkDTO.getName(), TEST);

		ArgumentCaptor<UUID> argument = ArgumentCaptor.forClass(UUID.class);
		verify(this.workFlowDefinitionRepository, times(1)).findById(argument.capture());
		assertEquals(argument.getValue(), uuid);
	}

	@Test
	public void getWorkFlowDefinitionByIdWithInvalidUUIDTest() {
		// given
		WorkFlowDefinition workFlowDefinition = this.sampleWorkFlowDefinition(TEST);

		UUID uuid = UUID.randomUUID();
		when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.empty());

		// when
		Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
			this.workFlowDefinitionService.getWorkFlowDefinitionById(uuid);
		});

		// then
		assertEquals(String.format("Workflow definition with ID: %s not found", uuid), exception.getMessage());
	}

	@Test
	public void getWorkFlowDefinitionByNameWithValidNameTest() {
		// given
		when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(sampleWorkFlowDefinition(TEST));

		// when
		WorkFlowDefinitionResponseDTO result = this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST);

		// then
		assertNotNull(result);
		assertEquals(result.getName(), TEST);

		verify(this.workFlowDefinitionRepository, times(1)).findFirstByName(any());
	}

	@Test
	public void getWorkFlowDefinitionByNameWithMasterWorkflow() {
		// given
		WorkFlowDefinition masterWorkFlow = sampleWorkFlowDefinition(TEST);
		WorkFlowWorkDefinition workFlowWorkDefinition = sampleWorkFlowWorkDefinition("workTest");
		when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(masterWorkFlow);

		when(this.workFlowDefinitionRepository.findById(any()))
				.thenReturn(Optional.of(sampleWorkFlowDefinition("SubWorkFlow")));

		when(this.workFlowWorkRepository.findByWorkFlowDefinitionIdOrderByCreateDateAsc(masterWorkFlow.getId()))
				.thenReturn(List.of(sampleWorkFlowWorkDefinition("SubWorkFlow")));

		// when
		WorkFlowDefinitionResponseDTO result = this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST);

		// then
		assertNotNull(result);
		assertEquals(result.getName(), TEST);

		verify(this.workFlowDefinitionRepository, times(1)).findFirstByName(any());
		when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(sampleWorkFlowDefinition(TEST));
		Optional<WorkDefinitionResponseDTO> firstWork = result.getWorks().stream().findFirst();
		assertTrue(firstWork.isPresent());
		assertEquals(result.getWorks().size(), 1);
		assertEquals(firstWork.get().getName(), "SubWorkFlow");
		assertEquals(firstWork.get().getWorkType(), WorkType.WORKFLOW);
	}

	@Test
	public void getWorkFlowDefinitionByNameWithEmptyMasterWorkflow() {
		// given
		WorkFlowDefinition masterWorkFlow = sampleWorkFlowDefinition(TEST);
		WorkFlowWorkDefinition workFlowWorkDefinition = sampleWorkFlowWorkDefinition("workTest");
		when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(masterWorkFlow);

		when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.empty());

		when(this.workFlowWorkRepository.findByWorkFlowDefinitionIdOrderByCreateDateAsc(any()))
				.thenReturn(List.of(workFlowWorkDefinition));

		// when
		WorkFlowDefinitionResponseDTO result = this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST);

		// then
		assertNotNull(result);
		assertEquals(result.getName(), TEST);

		verify(this.workFlowDefinitionRepository, times(1)).findFirstByName(any());
		when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(sampleWorkFlowDefinition(TEST));

		assertEquals(result.getWorks().size(), 0);
	}

	@Test
	public void getWorkFlowDefinitionByNameWithInvalidNameTest() {
		// given
		when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(null);

		Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
			this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST);
		});

		verify(this.workFlowDefinitionRepository, times(1)).findFirstByName(any());
	}

	@Test
	public void getWorkFlowDefinitionWithoutData() {

		// given
		when(this.workFlowDefinitionRepository.findByTypeIsNot(any())).thenReturn(new ArrayList<>());

		// when
		List<WorkFlowDefinitionResponseDTO> resultList = this.workFlowDefinitionService.getWorkFlowDefinitions();

		// then
		assertNotNull(resultList);
		assertEquals(resultList.size(), 0);

		verify(this.workFlowDefinitionRepository, times(1)).findByTypeIsNot(any());
	}

	@Test
	public void getWorkFlowDefinitionsWithData() {
		// given
		WorkFlowDefinition workFlowOne = sampleWorkFlowDefinition("workFLowOne");
		WorkFlowDefinition checker = sampleCheckerWorkFlowDefinition("checker");

		WorkParameter workParameter = WorkParameter.builder().key("key").description("the key").optional(false)
				.type(WorkParameterType.URI).build();
		WorkFlowCheckerMappingDefinition workFlowCheckerMappingDefinition = WorkFlowCheckerMappingDefinition.builder()
				.checkWorkFlow(checker).cronExpression("* * * * *")
				.tasks(Collections.singletonList(sampleWorkFlowTaskDefinition(checker, "taskOfChecker", workParameter)))
				.build();
		workFlowCheckerMappingDefinition.setId(UUID.randomUUID());
		WorkFlowTaskDefinition task01 = sampleWorkFlowTaskDefinitionWithChecker(workFlowOne, "task01", workParameter,
				workFlowCheckerMappingDefinition);
		WorkFlowTaskDefinition task02 = sampleWorkFlowTaskDefinition(workFlowOne, "task02", workParameter);

		WorkFlowWorkDefinition taskDefinition = WorkFlowWorkDefinition.builder().workFlowDefinition(workFlowOne)
				.workDefinitionId(UUID.randomUUID()).workDefinitionType(WorkType.TASK).createDate(new Date()).build();
		taskDefinition.setId(UUID.randomUUID());
		WorkFlowWorkDefinition taskDefinition02 = WorkFlowWorkDefinition.builder().workFlowDefinition(workFlowOne)
				.workDefinitionId(UUID.randomUUID()).workDefinitionType(WorkType.TASK).createDate(new Date()).build();
		taskDefinition02.setId(UUID.randomUUID());

		workFlowOne.setNumberOfWorks(1);

		when(this.workFlowCheckerMappingDefinitionRepository.findById(workFlowCheckerMappingDefinition.getId()))
				.thenReturn(Optional.of(workFlowCheckerMappingDefinition));
		when(this.workFlowTaskDefinitionRepository.findById(taskDefinition.getWorkDefinitionId()))
				.thenReturn(Optional.of(task01));
		when(this.workFlowTaskDefinitionRepository.findById(taskDefinition02.getWorkDefinitionId()))
				.thenReturn(Optional.of(task02));
		when(this.workFlowDefinitionRepository.findByTypeIsNot(WorkFlowType.CHECKER))
				.thenReturn(Arrays.asList(workFlowOne, sampleWorkFlowDefinition("workFLowTwo")));
		when(this.workFlowWorkRepository.findByWorkFlowDefinitionIdOrderByCreateDateAsc(workFlowOne.getId()))
				.thenReturn(List.of(taskDefinition, taskDefinition02));
		// when
		List<WorkFlowDefinitionResponseDTO> workFlowDefinitionResponseDTOs = this.workFlowDefinitionService
				.getWorkFlowDefinitions();

		// then
		assertNotNull(workFlowDefinitionResponseDTOs);
		assertEquals(workFlowDefinitionResponseDTOs.size(), 2);

		// check root WorkFlow
		WorkFlowDefinitionResponseDTO workFlowOneResponseDTO = workFlowDefinitionResponseDTOs.get(0);
		assertEquals(workFlowOneResponseDTO.getName(), "workFLowOne");
		assertEquals(workFlowOneResponseDTO.getType(), workFlowOne.getType());
		assertEquals(workFlowOneResponseDTO.getProcessingType(), workFlowOne.getProcessingType());
		assertNull(workFlowOneResponseDTO.getCronExpression());
		assertEquals(workFlowDefinitionResponseDTOs.get(1).getName(), "workFLowTwo");
		assertEquals(workFlowOneResponseDTO.getProperties().getVersion(), "1.0.0");
		assertFalse(workFlowOneResponseDTO.getWorks().isEmpty());

		// check associated tasks
		ArrayList<WorkDefinitionResponseDTO> tasks = new ArrayList<>(workFlowOneResponseDTO.getWorks());
		WorkDefinitionResponseDTO taskWorkUnit = tasks.get(0);
		assertEquals(taskWorkUnit.getWorkFlowCheckerMappingDefinitionId(), workFlowCheckerMappingDefinition.getId());
		assertEquals(taskWorkUnit.getParameters().get(workParameter.getKey()), workParameter.getAsJsonSchema());
		assertEquals(taskWorkUnit.getName(), task01.getName());
		WorkDefinitionResponseDTO taskWorkUnit02 = tasks.get(1);
		assertEquals(taskWorkUnit02.getName(), task02.getName());
		assertNull(taskWorkUnit02.getWorkFlowCheckerMappingDefinitionId());
		assertEquals(taskWorkUnit.getParameters().get(workParameter.getKey()), workParameter.getAsJsonSchema());

		// check checker of task01
		WorkDefinitionResponseDTO checkerWorkUnit = taskWorkUnit.getWorks().stream().findFirst().get();
		assertEquals(checkerWorkUnit.getWorkType(), WorkType.CHECKER);
		assertEquals(checkerWorkUnit.getName(), checker.getName());
		assertEquals(checkerWorkUnit.getProcessingType(), checker.getProcessingType());
		assertEquals(checkerWorkUnit.getCronExpression(), workFlowCheckerMappingDefinition.getCronExpression());
		assertEquals(checkerWorkUnit.getId(), checker.getId());

		verify(this.workFlowDefinitionRepository, times(1)).findByTypeIsNot(WorkFlowType.CHECKER);
	}

	@Test
	public void saveWorkFlowCheckerTestWithValidData() {
		// given
		WorkFlowTaskDefinition taskDefinition = this.sampleWorkflowTaskDefinition();
		when(this.workFlowTaskDefinitionRepository.findFirstByName(any())).thenReturn(taskDefinition);

		WorkFlowDefinition wfDefinition = this.sampleWorkFlowDefinition(TEST_WF);
		when(this.workFlowDefinitionRepository.findFirstByName(TEST_WF)).thenReturn(wfDefinition);

		when(workFlowCheckerMappingDefinitionRepository.findFirstByCheckWorkFlow(any())).thenReturn(null);

		// when
		this.workFlowDefinitionService.saveWorkFlowChecker(TEST, TEST_WF,
				WorkFlowCheckerDTO.builder().cronExpression(_10).build());
		// then

		ArgumentCaptor<WorkFlowTaskDefinition> argument = ArgumentCaptor.forClass(WorkFlowTaskDefinition.class);
		verify(this.workFlowTaskDefinitionRepository, times(1)).save(argument.capture());

		assertEquals(argument.getValue().getName(), TEST);
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
		when(this.workFlowTaskDefinitionRepository.findFirstByName(any())).thenReturn(null);

		// when
		this.workFlowDefinitionService.saveWorkFlowChecker(TEST, TEST_WF,
				WorkFlowCheckerDTO.builder().cronExpression(_10).build());

		// then
		verify(this.workFlowTaskDefinitionRepository, times(0)).save(any());
	}

	@Test
	void getWorkParametersByWorkName_when_workIsFound_then_returnParameters() {
		String workFlowName = "test";
		String workFlowTaskName = "testTask";
		String KEY = "key";
		// given
		WorkFlowDefinition workFlowDefinition = this.sampleWorkFlowDefinition(workFlowName);
		WorkParameter workParameter = WorkParameter.builder().key(KEY).description("the key").optional(false)
				.type(WorkParameterType.URI).build();
		WorkFlowTaskDefinition workFlowTaskDefinition = this.sampleWorkFlowTaskDefinition(workFlowDefinition,
				workFlowTaskName, workParameter);
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(workFlowTaskDefinition));
		when(this.workFlowDefinitionRepository.findFirstByName(anyString())).thenReturn(null);
		when(this.workFlowTaskDefinitionRepository.findFirstByName(anyString())).thenReturn(workFlowTaskDefinition);
		assertThat(workFlowDefinitionService.getWorkParametersByWorkName(workFlowTaskName), is(notNullValue()));
		assertThat(workFlowDefinitionService.getWorkParametersByWorkName(workFlowTaskName), hasKey(KEY));
	}

	@Test
	void getWorkParametersByWorkName_when_workIsNotFound_then_returnNull() {
		when(this.workFlowDefinitionRepository.findFirstByName(anyString())).thenReturn(null);
		when(this.workFlowTaskDefinitionRepository.findFirstByName(anyString())).thenReturn(null);
		assertNull(workFlowDefinitionService.getWorkParametersByWorkName("test"));
	}

	@Test
	void getParentWorkFlowByWorkName_when_workIsTask_then_returnWorkflow() {
		String workFlowName = "test";
		String workFlowTaskName = "testTask";
		// given
		WorkFlowDefinition workFlowDefinition = this.sampleWorkFlowDefinition(workFlowName);
		WorkParameter workParameter = WorkParameter.builder().key("key").description("the key").optional(false)
				.type(WorkParameterType.URI).build();
		WorkFlowTaskDefinition workFlowTaskDefinition = this.sampleWorkFlowTaskDefinition(workFlowDefinition,
				workFlowTaskName, workParameter);
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(workFlowTaskDefinition));
		when(this.workFlowDefinitionRepository.findFirstByName(anyString())).thenReturn(null);
		when(this.workFlowTaskDefinitionRepository.findFirstByName(anyString())).thenReturn(workFlowTaskDefinition);
		when(workFlowWorkRepository.findFirstByWorkDefinitionId(workFlowTaskDefinition.getId()))
				.thenReturn(WorkFlowWorkDefinition.builder().workFlowDefinition(workFlowDefinition).build());
		assertEquals(workFlowDefinition, workFlowDefinitionService.getParentWorkFlowByWorkName(workFlowTaskName));
	}

	@Test
	void getParentWorkFlowByWorkName_when_workIsWorkFlow_then_returnWorkflow() {
		String workFlowName = "test";
		String workFlowParentName = "testParent";
		// given
		WorkFlowDefinition workFlowDefinition = this.sampleWorkFlowDefinition(workFlowName);
		WorkFlowDefinition workFlowParentDefinition = this.sampleWorkFlowDefinition(workFlowParentName);

		when(this.workFlowDefinitionRepository.findFirstByName(workFlowName)).thenReturn(workFlowDefinition);
		when(workFlowWorkRepository.findFirstByWorkDefinitionId(workFlowDefinition.getId()))
				.thenReturn(WorkFlowWorkDefinition.builder().workFlowDefinition(workFlowParentDefinition).build());
		assertEquals(workFlowParentDefinition, workFlowDefinitionService.getParentWorkFlowByWorkName(workFlowName));
	}

	private WorkFlowTaskDefinition sampleWorkflowTaskDefinition() {
		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder().name(TEST).build();
		workFlowTaskDefinition.setId(UUID.randomUUID());
		return workFlowTaskDefinition;
	}

	private WorkFlowDefinition sampleWorkFlowDefinition(String name) {
		com.redhat.parodos.workflow.parameter.WorkParameter workParameter = com.redhat.parodos.workflow.parameter.WorkParameter
				.builder().key(KEY).description(KEY_DESCRIPTION).optional(false)
				.type(com.redhat.parodos.workflow.parameter.WorkParameterType.TEXT).build();

		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(name).type(WorkFlowType.ASSESSMENT)
				.properties(WorkFlowPropertiesDefinition.builder().version("1.0.0").build())
				.processingType(WorkFlowProcessingType.SEQUENTIAL)
				.parameters(WorkFlowDTOUtil
						.writeObjectValueAsString(Map.of(workParameter.getKey(), workParameter.getAsJsonSchema())))
				.numberOfWorks(1).build();
		workFlowDefinition.setId(UUID.randomUUID());
		return workFlowDefinition;
	}

	private WorkFlowDefinition sampleCheckerWorkFlowDefinition(String name) {
		com.redhat.parodos.workflow.parameter.WorkParameter workParameter = com.redhat.parodos.workflow.parameter.WorkParameter
				.builder().key(KEY).description(KEY_DESCRIPTION).optional(false)
				.type(com.redhat.parodos.workflow.parameter.WorkParameterType.TEXT).build();

		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(name).type(WorkFlowType.CHECKER)
				.properties(WorkFlowPropertiesDefinition.builder().version("1.0.0").build())
				.processingType(WorkFlowProcessingType.SEQUENTIAL)
				.parameters(WorkFlowDTOUtil
						.writeObjectValueAsString(Map.of(workParameter.getKey(), workParameter.getAsJsonSchema())))
				.numberOfWorks(1).build();
		workFlowDefinition.setId(UUID.randomUUID());
		return workFlowDefinition;
	}

	private WorkFlowTaskDefinition sampleWorkFlowTaskDefinition(WorkFlowDefinition workFlowDefinition,
			String workFlowTaskName, WorkParameter workParameter) {
		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder()
				.workFlowDefinition(workFlowDefinition).name(workFlowTaskName)
				.parameters(WorkFlowDTOUtil
						.writeObjectValueAsString(Map.of(workParameter.getKey(), workParameter.getAsJsonSchema())))
				.outputs("[]").build();
		workFlowTaskDefinition.setId(UUID.randomUUID());
		return workFlowTaskDefinition;
	}

	private WorkFlowTaskDefinition sampleWorkFlowTaskDefinitionWithChecker(WorkFlowDefinition workFlowDefinition,
			String workFlowTaskName, WorkParameter workParameter, WorkFlowCheckerMappingDefinition checker) {
		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder()
				.workFlowDefinition(workFlowDefinition).name(workFlowTaskName)
				.parameters(WorkFlowDTOUtil
						.writeObjectValueAsString(Map.of(workParameter.getKey(), workParameter.getAsJsonSchema())))
				.workFlowCheckerMappingDefinition(checker).outputs("[]").build();
		workFlowTaskDefinition.setId(UUID.randomUUID());
		return workFlowTaskDefinition;
	}

	private WorkFlowWorkDefinition sampleWorkFlowWorkDefinition(String name) {
		WorkFlowWorkDefinition workFlowWorkDefinition = WorkFlowWorkDefinition.builder().build();
		workFlowWorkDefinition.setWorkDefinitionType(WorkType.WORKFLOW);
		workFlowWorkDefinition.setId(UUID.randomUUID());
		return workFlowWorkDefinition;
	}

	private WorkFlowDefinitionServiceImpl getWorkflowDefinitionService() {
		return new WorkFlowDefinitionServiceImpl(this.workFlowDefinitionRepository,
				this.workFlowTaskDefinitionRepository, this.workFlowCheckerMappingDefinitionRepository,
				this.workFlowWorkRepository, new ModelMapper());
	}

}
