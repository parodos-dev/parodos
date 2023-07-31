package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflows.work.WorkReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(SpringExtension.class)
public class DefaultWorkFlowPostInterceptorTest {

	@Mock
	private WorkFlowServiceImpl workFlowService;

	@Mock
	private WorkFlowExecution workFlowExecution;

	@Test
	public void handlePostWorkFlowExecution() {
		// given
		DefaultWorkFlowPostInterceptor underTest = new DefaultWorkFlowPostInterceptor(workFlowService,
				workFlowExecution);

		// when
		WorkReport workReport = underTest.handlePostWorkFlowExecution();

		// then
		assertThat(workReport, is(nullValue()));
	}

}
