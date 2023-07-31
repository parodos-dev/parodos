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
package com.redhat.parodos.workflow;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.dto.WorkDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.registry.BeanWorkFlowRegistryImpl;
import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

/**
 * unit test for WorkFlowDelegate
 *
 * @author Richard Wang (Github: richardw98)
 */

@ExtendWith(SpringExtension.class)
class WorkFlowDelegateTest {

	@Mock
	private BeanWorkFlowRegistryImpl beanWorkFlowRegistry;

	private final WorkFlowDelegate workFlowDelegate = new WorkFlowDelegate(beanWorkFlowRegistry);

	private static final UUID TEST_PROJECT_ID = UUID.randomUUID();

	private static final String TEST_WORKFLOW_NAME = "test-workflow";

	private static final String TEST_SUB_WORKFLOW_NAME = "test-sub-workflow";

	private static final String TEST_TASK_NAME = "test-task";

	private static final String TEST_WORKFLOW_ARG_KEY = "test-workflow-key";

	private static final String TEST_WORKFLOW_ARG_VALUE = "test-workflow-value";

	private static final String TEST_SUB_WORKFLOW_ARG_KEY = "test-sub-workflow-key";

	private static final String TEST_SUB_WORKFLOW_ARG_VALUE = "test-sub-workflow-value";

	private static final String TEST_TASK_ARG_KEY = "test-task-key";

	private static final String TEST_TASK_ARG_VALUE = "test-task-value";

	private final WorkFlowRequestDTO workFlowRequestDTO = WorkFlowRequestDTO.builder().projectId(TEST_PROJECT_ID)
			.workFlowName(TEST_WORKFLOW_NAME)
			.arguments(List.of(WorkFlowRequestDTO.WorkRequestDTO.ArgumentRequestDTO.builder().key(TEST_WORKFLOW_ARG_KEY)
					.value(TEST_WORKFLOW_ARG_VALUE).build()))
			.works(List.of(WorkFlowRequestDTO.WorkRequestDTO.builder().workName(TEST_SUB_WORKFLOW_NAME)
					.arguments(List.of(WorkFlowRequestDTO.WorkRequestDTO.ArgumentRequestDTO.builder()
							.key(TEST_SUB_WORKFLOW_ARG_KEY).value(TEST_SUB_WORKFLOW_ARG_VALUE).build()))
					.type(WorkType.WORKFLOW)
					.works(List.of(WorkFlowRequestDTO.WorkRequestDTO.builder().workName(TEST_TASK_NAME)
							.arguments(List.of(WorkFlowRequestDTO.WorkRequestDTO.ArgumentRequestDTO.builder()
									.key(TEST_TASK_ARG_KEY).value(TEST_TASK_ARG_VALUE).build()))
							.type(WorkType.TASK).build()))
					.build()))
			.build();

	@Test
	void initWorkFlowContext_when_workflowParameterIsFound_thenReturn_success() {
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = sampleWorkflowDefinitionResponse();

		WorkContext workContext = workFlowDelegate.initWorkFlowContext(workFlowRequestDTO,
				workFlowDefinitionResponseDTO);

		assertThat(new ObjectMapper().convertValue(
				WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
						TEST_WORKFLOW_NAME, WorkContextDelegate.Resource.ARGUMENTS),
				new TypeReference<HashMap<String, String>>() {
				}), hasEntry(TEST_WORKFLOW_ARG_KEY, TEST_WORKFLOW_ARG_VALUE));

		assertThat(new ObjectMapper().convertValue(
				WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
						TEST_SUB_WORKFLOW_NAME, WorkContextDelegate.Resource.ARGUMENTS),
				new TypeReference<HashMap<String, String>>() {
				}), hasEntry(TEST_SUB_WORKFLOW_ARG_KEY, TEST_SUB_WORKFLOW_ARG_VALUE));

		assertThat(new ObjectMapper().convertValue(
				WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
						TEST_TASK_NAME, WorkContextDelegate.Resource.ARGUMENTS),
				new TypeReference<HashMap<String, String>>() {
				}), hasEntry(TEST_TASK_ARG_KEY, TEST_TASK_ARG_VALUE));
	}

	private WorkFlowDefinitionResponseDTO sampleWorkflowDefinitionResponse() {
		LinkedHashSet<WorkDefinitionResponseDTO> workFlowWorks = new LinkedHashSet<>();
		LinkedHashSet<WorkDefinitionResponseDTO> taskWorks = new LinkedHashSet<>();

		taskWorks.add(WorkDefinitionResponseDTO.builder().name(TEST_TASK_NAME).workType(WorkType.TASK).build());
		workFlowWorks.add(WorkDefinitionResponseDTO.builder().name(TEST_SUB_WORKFLOW_NAME).workType(WorkType.WORKFLOW)
				.works(taskWorks).build());
		return WorkFlowDefinitionResponseDTO.builder().name(TEST_WORKFLOW_NAME).works(workFlowWorks).build();
	}

}
