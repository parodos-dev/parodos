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

import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParallelFlowReportTest {

	private Exception exception;

	private ParallelFlowReport completedParallelFlowReport;

	private ParallelFlowReport failedParallelFlowReport;

	private ParallelFlowReport progressParallelFlowReport;

	@Before
	public void setUp() {
		exception = new Exception("test exception");
		WorkContext workContext = new WorkContext();

		completedParallelFlowReport = new ParallelFlowReport();
		completedParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		completedParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));

		failedParallelFlowReport = new ParallelFlowReport();
		failedParallelFlowReport.add(new DefaultWorkReport(WorkStatus.FAILED, workContext, exception));
		failedParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		failedParallelFlowReport.add(new DefaultWorkReport(WorkStatus.IN_PROGRESS, workContext));

		progressParallelFlowReport = new ParallelFlowReport();
		progressParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		progressParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
		progressParallelFlowReport.add(new DefaultWorkReport(WorkStatus.IN_PROGRESS, workContext));
		progressParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
	}

	@Test
	public void testGetStatus() {
		assertThat(completedParallelFlowReport.getStatus()).isEqualTo(WorkStatus.COMPLETED);
		assertThat(failedParallelFlowReport.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(progressParallelFlowReport.getStatus()).isEqualTo(WorkStatus.IN_PROGRESS);
	}

	@Test
	public void testGetError() {
		assertThat(failedParallelFlowReport.getError()).isEqualTo(exception);
	}

	@Test
	public void testGetReports() {
		assertThat(completedParallelFlowReport.getReports()).hasSize(2);
		assertThat(failedParallelFlowReport.getReports()).hasSize(3);
		assertThat(progressParallelFlowReport.getReports()).hasSize(4);
	}

}
