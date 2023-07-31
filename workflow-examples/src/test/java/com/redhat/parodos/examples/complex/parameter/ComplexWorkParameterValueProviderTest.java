package com.redhat.parodos.examples.complex.parameter;

import java.util.List;

import com.redhat.parodos.workflow.parameter.WorkParameterValueRequest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ComplexWorkParameterValueProviderTest {

	private static final String WORKFLOW_NAME = "test-workflow";

	public static final String INVALID_WORKFLOW = "invalid-workflow";

	private ComplexWorkParameterValueProvider complexWorkParameterValueProvider = new ComplexWorkParameterValueProvider(
			WORKFLOW_NAME);

	@Test
	public void getValuesForWorkflow_whenWorkFlowNameNotMatch_then_returnEmptyList() {
		assertThat(
				complexWorkParameterValueProvider.getValuesForWorkflow(INVALID_WORKFLOW, List.of(
						WorkParameterValueRequest.builder().key("WORKFLOW_SELECT_SAMPLE").value("option2").build())),
				hasSize(0));
	}

	@Test
	public void getValuesForWorkflow_whenWorkFlowNameNotMatch_then_returnList() {
		assertThat(
				complexWorkParameterValueProvider.getValuesForWorkflow(WORKFLOW_NAME, List.of(
						WorkParameterValueRequest.builder().key("WORKFLOW_SELECT_SAMPLE").value("option2").build())),
				hasSize(2));
	}

}
