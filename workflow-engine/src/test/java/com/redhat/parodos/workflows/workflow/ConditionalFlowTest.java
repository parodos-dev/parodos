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

import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReportPredicate;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ConditionalFlowTest {

	@Test
	public void callOnPredicateSuccess() {
		// given
		Work toExecute = mock(Work.class);
		Work nextOnPredicateSuccess = mock(Work.class);
		Work nextOnPredicateFailure = mock(Work.class);
		WorkContext workContext = mock(WorkContext.class);
		WorkReportPredicate predicate = WorkReportPredicate.ALWAYS_TRUE;
		ConditionalFlow conditionalFlow = ConditionalFlow.Builder.aNewConditionalFlow().named("testFlow")
				.execute(toExecute).when(predicate).then(nextOnPredicateSuccess).otherwise(nextOnPredicateFailure)
				.build();

		// when
		conditionalFlow.execute(workContext);

		// then
		verify(toExecute, times(1)).execute(workContext);
		verify(nextOnPredicateSuccess, times(1)).execute(workContext);
		verify(nextOnPredicateFailure, never()).execute(workContext);
	}

	@Test
	public void callOnPredicateFailure() {
		// given
		Work toExecute = mock(Work.class);
		Work nextOnPredicateSuccess = mock(Work.class);
		Work nextOnPredicateFailure = mock(Work.class);
		WorkContext workContext = mock(WorkContext.class);
		WorkReportPredicate predicate = WorkReportPredicate.ALWAYS_FALSE;
		ConditionalFlow conditionalFlow = ConditionalFlow.Builder.aNewConditionalFlow().named("anotherTestFlow")
				.execute(toExecute).when(predicate).then(nextOnPredicateSuccess).otherwise(nextOnPredicateFailure)
				.build();

		// when
		conditionalFlow.execute(workContext);

		// then
		verify(toExecute, times(1)).execute(workContext);
		verify(nextOnPredicateFailure, times(1)).execute(workContext);
		verify(nextOnPredicateSuccess, never()).execute(workContext);
	}

}
