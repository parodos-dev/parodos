/*
 * The MIT License
 *
 *  Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.redhat.parodos.workflows.workflow;

import java.util.Arrays;
import java.util.List;

import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class SequentialFlowTest {

	private final WorkContext workContext = mock(WorkContext.class);

	private final WorkReport completedWorkReport = new DefaultWorkReport(WorkStatus.COMPLETED, workContext);

	private final WorkReport failedWorkReport = new DefaultWorkReport(WorkStatus.FAILED, workContext);

	private final WorkReport progressWorkReport = new DefaultWorkReport(WorkStatus.IN_PROGRESS, workContext);

	private Work[] works;

	@BeforeEach
	public void initWorks() {
		works = new Work[] { mock(Work.class), mock(Work.class), mock(Work.class), mock(Work.class) };
	}

	@Test
	public void testExecuteWorksResultSuccess() {
		// Happy flow - all works completed
		prepareWorks(works, null, -1);
		executeWorks(works);
		validateWorks(works, -1);
	}

	@Test
	public void testExecuteWorksResultFail() {
		int failureWork = 0;
		prepareWorks(works, failedWorkReport, failureWork);
		executeWorks(works);
		validateWorks(works, failureWork);
	}

	@Test
	public void testExecuteWorksResultInProgress() {
		int failureWork = 2;
		prepareWorks(works, progressWorkReport, failureWork);
		executeWorks(works);
		validateWorks(works, failureWork);
	}

	@Test
	public void testExecuteWorksResultNull() {
		int failureWork = 1;
		prepareWorks(works, null, failureWork);
		executeWorks(works);
		validateWorks(works, failureWork);
	}

	@Test
	public void testExecuteWorkUnitsResultSuccess() {
		prepareWorks(works, null, -1);
		executeWorkUnits(works);
		validateWorks(works, -1);
	}

	@Test
	public void testExecuteWorkUnitsResultFail() {
		int failureWork = 0;
		prepareWorks(works, failedWorkReport, failureWork);
		executeWorkUnits(works);
		validateWorks(works, failureWork);
	}

	@Test
	public void testExecuteWorkUnitsResultInProgress() {
		int failureWork = 2;
		prepareWorks(works, progressWorkReport, failureWork);
		executeWorkUnits(works);
		validateWorks(works, failureWork);
	}

	@Test
	public void testExecuteWorkUnitsResultNull() {
		int failureWork = 1;
		prepareWorks(works, null, failureWork);
		executeWorkUnits(works);
		validateWorks(works, failureWork);
	}

	private void executeWorks(Work[] works) {
		SequentialFlow.Builder.aNewSequentialFlow().named("testFlow").execute(works[0]).then(works[1]).then(works[2])
				.then(works[3]).build().execute(workContext);
	}

	private void executeWorkUnits(Work[] works) {
		List<Work> initialWorkUnits = Arrays.asList(works[0], works[1]);
		List<Work> nextWorkUnits = Arrays.asList(works[2], works[3]);

		SequentialFlow.Builder.aNewSequentialFlow().named("testFlow").execute(initialWorkUnits).then(nextWorkUnits)
				.build().execute(workContext);
	}

	private void prepareWorks(Work[] works, WorkReport errReport, int failureWork) {
		for (int i = 0; i < works.length; i++) {
			WorkReport report = completedWorkReport;
			if (failureWork == i) {
				report = errReport;
			}
			when(works[i].execute(workContext)).thenReturn(report);
			int count = i + 1;
			when(works[i].getName()).thenReturn("work#" + count);
		}
	}

	private void validateWorks(Work[] works, int failureWork) {
		var inOrder = inOrder(works);
		for (int i = 0; i < works.length; i++) {
			int runCount = 1;
			if (failureWork >= 0 && failureWork < i) {
				runCount = 0;
			}
			inOrder.verify(works[i], times(runCount)).execute(workContext);
		}
	}

}
