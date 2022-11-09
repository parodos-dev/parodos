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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.execution.WorkFlowTransactionEntity;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static com.redhat.parodos.workflows.WorkFlowConstants.WORKFLOW_EXECUTION_ENTITY_REFERENCE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for InfrastructureWorkFlowController
 *
 * @author Richard Wang (Github: RichardW98)
 */
@WebMvcTest(InfrastructureWorkFlowController.class)
@AutoConfigureMockMvc(addFilters = false)
class InfrastructureWorkFlowControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private WorkFlowTransactionEntity entity;

    @MockBean
    private InfrastructureWorkFlowService InfrastructureWorkFlowService;

    @Test
    void executeWorkFlow_success_shouldReturn_Uri() throws Exception {
        String uuid = "a871ba7b-e27b-40ba-b28d-17092a83b213";
        when(entity.getId()).thenReturn(UUID.fromString(uuid));
        WorkContext workContext = new WorkContext();
        workContext.put(WORKFLOW_EXECUTION_ENTITY_REFERENCE, entity);
        WorkReport workReport = new DefaultWorkReport(WorkStatus.COMPLETED, workContext);

        when(InfrastructureWorkFlowService.execute(any())).thenReturn(workReport);
        mockMvc.perform(
                        post("/api/v1/workflows/infrastructures/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(Map.of("workFlowId", "test", "workFlowParameters", Map.of()))))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/workflows/transactions/" + uuid));
        verify(InfrastructureWorkFlowService, times(1)).execute(any());
    }
}
