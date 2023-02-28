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
 * The WorkflowDefinitionException wraps unchecked standard Java exception and enriches
 * them with a custom error code. You can use this execution when a Workflow has not been
 * properly defined.
 *
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */

public class WorkflowDefinitionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WorkflowDefinitionException() {
	}

	public WorkflowDefinitionException(String message) {
		super(message);
	}

	public WorkflowDefinitionException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorkflowDefinitionException(Throwable cause) {
		super(cause);
	}

}
