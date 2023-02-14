package com.redhat.parodos.workflow.definition.service;

import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import com.redhat.parodos.workflow.task.WorkFlowTask;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.HashMap;
import java.util.Map;

class WorkFlowDefinitionServiceImplTest {

	private WorkFlowDefinitionRepository wfDefinitionRepository;

	private WorkFlowTaskDefinitionRepository wfTaskDefinitionRepository;

	private WorkFlowTask wfTask;

	@BeforeEach
	public void initEach() {
		this.wfDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		this.wfTaskDefinitionRepository = Mockito.mock(WorkFlowTaskDefinitionRepository.class);
		this.wfTask = Mockito.mock(WorkFlowTask.class);
	}

	@Test
	public void simpleSaveTest() {
		// given
		WorkFlowDefinitionServiceImpl wkService = new WorkFlowDefinitionServiceImpl(this.wfDefinitionRepository,
				this.wfTaskDefinitionRepository, new ModelMapper());

		WorkFlowDefinition wfDefinition = WorkFlowDefinition.builder().name("test").build();

		Mockito.when(this.wfDefinitionRepository.save(Mockito.any())).thenReturn(wfDefinition);

		// when
		WorkFlowDefinitionResponseDTO res = wkService.save("test", "test description", WorkFlowType.ASSESSMENT,
				new HashMap<String, WorkFlowTask>() {
					{
						put("testTask", Mockito.mock(WorkFlowTask.class));
					}
				});

		// then
		assertNotNull(res);
		assertEquals(res.getId(), null);
		assertEquals(res.getName(), "test");

		ArgumentCaptor<WorkFlowDefinition> argument = ArgumentCaptor.forClass(WorkFlowDefinition.class);
		Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).save(argument.capture());
		assertEquals(argument.getValue().getName(), "test");
		assertEquals(argument.getValue().getDescription(), "test description");
		assertEquals(argument.getValue().getType(), WorkFlowType.ASSESSMENT.toString());
		assertEquals(argument.getValue().getWorkFlowTaskDefinitions().size(), 1);
		assertEquals(argument.getValue().getWorkFlowTaskDefinitions().stream().findFirst().get().getName(), "testTask");
	}

}
