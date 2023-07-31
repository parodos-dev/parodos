package com.redhat.parodos.workflow.execution.aspect;

import java.util.List;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.entity.WorkFlowCheckerMappingDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.execution.continuation.WorkFlowContinuationServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.redhat.parodos.workflow.execution.aspect.WorkFlowExecutionAspectTest.getSampleWorkFlowDefinition;
import static com.redhat.parodos.workflow.execution.aspect.WorkFlowExecutionAspectTest.getSampleWorkFlowExecution;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AssessmentInfrastructureWorkFlowPostInterceptorTest {

	@Mock
	private WorkFlowServiceImpl workFlowService;

	@Mock
	private WorkFlowRepository workFlowRepository;

	@Mock
	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	@Mock
	private WorkFlowContinuationServiceImpl workFlowContinuationServiceImpl;

	private WorkFlowExecution workFlowExecution;

	private WorkFlowExecution checkerExecution;

	private AssessmentInfrastructureWorkFlowPostInterceptor underTest;

	@BeforeEach
	void setUp() {
		workFlowExecution = getSampleWorkFlowExecution();
		checkerExecution = getSampleWorkFlowExecution();

		var workFlowDefinition = getSampleWorkFlowDefinition("");
		workFlowDefinition.getCheckerWorkFlowDefinition().setCheckWorkFlow(workFlowDefinition);
		workFlowDefinition
				.setWorkFlowTaskDefinitions(List.of(WorkFlowTaskDefinition.builder()
						.workFlowCheckerMappingDefinition(
								WorkFlowCheckerMappingDefinition.builder().checkWorkFlow(workFlowDefinition).build())
						.build()));
		WorkContext workContext = new WorkContext();
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_DEFINITION,
				WorkContextDelegate.Resource.NAME, "");
		WorkContextUtils.setMainExecutionId(workContext, UUID.randomUUID());

		when(workFlowDefinitionRepository.findFirstByName(any())).thenReturn(workFlowDefinition);
		when(workFlowRepository.findFirstByWorkFlowDefinitionIdAndMainWorkFlowExecution(any(), any()))
				.thenReturn(checkerExecution);
		when(workFlowService.saveWorkFlow(any(), any(), any(), any(), any(), any())).thenReturn(workFlowExecution);

		underTest = new AssessmentInfrastructureWorkFlowPostInterceptor(workFlowDefinition, workContext,
				workFlowService, workFlowRepository, workFlowExecution, getSampleWorkFlowExecution());
	}

	@Test
	void workFlowRejectedTest() {
		// given
		workFlowExecution.setStatus(WorkStatus.REJECTED);
		// when
		underTest.handlePostWorkFlowExecution();
		// then
		verify(workFlowService, times(1)).updateWorkFlow(workFlowExecution);
	}

	@Test
	public void checkerRejectedTest() {
		// given
		checkerExecution.setStatus(WorkStatus.REJECTED);
		// when
		underTest.handlePostWorkFlowExecution();
		// then
		assertThat(workFlowExecution.getStatus(), equalTo(WorkStatus.FAILED));
	}

	@Test
	public void checkerFailedTest() {
		// given
		checkerExecution.setStatus(WorkStatus.FAILED);
		// when
		underTest.handlePostWorkFlowExecution();
		// then
		assertThat(workFlowExecution.getStatus(), equalTo(WorkStatus.IN_PROGRESS));
	}

}
