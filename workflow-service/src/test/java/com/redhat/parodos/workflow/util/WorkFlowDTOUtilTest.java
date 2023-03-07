package com.redhat.parodos.workflow.util;

import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkFlowDTOUtilTest {

	@Test
	public void testConvertWorkFlowTaskToMapWithValidData() {
		// given
		WorkFlowRequestDTO.WorkFlowTaskRequestDTO workFlowTaskRequestDTO = WorkFlowRequestDTO.WorkFlowTaskRequestDTO
				.builder().name("bar").arguments(List.of(getRandomArgument("key1"), getRandomArgument("key2"))).build();

		WorkFlowRequestDTO.WorkFlowTaskRequestDTO workFlowTaskRequestDTOB = WorkFlowRequestDTO.WorkFlowTaskRequestDTO
				.builder().name("foo").arguments(List.of(getRandomArgument("key1"))).build();

		// when
		Map<String, Map<String, String>> res = WorkFlowDTOUtil
				.convertWorkFlowTaskRequestDTOListToMap(List.of(workFlowTaskRequestDTO, workFlowTaskRequestDTOB));

		// then
		assertNotNull(res);
		assertEquals(res.size(), 2);
		assertEquals(res.get("bar").size(), 2);
		assertEquals(res.get("bar").get("key1"), "key1");
		assertEquals(res.get("bar").get("key2"), "key2");

		assertEquals(res.get("foo").size(), 1);
		assertEquals(res.get("foo").get("key1"), "key1");
	}

	@Test
	public void testConvertWorkFlowTaskToMapWithoutTaskArguments() {
		// given
		WorkFlowRequestDTO.WorkFlowTaskRequestDTO workFlowTaskRequestDTO = WorkFlowRequestDTO.WorkFlowTaskRequestDTO
				.builder().name("bar").arguments(new ArrayList<>()).build();

		WorkFlowRequestDTO.WorkFlowTaskRequestDTO workFlowTaskRequestDTOB = WorkFlowRequestDTO.WorkFlowTaskRequestDTO
				.builder().name("foo").arguments(null).build();

		// when
		Map<String, Map<String, String>> res = WorkFlowDTOUtil
				.convertWorkFlowTaskRequestDTOListToMap(List.of(workFlowTaskRequestDTO, workFlowTaskRequestDTOB));

		// then
		assertNotNull(res);
		assertEquals(res.size(), 2);
		assertEquals(res.get("bar").size(), 0);
		assertEquals(res.get("foo").size(), 0);
	}

	@Test
	public void testWriteOjectValueAsString() {
		// when
		String res = WorkFlowDTOUtil.writeObjectValueAsString(getRandomArgument("foo"));

		// then
		assertEquals(res, "{\"key\":\"foo\",\"value\":\"foo\"}");
	}

	@Test
	public void testWriteOjectValueAsStringWithoutObject() {
		// when
		String res = WorkFlowDTOUtil.writeObjectValueAsString(null);

		// then
		assertEquals(res, "null");
	}

	WorkFlowRequestDTO.WorkFlowTaskRequestDTO.ArgumentRequestDTO getRandomArgument(String key) {
		return WorkFlowRequestDTO.WorkFlowTaskRequestDTO.ArgumentRequestDTO.builder().key(key).value(key).build();
	}

}