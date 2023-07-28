/*
 * Copyright (c) 2023 Red Hat Developer
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
package com.redhat.parodos.workflow.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkFlowTaskTest {

	private class TestTask extends BaseWorkFlowTask {

		private boolean executed;

		@Override
		public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
			return List.of(
					WorkParameter.builder().key("username").type(WorkParameterType.TEXT).optional(false)
							.description("A username").build(),
					WorkParameter.builder().key("test-select").type(WorkParameterType.SELECT).optional(false)
							.description("A test").selectOptions(List.of("test1", "test2")).build());
		}

		@Override
		public @NonNull List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
			return List.of(WorkFlowTaskOutput.OTHER);
		}

		@Override
		public WorkReport execute(WorkContext workContext) {
			this.executed = true;
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}

		public boolean isExecuted() {
			return this.executed;
		}

	}

	@Test
	public void checkTask() throws MissingParameterException {
		TestTask task = new TestTask();
		WorkContext context = new WorkContext();

		SequentialFlow flow = SequentialFlow.Builder.aNewSequentialFlow().named("test WorkFlow").execute(task).build();
		WorkReport report = flow.execute(context);

		assertTrue(task.isExecuted());
		assertEquals(WorkStatus.COMPLETED, report.getStatus());
	}

	@Test
	public void verifyParameter() throws MissingParameterException {
		TestTask task = new TestTask();
		WorkContext context = new WorkContext();
		WorkContextDelegate.write(context, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());

		task.preExecute(context);
		Map<String, String> map = Map.of("username", "test");

		WorkContextDelegate.write(context, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, "Test",
				WorkContextDelegate.Resource.ARGUMENTS, map);

		task.setBeanName("Test");
		assertEquals("test", task.getRequiredParameterValue("username"));
	}

	@Test
	public void noParameters() throws MissingParameterException {
		WorkContext context = new WorkContext();
		WorkContextDelegate.write(context, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, "Test",
				WorkContextDelegate.Resource.ARGUMENTS, new HashMap<String, String>());

		WorkContextDelegate.write(context, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, "Test",
				WorkContextDelegate.Resource.ID, UUID.randomUUID());

		WorkContextDelegate.write(context, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());

		BaseWorkFlowTask flowTask = new TestTask();
		flowTask.preExecute(context);
		flowTask.setBeanName("Test");
		Throwable thrown = assertThrows(MissingParameterException.class, () -> {
			String username = flowTask.getRequiredParameterValue("username");
			assertThat("Test", equalTo(username));
		});
		assertThat("missing parameter(s) for ParameterName: username", equalTo(thrown.getMessage()));
	}

	@Test
	public void compareOutput() {
		TestTask task = new TestTask();

		assertNotEquals(WorkFlowTaskOutput.HTTP2XX, task.getWorkFlowTaskOutputs().get(0));
	}

	@Test
	public void checkTaskParams() {
		TestTask task = new TestTask();
		List<WorkParameter> params = task.getWorkFlowTaskParameters();

		assertEquals(WorkParameterType.TEXT, params.get(0).getType());
		assertEquals(WorkParameterType.SELECT, params.get(1).getType());

		HashMap<String, Map<String, Object>> jsonSchema = task.getAsJsonSchema();
		assertThat(jsonSchema.get(params.get(1).getKey()).get("enum"), instanceOf(List.class));
		assertThat((List) jsonSchema.get(params.get(1).getKey()).get("enum"), equalTo(List.of("test1", "test2")));
	}

}
