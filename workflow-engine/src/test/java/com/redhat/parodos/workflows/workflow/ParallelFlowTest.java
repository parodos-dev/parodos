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

import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ParallelFlowTest {

	@Test
	public void testExecute() {
		// given
		Work work1 = mock(Work.class);
		Work work2 = mock(Work.class);
		WorkContext workContext = mock(WorkContext.class);
		ParallelFlowExecutor parallelFlowExecutor = mock(ParallelFlowExecutor.class);
		List<Work> works = Arrays.asList(work1, work2);
		ParallelFlow parallelFlow = new ParallelFlow("pf", works, parallelFlowExecutor);

		// when
		ParallelFlowReport parallelFlowReport = parallelFlow.execute(workContext);

		// then
		assertThat(parallelFlowReport, is(notNullValue()));
		verify(parallelFlowExecutor).executeInParallel(works, workContext);
	}

}
