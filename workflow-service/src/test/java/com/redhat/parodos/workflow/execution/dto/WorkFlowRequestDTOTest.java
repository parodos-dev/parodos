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
package com.redhat.parodos.workflow.execution.dto;

import java.util.List;

import com.redhat.parodos.workflow.enums.WorkType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Workflow request dto test
 *
 * @author Richard Wang (Github: richardw98)
 */

class WorkFlowRequestDTOTest {

	private static final String TEST_WORK_TASK = "test-work-task";

	private static final String TEST_WORKFLOW_EXECUTION_REQUEST = "test-workflow-execution-request";

	private static final String MAIN_WORKFLOW_PARAM_1_KEY = "main-workflow-param-1-key";

	private static final String MAIN_WORKFLOW_PARAM_1_VALUE = "main-workflow-param-1-value";

	private static final String TEST_WORKFLOW = "test-workflow";

	private static final String WORK_PARAM_TASK_KEY = "work-param-task-key";

	private static final String WORK_PARAM_TASK_VALUE = "work-param-task-value";

	@Test
	void findWorkByName_whenWorkIsFound_then_shouldReturnWork() {
		WorkFlowRequestDTO request = getSimpleWorkFlowRequestDTO();
		WorkFlowRequestDTO.WorkRequestDTO subWorkflow = request.findFirstWorkByName(TEST_WORKFLOW);

		WorkFlowRequestDTO.WorkRequestDTO task = subWorkflow.findFirstWorkByName(TEST_WORK_TASK);

		assertNull(request.findFirstWorkByName("test"));
		assertNull(subWorkflow.findFirstWorkByName("test"));

		assertThat(subWorkflow, is(notNullValue()));
		assertThat(subWorkflow.getType(), equalTo((WorkType.WORKFLOW)));
		assertThat(subWorkflow.getArguments(), hasSize(0));

		assertThat(task, is(notNullValue()));
		assertThat(task.getType(), equalTo(WorkType.TASK));
		assertThat(task.getArguments(), hasSize(1));

	}

	private WorkFlowRequestDTO getSimpleWorkFlowRequestDTO() {
		return WorkFlowRequestDTO.builder().workFlowName(TEST_WORKFLOW_EXECUTION_REQUEST)
				.arguments(List.of(WorkFlowRequestDTO.WorkRequestDTO.ArgumentRequestDTO.builder()
						.key(MAIN_WORKFLOW_PARAM_1_KEY).value(MAIN_WORKFLOW_PARAM_1_VALUE).build()))
				.works(List
						.of(getSimpleWorkRequestDTO(TEST_WORKFLOW, WorkType.WORKFLOW, List.of(),
								getSimpleWorkRequestDTO(TEST_WORK_TASK, WorkType.TASK,
										List.of(WorkFlowRequestDTO.WorkRequestDTO.ArgumentRequestDTO.builder()
												.key(WORK_PARAM_TASK_KEY).value(WORK_PARAM_TASK_VALUE).build())))))
				.build();
	}

	private WorkFlowRequestDTO.WorkRequestDTO getSimpleWorkRequestDTO(String workName, WorkType type,
			List<WorkFlowRequestDTO.WorkRequestDTO.ArgumentRequestDTO> arguments,
			WorkFlowRequestDTO.WorkRequestDTO... works) {
		return WorkFlowRequestDTO.WorkRequestDTO.builder().workName(workName).arguments(arguments).type(type)
				.works((WorkType.WORKFLOW == type ? List.of(works) : null)).build();
	}

}
