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
package com.redhat.parodos.workflow.exceptions;

/**
 * The WorkflowExecutionNotFoundException wraps unchecked standard Java exception and
 * enriches them with a custom error code. You can use this exception when a Workflow
 * Execution is not Found.
 *
 * @author Richard Wang (Github: richardW98)
 */

public class WorkflowExecutionNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WorkflowExecutionNotFoundException(String message) {
		super(message);
	}

}
