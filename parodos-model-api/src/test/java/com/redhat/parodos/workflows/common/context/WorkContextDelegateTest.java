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
package com.redhat.parodos.workflows.common.context;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WorkContextDelegateTest {

	private static final String KEY = "test";

	private static final String VALUE = "value";

	private final static String DEFINITION_NAME = "testName";

	private final static String UUID = randomUUID().toString();

	@Test
	public void checkContextWithNameResource() {
		WorkContext workContext = new WorkContext();

		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_DEFINITION,
				DEFINITION_NAME, WorkContextDelegate.Resource.NAME, DEFINITION_NAME);

		String value = WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_DEFINITION,
				DEFINITION_NAME, WorkContextDelegate.Resource.NAME).toString();

		assertEquals(DEFINITION_NAME, value);
	}

	@Test
	public void checkContextResource() {
		WorkContext workContext = new WorkContext();

		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID);

		String value = WorkContextDelegate
				.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION, WorkContextDelegate.Resource.ID)
				.toString();

		assertEquals(UUID, value);
	}

	@Test
	public void failToGetValue() throws MissingParameterException {
		WorkContext workContext = new WorkContext();

		assertThrows(MissingParameterException.class,
				() -> WorkContextDelegate.getRequiredValueFromRequestParams(workContext, KEY));
	}

	@Test
	public void checkGetOptionalValue() throws MissingParameterException {
		WorkContext context = new WorkContext();

		String value = WorkContextDelegate.getOptionalValueFromRequestParams(context, KEY, VALUE);
		assertEquals(VALUE, value);
	}

	@Test
	public void checkFetchingValue() throws MissingParameterException {
		WorkContext context = new WorkContext();
		context.put(KEY, "value2");

		String value = WorkContextDelegate.getOptionalValueFromRequestParams(context, KEY, VALUE);
		assertEquals("value2", value);
	}

	@Test
	public void checkFetchingRequiredValue() throws MissingParameterException {
		WorkContext context = new WorkContext();
		context.put(KEY, VALUE);

		String value = WorkContextDelegate.getRequiredValueFromRequestParams(context, KEY);
		assertEquals(VALUE, value);
	}

}
