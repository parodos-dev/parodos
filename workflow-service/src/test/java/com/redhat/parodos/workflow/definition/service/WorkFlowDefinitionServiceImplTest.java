// package com.redhat.parodos.workflow.definition.service;
//
// import com.redhat.parodos.workflow.enums.WorkFlowType;
// import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
// import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
// import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerDefinition;
// import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
// import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
// import
// com.redhat.parodos.workflow.definition.repository.WorkFlowCheckerDefinitionRepository;
// import
// com.redhat.parodos.workflow.definition.repository.WorkFlowWorkDependencyRepository;
// import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
// import
// com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
// import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
// import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
//
// import com.redhat.parodos.workflow.task.WorkFlowTask;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
//
// import org.mockito.ArgumentCaptor;
// import org.mockito.Mockito;
// import org.modelmapper.ModelMapper;
//
// import java.util.HashMap;
// import java.util.Optional;
// import java.util.UUID;
// import java.util.Arrays;
// import java.util.ArrayList;
// import java.util.List;
//
// class WorkFlowDefinitionServiceImplTest {
//
// private WorkFlowDefinitionRepository wfDefinitionRepository;
//
// private WorkFlowCheckerDefinitionRepository workFlowCheckerDefinitionRepository;
//
// private WorkFlowTaskDefinitionRepository wfTaskDefinitionRepository;
//
// private WorkFlowWorkDependencyRepository wfDefinitionChainRepository;
//
// private WorkFlowTask wfTask;
//
// private WorkFlowDefinitionServiceImpl wkService;
//
// @BeforeEach
// public void initEach() {
// this.wfDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
// this.wfTaskDefinitionRepository = Mockito.mock(WorkFlowTaskDefinitionRepository.class);
// this.workFlowCheckerDefinitionRepository =
// Mockito.mock(WorkFlowCheckerDefinitionRepository.class);
// this.wfTask = Mockito.mock(WorkFlowTask.class);
// this.wkService = getWorkflowService();
// }
//
// @Test
// public void simpleSaveTest() {
// // given
//
// WorkFlowDefinition wfDefinition = this.sampleWorkflowDefinition("test");
//
// Mockito.when(this.wfDefinitionRepository.save(any())).thenReturn(wfDefinition);
//
// WorkFlowTask testTask = Mockito.mock(WorkFlowTask.class);
// WorkFlowTaskParameter taskParameter =
// WorkFlowTaskParameter.builder().key("key").optional(false)
// .type(WorkFlowTaskParameterType.URL).description("key").build();
// Mockito.when(testTask.getWorkFlowTaskParameters()).thenReturn(Arrays.asList(taskParameter));
//
// // when
// WorkFlowDefinitionResponseDTO res = this.wkService.save("test", "test description",
// WorkFlowType.ASSESSMENT,
// new HashMap<>() {
// {
// put("testTask", testTask);
// }
// });
//
// // then
// assertNotNull(res);
// assertNotNull(res.getId());
// assertEquals(res.getName(), "test");
//
// ArgumentCaptor<WorkFlowDefinition> argument =
// ArgumentCaptor.forClass(WorkFlowDefinition.class);
// Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).save(argument.capture());
// assertEquals(argument.getValue().getName(), "test");
// assertEquals(argument.getValue().getDescription(), "test description");
// assertEquals(argument.getValue().getType(), WorkFlowType.ASSESSMENT.toString());
// assertEquals(argument.getValue().getWorkFlowTaskDefinitions().size(), 1);
// assertEquals(argument.getValue().getWorkFlowTaskDefinitions().stream().findFirst().get().getName(),
// "testTask");
// }
//
// @Test
// public void getWorkFlowDefinitionByIdWithValidUUIDTest() {
// // given
// WorkFlowDefinition wfDefinition = this.sampleWorkflowDefinition("test");
//
// UUID uuid = UUID.randomUUID();
// Mockito.when(this.wfDefinitionRepository.findById(uuid)).thenReturn(Optional.of(wfDefinition));
//
// // when
// WorkFlowDefinitionResponseDTO wkDTO = this.wkService.getWorkFlowDefinitionById(uuid);
//
// // then
// assertNotNull(wkDTO);
// assertEquals(wkDTO.getName(), "test");
//
// ArgumentCaptor<UUID> argument = ArgumentCaptor.forClass(UUID.class);
// Mockito.verify(this.wfDefinitionRepository,
// Mockito.times(1)).findById(argument.capture());
// assertEquals(argument.getValue(), uuid);
// }
//
// @Test
// public void getWorkFlowDefinitionByIdWithInvalidUUIDTest() {
// // given
// WorkFlowDefinition wfDefinition = this.sampleWorkflowDefinition("test");
//
// UUID uuid = UUID.randomUUID();
// Mockito.when(this.wfDefinitionRepository.findById(any())).thenReturn(Optional.empty());
//
// // when
// Exception exception = assertThrows(RuntimeException.class, () -> {
// // WorkFlowDefinitionResponseDTO wkDTO =
// // wkService.getWorkFlowDefinitionById(uuid);
// this.wkService.getWorkFlowDefinitionById(uuid);
// });
//
// // then
// assertEquals(exception.getMessage(), String.format("Workflow definition id %s not
// found", uuid));
// }
//
// @Test
// public void getWorkFlowDefinitionByNameWithValidNameTest() {
// // given
// Mockito.when(this.wfDefinitionRepository.findByName(any()))
// .thenReturn(Arrays.asList(sampleWorkflowDefinition("test"),
// sampleWorkflowDefinition("test")));
//
// // when
// List<WorkFlowDefinitionResponseDTO> resultList =
// this.wkService.getWorkFlowDefinitionsByName("test");
//
// // then
// assertNotNull(resultList);
// assertEquals(resultList.size(), 2);
// assertEquals(resultList.get(0).getName(), "test");
//
// Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).findByName(any());
// }
//
// @Test
// public void getWorkFlowDefinitionByNameWithInvalidNameTest() {
// // given
// Mockito.when(this.wfDefinitionRepository.findByName(any())).thenReturn(new
// ArrayList<WorkFlowDefinition>());
//
// // when
// List<WorkFlowDefinitionResponseDTO> resultList =
// this.wkService.getWorkFlowDefinitionsByName("test");
//
// // then
// assertNotNull(resultList);
// assertEquals(resultList.size(), 0);
//
// Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).findByName(any());
// }
//
// @Test
// public void getWorkFlowDefinitionWithoutData() {
// // given
// Mockito.when(this.wfDefinitionRepository.findAll()).thenReturn(new
// ArrayList<WorkFlowDefinition>());
//
// // when
// List<WorkFlowDefinitionResponseDTO> resultList =
// this.wkService.getWorkFlowDefinitions();
//
// // then
// assertNotNull(resultList);
// assertEquals(resultList.size(), 0);
//
// Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).findAll();
// }
//
// @Test
// public void getWorkFlowDefinitionWitData() {
// // given
// Mockito.when(this.wfDefinitionRepository.findAll())
// .thenReturn(Arrays.asList(sampleWorkflowDefinition("test"),
// sampleWorkflowDefinition("alice")));
//
// // when
// List<WorkFlowDefinitionResponseDTO> resultList =
// this.wkService.getWorkFlowDefinitions();
//
// // then
// assertNotNull(resultList);
// assertEquals(resultList.size(), 2);
//
// Mockito.verify(this.wfDefinitionRepository, Mockito.times(1)).findAll();
// }
//
// @Test
// public void saveWorkFlowCheckerTestWithValidData() {
// // given
// WorkFlowTaskDefinition taskDefinition = this.sampleWorkflowTaskDefinition("test");
// Mockito.when(this.wfTaskDefinitionRepository.findFirstByName(any())).thenReturn(taskDefinition);
//
// WorkFlowDefinition wfDefinition = this.sampleWorkflowDefinition("test-wf");
// Mockito.when(this.wfDefinitionRepository.findByName("test-wf")).thenReturn(Arrays.asList(wfDefinition));
//
// WorkFlowDefinition nextWfDefinition = this.sampleWorkflowDefinition("next-wf");
// Mockito.when(this.wfDefinitionRepository.findByName("next-wf")).thenReturn(Arrays.asList(nextWfDefinition));
//
// Mockito.when(workFlowCheckerDefinitionRepository.findFirstByCheckWorkFlow(any())).thenReturn(null);
//
// // when
// this.wkService.saveWorkFlowChecker("test", "test-wf",
// WorkFlowCheckerDTO.builder().cronExpression("10 * *").build());
// // then
//
// ArgumentCaptor<WorkFlowTaskDefinition> argument =
// ArgumentCaptor.forClass(WorkFlowTaskDefinition.class);
// Mockito.verify(this.wfTaskDefinitionRepository,
// Mockito.times(1)).save(argument.capture());
//
// assertEquals(argument.getValue().getName(), "test");
// WorkFlowCheckerDefinition checkerDefinition =
// argument.getValue().getWorkFlowCheckerDefinition();
// assertNotNull(checkerDefinition);
//
// assertNotNull(checkerDefinition.getCheckWorkFlow());
// assertEquals(checkerDefinition.getCheckWorkFlow().getName(), wfDefinition.getName());
// assertEquals(checkerDefinition.getCheckWorkFlow().getId(), wfDefinition.getId());
//
// assertNotNull(checkerDefinition.getNextWorkFlow());
// assertEquals(checkerDefinition.getNextWorkFlow().getName(),
// nextWfDefinition.getName());
// assertEquals(checkerDefinition.getNextWorkFlow().getId(), nextWfDefinition.getId());
//
// assertEquals(checkerDefinition.getCheckWorkFlow().getId(), wfDefinition.getId());
// assertEquals(checkerDefinition.getTasks().get(0).getId(), taskDefinition.getId());
// }
//
// @Test
// public void saveWorkFlowCheckerTestWithInvalidData() {
// // @TODO not sure if not doing anything is the right thing to do here
// // given
// WorkFlowTaskDefinition taskDefinition = this.sampleWorkflowTaskDefinition("test");
// Mockito.when(this.wfTaskDefinitionRepository.findFirstByName(any())).thenReturn(null);
//
// // when
// this.wkService.saveWorkFlowChecker("test", "test-wf",
// WorkFlowCheckerDTO.builder().cronExpression("10 * *").build());
// // then
// Mockito.verify(this.wfTaskDefinitionRepository, Mockito.times(0)).save(any());
// }
//
// private WorkFlowTaskDefinition sampleWorkflowTaskDefinition(String name) {
// WorkFlowTaskDefinition taskDefinition =
// WorkFlowTaskDefinition.builder().name(name).build();
// taskDefinition.setId(UUID.randomUUID());
// assertNotNull(taskDefinition.getId());
// return taskDefinition;
// }
//
// private WorkFlowDefinition sampleWorkflowDefinition(String name) {
// WorkFlowDefinition wf = WorkFlowDefinition.builder().name(name).build();
// wf.setId(UUID.randomUUID());
// return wf;
// }
//
// private WorkFlowDefinitionServiceImpl getWorkflowService() {
// return new WorkFlowDefinitionServiceImpl(this.wfDefinitionRepository,
// this.wfTaskDefinitionRepository,
// this.workFlowCheckerDefinitionRepository, this.wfDefinitionChainRepository, new
// ModelMapper());
//
// }
//
// }
