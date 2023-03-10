// package com.redhat.parodos.workflow.execution.aspect;
//
// import com.redhat.parodos.workflow.WorkFlowDelegate;
// import com.redhat.parodos.workflow.enums.WorkFlowType;
// import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerDefinition;
// import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
// import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
// import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
// import com.redhat.parodos.workflow.execution.scheduler.WorkFlowSchedulerServiceImpl;
// import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
// import com.redhat.parodos.workflows.work.DefaultWorkReport;
// import com.redhat.parodos.workflows.work.WorkContext;
// import com.redhat.parodos.workflows.work.WorkReport;
// import com.redhat.parodos.workflows.work.WorkStatus;
// import org.aspectj.lang.ProceedingJoinPoint;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
//
// import java.util.UUID;
// import java.util.List;
//
// import static org.junit.jupiter.api.Assertions.*;
//
// class WorkFlowExecutionAspectTest {
//
// private WorkFlowServiceImpl workFlowService;
//
// private WorkFlowSchedulerServiceImpl workFlowSchedulerService;
//
// private WorkFlowDefinitionRepository workFlowDefinitionRepository;
//
// private WorkFlowExecutionAspect workFlowExecutionAspect;
//
// @BeforeEach
// public void initEach() {
// this.workFlowService = Mockito.mock(WorkFlowServiceImpl.class);
// this.workFlowSchedulerService = Mockito.mock(WorkFlowSchedulerServiceImpl.class);
// this.workFlowDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
//
// // this.workFlow =
// //
// SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(Mockito.mock(Work.class)).build();
// this.workFlowExecutionAspect = new WorkFlowExecutionAspect(this.workFlowService,
// this.workFlowSchedulerService,
// this.workFlowDefinitionRepository);
// }
//
// @Test
// public void ExecuteAroundAdviceWithValidDataTest() {
//
// // given
// UUID projectID = UUID.randomUUID();
// WorkContext workContext = new WorkContext() {
// {
// put("WORKFLOW_DEFINITION_NAME", "testWorkFlow");
// put("PROJECT_ID", projectID);
// }
// };
//
// WorkFlowDefinition workFlowDefinition = getSampleWorkFlowDefinition("test");
// Mockito.when(this.workFlowDefinitionRepository.findByName(Mockito.any()))
// .thenReturn(List.of(workFlowDefinition));
// Mockito.when(this.workFlowService.saveWorkFlow(Mockito.any(), Mockito.any(),
// Mockito.any()))
// .thenReturn(getSampleWorkFlowExecution());
//
// ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
// assertDoesNotThrow(() -> {
// Mockito.when(proceedingJoinPoint.proceed())
// .thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
// });
//
// // when
// WorkReport workReport =
// this.workFlowExecutionAspect.executeAroundAdvice(proceedingJoinPoint, workContext);
//
// // then
// assertNotNull(workReport);
// assertEquals(workReport.getStatus().toString(), "COMPLETED");
// assertEquals(workReport.getWorkContext().get("WORKFLOW_DEFINITION_NAME"),
// "testWorkFlow");
// assertEquals(workReport.getWorkContext().get("WORKFLOW_DEFINITION_ID"),
// workFlowDefinition.getId().toString());
// assertEquals(workReport.getWorkContext().get("PROJECT_ID"), projectID);
// Mockito.verify(this.workFlowSchedulerService, Mockito.times(1)).stop(Mockito.any());
// Mockito.verify(this.workFlowService, Mockito.times(1)).saveWorkFlow(Mockito.any(),
// Mockito.any(),
// Mockito.any());
// Mockito.verify(this.workFlowService, Mockito.times(1))
// .updateWorkFlow(Mockito.argThat(w -> w.getStatus().toString().equals("COMPLETED")));
// }
//
// @Test
// void ExecuteAroundAdviceWithInProgressWorkFlowTest() {
// // given
// UUID projectID = UUID.randomUUID();
// WorkContext workContext = new WorkContext() {
// {
// put("WORKFLOW_DEFINITION_NAME", "testWorkFlow");
// put("PROJECT_ID", projectID);
// }
// };
//
// WorkFlowDefinition workFlowDefinition = getSampleWorkFlowDefinition("test");
//
// Mockito.when(this.workFlowDefinitionRepository.findByName(Mockito.any()))
// .thenReturn(List.of(workFlowDefinition));
// Mockito.when(this.workFlowService.saveWorkFlow(Mockito.any(), Mockito.any(),
// Mockito.any()))
// .thenReturn(getSampleWorkFlowExecution());
//
// ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
// assertDoesNotThrow(() -> {
// Mockito.when(proceedingJoinPoint.proceed())
// .thenReturn(new DefaultWorkReport(WorkStatus.FAILED, workContext));
// });
//
// // when
// WorkReport workReport =
// this.workFlowExecutionAspect.executeAroundAdvice(proceedingJoinPoint, workContext);
//
// // then
// assertNotNull(workReport);
// assertEquals(workReport.getStatus().toString(), "FAILED");
// assertEquals(workReport.getWorkContext().get("WORKFLOW_DEFINITION_NAME"),
// "testWorkFlow");
// assertEquals(workReport.getWorkContext().get("WORKFLOW_DEFINITION_ID"),
// workFlowDefinition.getId().toString());
// assertEquals(workReport.getWorkContext().get("PROJECT_ID"), projectID);
// Mockito.verify(this.workFlowSchedulerService, Mockito.times(1)).schedule(Mockito.any(),
// Mockito.any(),
// Mockito.any());
// Mockito.verify(this.workFlowService, Mockito.times(1)).saveWorkFlow(Mockito.any(),
// Mockito.any(),
// Mockito.any());
// Mockito.verify(this.workFlowService, Mockito.times(1))
// .updateWorkFlow(Mockito.argThat(w -> w.getStatus().toString().equals("FAILED")));
// }
//
// WorkFlowExecution getSampleWorkFlowExecution() {
// return new WorkFlowExecution() {
// {
// setId(UUID.randomUUID());
// }
// };
// }
//
// WorkFlowDefinition getSampleWorkFlowDefinition(String name) {
// WorkFlowCheckerDefinition workFlowCheckerDefinition =
// WorkFlowCheckerDefinition.builder()
// .cronExpression("* * * * *").build();
//
// WorkFlowDefinition workFlowDefinition =
// WorkFlowDefinition.builder().type(WorkFlowType.CHECKER.toString())
// .checkerWorkFlowDefinition(workFlowCheckerDefinition).name(name).build();
// workFlowDefinition.setId(UUID.randomUUID());
// return workFlowDefinition;
// }
//
// }