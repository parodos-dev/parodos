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

import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowPropertiesDefinition;
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
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.util.WorkFlowDTOUtil;
import com.redhat.parodos.workflows.workflow.WorkFlowPropertiesMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import com.redhat.parodos.workflow.task.WorkFlowTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
		this.workFlowDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = Mockito.mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowWorkRepository = Mockito.mock(WorkFlowWorkRepository.class);
		this.workFlowCheckerMappingDefinitionRepository = Mockito
				.mock(WorkFlowCheckerMappingDefinitionRepository.class);
		this.workFlowDefinitionService = getWorkflowDefinitionService();
	}

	@Test
	public void simpleSaveTest() {
		String workFlowName = "test";
		String workFlowTaskName = "testTask";
		// given
		WorkFlowDefinition workFlowDefinition = this.sampleWorkFlowDefinition(workFlowName);
		WorkFlowTask workFlowTask = Mockito.mock(WorkFlowTask.class);
		WorkParameter workParameter = WorkParameter.builder().key("key").description("the key").optional(false)
				.type(WorkParameterType.URL).build();
		Mockito.when(workFlowTask.getName()).thenReturn(workFlowTaskName);
		Mockito.when(workFlowTask.getWorkFlowTaskParameters()).thenReturn(List.of(workParameter));
		WorkFlowTaskDefinition workFlowTaskDefinition = this.sampleWorkFlowTaskDefinition(workFlowDefinition,
				workFlowTaskName, workParameter);
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(workFlowTaskDefinition));
		Mockito.when(this.workFlowTaskDefinitionRepository.save(any())).thenReturn(workFlowTaskDefinition);
		Mockito.when(this.workFlowDefinitionRepository.save(any())).thenReturn(workFlowDefinition);
		WorkFlowPropertiesMetadata properties = WorkFlowPropertiesMetadata.builder().version("1.0.0").build();

		// when
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = this.workFlowDefinitionService.save(workFlowName,
				WorkFlowType.ASSESSMENT, properties, Collections.emptyList(), List.of(workFlowTask),
				WorkFlowProcessingType.SEQUENTIAL);

		// then
		assertNotNull(workFlowDefinitionResponseDTO);
		assertNotNull(workFlowDefinitionResponseDTO.getId());
		assertEquals(workFlowDefinitionResponseDTO.getName(), workFlowName);
		assertEquals(workFlowDefinitionResponseDTO.getProperties().getVersion(), "1.0.0");

		ArgumentCaptor<WorkFlowDefinition> argument = ArgumentCaptor.forClass(WorkFlowDefinition.class);
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(2)).save(argument.capture());
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
		WorkFlowTask workFlowTask = Mockito.mock(WorkFlowTask.class);
		WorkParameter workParameter = WorkParameter.builder().key("key").description("the key").optional(false)
				.type(WorkParameterType.URL).build();
		Mockito.when(workFlowTask.getName()).thenReturn(workFlowTaskName);
		Mockito.when(workFlowTask.getWorkFlowTaskParameters()).thenReturn(List.of(workParameter));
		WorkFlowTaskDefinition workFlowTaskDefinition = this.sampleWorkFlowTaskDefinition(workFlowDefinition,
				workFlowTaskName, workParameter);
		workFlowTaskDefinition.setParameters("{}");
		workFlowTaskDefinition.setOutputs("[]");
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(workFlowTaskDefinition));
		Mockito.when(this.workFlowTaskDefinitionRepository.save(any())).thenReturn(workFlowTaskDefinition);
		Mockito.when(this.workFlowDefinitionRepository.save(any())).thenReturn(workFlowDefinition);
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(anyString())).thenReturn(workFlowDefinition);
		Mockito.when(this.workFlowTaskDefinitionRepository.findFirstByName(anyString()))
				.thenReturn(workFlowTaskDefinition);
		// when
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = this.workFlowDefinitionService.save(workFlowName,
				WorkFlowType.ASSESSMENT, null, Collections.emptyList(), List.of(workFlowTask),
				WorkFlowProcessingType.SEQUENTIAL);

		// then
		assertNotNull(workFlowDefinitionResponseDTO);
		assertNotNull(workFlowDefinitionResponseDTO.getId());
		assertEquals(workFlowDefinitionResponseDTO.getName(), workFlowName);

		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(2)).save(workFlowDefinition);
		Mockito.verify(this.workFlowTaskDefinitionRepository, Mockito.never()).save(any());
	}

	@Test
	public void getWorkFlowDefinitionByIdWithValidUUIDTest() {
		// given
		WorkFlowDefinition workFlowDefinition = this.sampleWorkFlowDefinition(TEST);

		UUID uuid = UUID.randomUUID();
		Mockito.when(this.workFlowDefinitionRepository.findById(uuid)).thenReturn(Optional.of(workFlowDefinition));

		// when
		WorkFlowDefinitionResponseDTO wkDTO = this.workFlowDefinitionService.getWorkFlowDefinitionById(uuid);

		// then
		assertNotNull(wkDTO);
		assertEquals(wkDTO.getName(), TEST);

		ArgumentCaptor<UUID> argument = ArgumentCaptor.forClass(UUID.class);
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1)).findById(argument.capture());
		assertEquals(argument.getValue(), uuid);
	}

	@Test
	public void getWorkFlowDefinitionByIdWithInvalidUUIDTest() {
		// given
		WorkFlowDefinition workFlowDefinition = this.sampleWorkFlowDefinition(TEST);

		UUID uuid = UUID.randomUUID();
		Mockito.when(this.workFlowDefinitionRepository.findById(any())).thenReturn(Optional.empty());

		// when
		Exception exception = assertThrows(RuntimeException.class, () -> {
			this.workFlowDefinitionService.getWorkFlowDefinitionById(uuid);
		});

		// then
		assertEquals(exception.getMessage(), String.format("Workflow definition id %s not found", uuid));
	}

	@Test
	public void getWorkFlowDefinitionByNameWithValidNameTest() {
		// given
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(any()))
				.thenReturn(sampleWorkFlowDefinition(TEST));

		// when
		WorkFlowDefinitionResponseDTO result = this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST);

		// then
		assertNotNull(result);
		assertEquals(result.getName(), TEST);

		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1)).findFirstByName(any());
	}

	@Test
	public void getWorkFlowDefinitionByNameWithInvalidNameTest() {
		// given
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(null);

		Exception exception = assertThrows(RuntimeException.class, () -> {
			this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST);
		});

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
		Mockito.when(this.workFlowDefinitionRepository.findByTypeIsNot(WorkFlowType.CHECKER)).thenReturn(
				Arrays.asList(sampleWorkFlowDefinition("workFLowOne"), sampleWorkFlowDefinition("workFLowTwo")));

		// when
		List<WorkFlowDefinitionResponseDTO> workFlowDefinitionResponseDTOs = this.workFlowDefinitionService
				.getWorkFlowDefinitions();

		// then
		assertNotNull(workFlowDefinitionResponseDTOs);
		assertEquals(workFlowDefinitionResponseDTOs.size(), 2);
		assertEquals(workFlowDefinitionResponseDTOs.get(0).getName(), "workFLowOne");
		assertEquals(workFlowDefinitionResponseDTOs.get(0).getProperties().getVersion(), "1.0.0");
		Mockito.verify(this.workFlowDefinitionRepository, Mockito.times(1)).findByTypeIsNot(WorkFlowType.CHECKER);
	}

	@Test
	public void saveWorkFlowCheckerTestWithValidData() {
		// given
		WorkFlowTaskDefinition taskDefinition = this.sampleWorkflowTaskDefinition();
		Mockito.when(this.workFlowTaskDefinitionRepository.findFirstByName(any())).thenReturn(taskDefinition);

		WorkFlowDefinition wfDefinition = this.sampleWorkFlowDefinition(TEST_WF);
		Mockito.when(this.workFlowDefinitionRepository.findFirstByName(TEST_WF)).thenReturn(wfDefinition);

		Mockito.when(workFlowCheckerMappingDefinitionRepository.findFirstByCheckWorkFlow(any())).thenReturn(null);

		// when
		this.workFlowDefinitionService.saveWorkFlowChecker(TEST, TEST_WF,
				WorkFlowCheckerDTO.builder().cronExpression(_10).build());
		// then

		ArgumentCaptor<WorkFlowTaskDefinition> argument = ArgumentCaptor.forClass(WorkFlowTaskDefinition.class);
		Mockito.verify(this.workFlowTaskDefinitionRepository, Mockito.times(1)).save(argument.capture());

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
		Mockito.when(this.workFlowTaskDefinitionRepository.findFirstByName(any())).thenReturn(null);

		// when
		this.workFlowDefinitionService.saveWorkFlowChecker(TEST, TEST_WF,
				WorkFlowCheckerDTO.builder().cronExpression(_10).build());

		// then
		Mockito.verify(this.workFlowTaskDefinitionRepository, Mockito.times(0)).save(any());
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

	private WorkFlowTaskDefinition sampleWorkFlowTaskDefinition(WorkFlowDefinition workFlowDefinition,
			String workFlowTaskName, WorkParameter workParameter) {
		WorkFlowTaskDefinition workFlowTaskDefinition = WorkFlowTaskDefinition.builder()
				.workFlowDefinition(workFlowDefinition).name(workFlowTaskName)
				.parameters(WorkFlowDTOUtil.writeObjectValueAsString(List.of(workParameter))).build();
		workFlowTaskDefinition.setId(UUID.randomUUID());
		return workFlowTaskDefinition;
	}

	private WorkFlowDefinitionServiceImpl getWorkflowDefinitionService() {
		return new WorkFlowDefinitionServiceImpl(this.workFlowDefinitionRepository,
				this.workFlowTaskDefinitionRepository, this.workFlowCheckerMappingDefinitionRepository,
				this.workFlowWorkRepository, new ModelMapper());
	}

}
