package com.redhat.parodos.workflow.parameter;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkFlowParameterTypeTest extends TestCase {

	@Test
	public void testJsonSchema() {
		WorkFlowParameterType[] inputValues = { WorkFlowParameterType.PASSWORD, WorkFlowParameterType.TEXT,
				WorkFlowParameterType.EMAIL, WorkFlowParameterType.NUMBER, WorkFlowParameterType.URL,
				WorkFlowParameterType.DATE };

		List<Map<String, Object>> outputs = new ArrayList<>();
		for (WorkFlowParameterType inputValue : inputValues) {
			outputs.add(inputValue.getAsJsonSchema());
		}
		assertEquals(inputValues.length, outputs.size());

		Map<String, String> expectedPasswordOutput = new HashMap<>();
		expectedPasswordOutput.put("type", "string");
		expectedPasswordOutput.put("format", "password");
		assertEquals(expectedPasswordOutput, outputs.get(0));

		Map<String, String> expectedTextOutput = new HashMap<>();
		expectedTextOutput.put("type", "string");
		expectedTextOutput.put("format", "text");
		assertEquals(expectedTextOutput, outputs.get(1));

		Map<String, String> expectedEmailOutput = new HashMap<>();
		expectedEmailOutput.put("type", "string");
		expectedEmailOutput.put("format", "email");
		assertEquals(expectedEmailOutput, outputs.get(2));

		Map<String, String> expectedNumberOutput = new HashMap<>();
		expectedNumberOutput.put("type", "number");
		assertEquals(expectedNumberOutput, outputs.get(3));

		Map<String, String> expectedURLOutput = new HashMap<>();
		expectedURLOutput.put("type", "string");
		expectedURLOutput.put("format", "url");
		assertEquals(expectedURLOutput, outputs.get(4));

		Map<String, String> expectedDateOutput = new HashMap<>();
		expectedDateOutput.put("type", "string");
		expectedDateOutput.put("format", "date");
		assertEquals(expectedDateOutput, outputs.get(5));

	}

}