package com.redhat.parodos.workflow.parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkParameterTypeTest {

	@Test
	public void testJsonSchema() {
		WorkParameterType[] inputValues = { WorkParameterType.PASSWORD, WorkParameterType.TEXT, WorkParameterType.EMAIL,
				WorkParameterType.NUMBER, WorkParameterType.URI, WorkParameterType.DATE, WorkParameterType.SELECT,
				WorkParameterType.MULTI_SELECT };

		List<Map<String, Object>> outputs = new ArrayList<>();
		for (WorkParameterType inputValue : inputValues) {
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
		expectedURLOutput.put("format", "uri");
		assertEquals(expectedURLOutput, outputs.get(4));

		Map<String, String> expectedDateOutput = new HashMap<>();
		expectedDateOutput.put("type", "string");
		expectedDateOutput.put("format", "date");
		assertEquals(expectedDateOutput, outputs.get(5));

		Map<String, String> expectedSelectOutput = new HashMap<>();
		expectedSelectOutput.put("type", "string");
		expectedSelectOutput.put("format", "select");
		assertTrue(WorkParameterType.SELECT.isSelect());
		assertEquals(expectedSelectOutput, outputs.get(6));

		Map<String, String> expectedMultiSelectOutput = new HashMap<>();
		expectedMultiSelectOutput.put("type", "string");
		expectedMultiSelectOutput.put("format", "multi-select");
		assertTrue(WorkParameterType.MULTI_SELECT.isSelect());
		assertEquals(expectedMultiSelectOutput, outputs.get(7));

	}

}
