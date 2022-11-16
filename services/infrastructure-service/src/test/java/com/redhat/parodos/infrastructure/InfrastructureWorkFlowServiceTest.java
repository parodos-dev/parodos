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
package com.redhat.parodos.infrastructure;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;

import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.WorkFlowTaskParameterType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.redhat.parodos.workflow.BeanWorkFlowRegistryImpl;
import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.WorkFlowEngine;
import com.redhat.parodos.workflows.WorkFlowExecuteRequestDto;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

/**
 * Test class for InfrastructureWorkFlowService
 *
 * @author Richard Wang (Github: RichardW98)
 */
@ExtendWith(SpringExtension.class)
class InfrastructureWorkFlowServiceTest {

    @MockBean
    private WorkFlowEngine workFlowEngine;

    @MockBean
    private BeanWorkFlowRegistryImpl beanWorkflowRegistry;

    @MockBean
    private WorkFlowDelegate workFlowDelegate;

    private InfrastructureWorkFlowService infrastructureWorkFlowService;

    private List<WorkFlow> workFlows = List.of(
            SequentialFlow.Builder
                    .aNewSequentialFlow()
                    .named("test_fail_INFRASTRUCTURE_TASK_WORKFLOW")
                    .execute(new TestFailedWork())
                    .build(),
            SequentialFlow.Builder
                    .aNewSequentialFlow()
                    .named("test_success_INFRASTRUCTURE_TASK_WORKFLOW")
                    .execute(new TestSuccessWork())
                    .build()
    );

    @BeforeEach
    void init() {
        infrastructureWorkFlowService = new InfrastructureWorkFlowService(workFlowEngine, beanWorkflowRegistry, workFlowDelegate);
    }


    @Test
    void execute_whenWorkFlowNotFound_shouldReturn_Failed() {
        Mockito.when(workFlowDelegate.getWorkFlowById(any())).thenReturn(null);
        WorkFlowExecuteRequestDto dto = new WorkFlowExecuteRequestDto();
        dto.setWorkFlowId("test");
        assertEquals(WorkStatus.FAILED, infrastructureWorkFlowService.execute(dto).getStatus());
        verify(workFlowEngine, times(0)).executeWorkFlows(any(), any());
    }

    @Test
    void execute_whenWorkFlowIsFailed_shouldReturn_Failed() {
        WorkFlowExecuteRequestDto dto = new WorkFlowExecuteRequestDto();
        dto.setWorkFlowId("test_fail_INFRASTRUCTURE_TASK_WORKFLOW");
        when(workFlowDelegate.getWorkContextWithParameters(any())).thenReturn(new WorkContext());
        when(workFlowDelegate.getWorkFlowById(any())).thenReturn(workFlows.get(0));
        WorkContext workContext = new WorkContext();
        when(workFlowEngine.executeWorkFlows(any(), any())).thenReturn(new DefaultWorkReport(WorkStatus.FAILED, workContext));
        assertEquals(WorkStatus.FAILED, infrastructureWorkFlowService.execute(dto).getStatus());
        verify(workFlowEngine, times(1)).executeWorkFlows(any(), any());
    }

    @Test
    void execute_whenWorkFlowIsFailed_shouldReturn_Rollback() {
        WorkFlowExecuteRequestDto dto = new WorkFlowExecuteRequestDto();
        WorkContext workContext = new WorkContext();
        workContext.put(WorkFlowConstants.ROLL_BACK_WORKFLOW_NAME, "test_success_INFRASTRUCTURE_TASK_WORKFLOW");
        dto.setWorkFlowId("test_fail_INFRASTRUCTURE_TASK_WORKFLOW");
        when(workFlowDelegate.getWorkContextWithParameters(any())).thenReturn(workContext);
        when(workFlowDelegate.getWorkFlowById(any())).thenReturn(workFlows.get(0));
        when(workFlowEngine.executeWorkFlows(any(), any())).thenReturn(new DefaultWorkReport(WorkStatus.FAILED, workContext));
        assertEquals(WorkStatus.FAILED, infrastructureWorkFlowService.execute(dto).getStatus());
        verify(workFlowEngine, times(2)).executeWorkFlows(any(), any());
    }



    @Test
    void execute_whenWorkFlowIsCompleted_shouldReturn_entity() {
    	Mockito.when(workFlowDelegate.getWorkContextWithParameters(any())).thenReturn(new WorkContext());
        WorkFlowExecuteRequestDto dto = new WorkFlowExecuteRequestDto();
        dto.setWorkFlowId("test_success_INFRASTRUCTURE_TASK_WORKFLOW");
        when(workFlowDelegate.getWorkFlowById(any())).thenReturn(workFlows.get(1));
        WorkContext workContext = new WorkContext();
        when(workFlowEngine.executeWorkFlows(any(), any())).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
        when(workFlowDelegate.getWorkContextWithParameters(any())).thenReturn(workContext);
        assertEquals(WorkStatus.COMPLETED, infrastructureWorkFlowService.execute(dto).getStatus());
        verify(workFlowEngine, times(1)).executeWorkFlows(any(), any());
    }

    @Test
    void getInfraStructureTaskWorkFlows_whenWorkflowIsFound_thenReturn_workflow() {
        List<String> workflowNames = List.of("test-flow");
        when(workFlowDelegate.getWorkFlowIdsByWorkFlowType("test-type")).thenReturn(workflowNames);
        assertThat(infrastructureWorkFlowService.getInfraStructureTaskWorkFlows("test-type"))
                .hasSize(1)
                .contains("test-flow");
    }

    @Test
    void getWorkFlowParametersForWorkFlow_whenWorkflowIsFound_thenReturn_workflowParameters() {
        String id = "test_id";
        when(workFlowDelegate.getWorkFlowParametersForWorkFlow(id)).thenReturn(List.of(
                WorkFlowTaskParameter.builder()
                        .key("param_1")
                        .type(WorkFlowTaskParameterType.TEXT)
                        .optional(false)
                        .build(),
                WorkFlowTaskParameter.builder()
                        .key("param_2")
                        .type(WorkFlowTaskParameterType.PASSWORD)
                        .optional(true)
                        .build()));
        assertThat(infrastructureWorkFlowService.getWorkFlowParametersForWorkFlow(id))
                .hasSize(2)
                .extracting(WorkFlowTaskParameter::getKey)
                .containsExactlyInAnyOrder("param_1", "param_2");
    }

    private static class TestFailedWork implements Work {
        public WorkReport execute(WorkContext workContext) {
            return new DefaultWorkReport(WorkStatus.FAILED, workContext);
        }
    }

    private static class TestSuccessWork implements Work {
        public WorkReport execute(WorkContext workContext) {
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        }
    }
}
