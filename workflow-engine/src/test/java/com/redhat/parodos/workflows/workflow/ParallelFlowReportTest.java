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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ParallelFlowReportTest {

	private Exception exception;

	private ParallelFlowReport completedParallelFlowReport;

	private ParallelFlowReport failedParallelFlowReport;

	private ParallelFlowReport progressParallelFlowReport;

	@BeforeEach
	public void setUp() {
		exception = new Exception("test exception");
		WorkContext workContext = new WorkContext();

		completedParallelFlowReport = new ParallelFlowReport();
		completedParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext, "alertMessage_1"));
		completedParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext, "alertMessage_2"));

		failedParallelFlowReport = new ParallelFlowReport();
		failedParallelFlowReport.add(new DefaultWorkReport(WorkStatus.FAILED, workContext, exception));
		failedParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext, "alertMessage_1"));
		failedParallelFlowReport.add(new DefaultWorkReport(WorkStatus.IN_PROGRESS, workContext));

		progressParallelFlowReport = new ParallelFlowReport();
		progressParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext, "alertMessage_1"));
		progressParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext, "alertMessage_2"));
		progressParallelFlowReport.add(new DefaultWorkReport(WorkStatus.IN_PROGRESS, workContext));
		progressParallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext, "alertMessage_3"));
	}

	@Test
	public void testGetStatus() {
		assertThat(completedParallelFlowReport.getStatus(), equalTo(WorkStatus.COMPLETED));
		assertThat(failedParallelFlowReport.getStatus(), equalTo(WorkStatus.FAILED));
		assertThat(progressParallelFlowReport.getStatus(), equalTo(WorkStatus.IN_PROGRESS));
	}

	@Test
	public void testGetError() {
		assertThat(failedParallelFlowReport.getError(), equalTo(exception));
	}

	@Test
	public void testGetReports() {
		assertThat(completedParallelFlowReport.getReports(), hasSize(2));
		assertThat(failedParallelFlowReport.getReports(), hasSize(3));
		assertThat(progressParallelFlowReport.getReports(), hasSize(4));
	}

	@Test
	public void testGetAlertMessage() {
		assertThat(completedParallelFlowReport.getAlertMessage(), equalTo("alertMessage_2"));
		assertThat(failedParallelFlowReport.getAlertMessage(), is(nullValue()));
		assertThat(progressParallelFlowReport.getAlertMessage(), equalTo("alertMessage_3"));
	}

}
