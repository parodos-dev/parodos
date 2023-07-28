package com.redhat.parodos.workflow.parameter;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WorkParameterTest {

	private static final String VALUE_PROVIDER_NAME = "valueProviderName";

	String key = "key";

	String description = "key description";

	@Test
	public void testGetAsJsonSchemaWithValidData() {

		// given
		WorkParameter parameters = WorkParameter.builder().key(key).description(description)
				.valueProviderName(VALUE_PROVIDER_NAME).type(WorkParameterType.TEXT).build();
		// when
		Map<String, Object> result = parameters.getAsJsonSchema();
		// then
		assertNotNull(result);
		assertEquals(result.size(), 5);
		assertEquals(result.get("type"), "string");
		assertEquals(result.get("description"), description);
		assertEquals(result.get("valueProviderName"), VALUE_PROVIDER_NAME);
		assertEquals(result.get("format"), "text");
		assertEquals(result.get("required"), true);
	}

	@Test
	public void testGetAsJsonSchemaWithoutType() {
		// given
		WorkParameter parameters = WorkParameter.builder().key(key).description(description).build();
		// when
		Map<String, Object> result = parameters.getAsJsonSchema();
		// then
		assertNotNull(result);
		assertEquals(result.size(), 0);
	}

}
