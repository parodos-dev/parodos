package com.redhat.parodos.workflow.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkFlowDTOUtilTest {

	@Test
	public void testConvertWorkFlowTaskToMapWithValidData() {
		// given
		WorkFlowRequestDTO.WorkRequestDTO workFlowRequestDTO = WorkFlowRequestDTO.WorkRequestDTO.builder()
				.workName("bar").arguments(List.of(getRandomArgument("key1"), getRandomArgument("key2"))).build();

		WorkFlowRequestDTO.WorkRequestDTO workFlowTaskRequestDTOB = WorkFlowRequestDTO.WorkRequestDTO.builder()
				.workName("foo").arguments(List.of(getRandomArgument("key1"))).build();

		// when
		Map<String, Map<String, String>> res = WorkFlowDTOUtil
				.convertWorkRequestDTOListToMap(List.of(workFlowRequestDTO, workFlowTaskRequestDTOB));

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
		WorkFlowRequestDTO.WorkRequestDTO workRequestDTO = WorkFlowRequestDTO.WorkRequestDTO.builder().workName("bar")
				.arguments(new ArrayList<>()).build();

		WorkFlowRequestDTO.WorkRequestDTO workRequestDTOB = WorkFlowRequestDTO.WorkRequestDTO.builder().workName("foo")
				.arguments(null).build();

		// when
		Map<String, Map<String, String>> res = WorkFlowDTOUtil
				.convertWorkRequestDTOListToMap(List.of(workRequestDTO, workRequestDTOB));

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

	WorkFlowRequestDTO.WorkRequestDTO.ArgumentRequestDTO getRandomArgument(String key) {
		return WorkFlowRequestDTO.WorkRequestDTO.ArgumentRequestDTO.builder().key(key).value(key).build();
	}

}
