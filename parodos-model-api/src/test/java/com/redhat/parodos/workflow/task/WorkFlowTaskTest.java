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

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import lombok.NonNull;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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

		SequentialFlow flow = SequentialFlow.Builder.aNewSequentialFlow().named("test WorkFlow").execute((Work) task)
				.build();
		WorkReport report = flow.execute(context);

		assertTrue(task.isExecuted());
		assertEquals(WorkStatus.COMPLETED, report.getStatus());
		assertEquals(WorkFlowTaskStatus.COMPLETED, WorkFlowTaskStatus.valueOf(report.getStatus().name()));
	}

	@Test
	public void verifyParameter() throws MissingParameterException {
		TestTask task = new TestTask();
		WorkContext context = new WorkContext();
		Map<String, String> map = Map.of("username", "test");

		WorkContextDelegate.write(context, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, "Test",
				WorkContextDelegate.Resource.ARGUMENTS, map);

		BaseWorkFlowTask flowTask = (BaseWorkFlowTask) task;
		flowTask.setBeanName("Test");
		assertEquals("test", flowTask.getRequiredParameterValue(context, "username"));
	}

	@Test(expected = MissingParameterException.class)
	public void noParameters() throws MissingParameterException {
		TestTask task = new TestTask();
		WorkContext context = new WorkContext();
		WorkContextDelegate.write(context, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, "Test",
				WorkContextDelegate.Resource.ARGUMENTS, new HashMap<String, String>());

		BaseWorkFlowTask flowTask = (BaseWorkFlowTask) task;
		flowTask.setBeanName("Test");
		assertEquals("Test", flowTask.getRequiredParameterValue(context, "username"));
	}

	@Test
	public void compareOutpout() {
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
		assertThat(jsonSchema.get(params.get(1).getKey()).get("enum")).isInstanceOf(List.class)
				.isEqualTo(List.of("test1", "test2"));
	}

}
