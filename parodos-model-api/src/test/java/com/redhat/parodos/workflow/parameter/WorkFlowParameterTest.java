package com.redhat.parodos.workflow.parameter;

import junit.framework.TestCase;

import java.util.Map;

public class WorkFlowParameterTest extends TestCase {

	String key = "key";

	String description = "key description";

	public void testGetAsJsonSchemaWithValidData() {

		// given
		WorkFlowParameter parameters = WorkFlowParameter.builder().key(key).description(description)
				.type(WorkFlowParameterType.TEXT).build();
		// when
		Map<String, Object> result = parameters.getAsJsonSchema();
		// then
		assertNotNull(result);
		assertEquals(result.size(), 4);
		assertEquals(result.get("type"), "string");
		assertEquals(result.get("description"), description);
		assertEquals(result.get("format"), "text");
		assertEquals(result.get("required"), true);
	}

	public void testGetAsJsonSchemaWithoutType() {
		// given
		WorkFlowParameter parameters = WorkFlowParameter.builder().key(key).description(description).build();
		// when
		Map<String, Object> result = parameters.getAsJsonSchema();
		// then
		assertNotNull(result);
		assertEquals(result.size(), 0);
	}

}