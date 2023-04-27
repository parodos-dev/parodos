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
package com.redhat.parodos.patterndetection.clue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

/**
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
class ClueTest {

	private static final String MESSAGE_VALUE = "This clue works";
	private static final String MESSAGE = "message";

	@Test
	void testExecute() {
		// Define a mock implementation of the Clue interface
		Clue mockClue = new Clue() {
			@Override
			public WorkReport execute(WorkContext workContext) {
				workContext.put(MESSAGE, MESSAGE_VALUE);
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
			}
		};
		
		// Test that the execute method returns a WorkReport with a COMPLETED status and the message set in the WorkConext during execution is in the WorkReport Context
		WorkReport report = mockClue.execute(new WorkContext());
		assertEquals(WorkStatus.COMPLETED, report.getStatus());
		assertEquals(MESSAGE_VALUE, report.getWorkContext().get(MESSAGE));
	}

}
