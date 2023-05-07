/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.workflow.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * DTO util class for request and response objects conversion
 *
 * @author Annel Ketcha (Github: anludke)
 * @author Richard Wang (Github: richardW98)
 */

@Slf4j
public class WorkFlowDTOUtil {

	public static Map<String, String> convertArgumentListToMap(
			List<WorkFlowRequestDTO.WorkRequestDTO.ArgumentRequestDTO> argumentRequestDTOList) {
		return argumentRequestDTOList.stream()
				.collect(Collectors.toMap(WorkFlowRequestDTO.WorkRequestDTO.ArgumentRequestDTO::getKey,
						WorkFlowRequestDTO.WorkRequestDTO.ArgumentRequestDTO::getValue));
	}

	public static Map<String, String> convertWorkRequestDTOToMap(WorkFlowRequestDTO.WorkRequestDTO workRequestDTOS) {
		Map<String, String> hm = new HashMap<>();
		workRequestDTOS.getArguments().forEach(i -> hm.put(i.getKey(), i.getValue()));
		return hm;
	}

	public static Map<String, Map<String, String>> convertWorkRequestDTOListToMap(
			List<WorkFlowRequestDTO.WorkRequestDTO> workRequestDTOS) {
		Map<String, Map<String, String>> output = new HashMap<>();
		workRequestDTOS.forEach(arg -> {
			Map<String, String> hm = new HashMap<>();
			Optional.ofNullable(arg.getArguments())
					.ifPresent(item -> item.forEach(i -> hm.put(i.getKey(), i.getValue())));
			output.put(arg.getWorkName(), hm);
		});
		return output;
	}

	public static String writeObjectValueAsString(Object objectValue) {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append((new ObjectMapper()).writeValueAsString(objectValue));
		}
		catch (JsonProcessingException e) {
			log.error("Error occurred in string conversion: {}", e.getMessage());
		}
		return sb.toString();
	}

	public static <T> T readStringAsObject(String stringValue, TypeReference<T> typeReference, T defaultValue) {
		try {
			return (new ObjectMapper()).readValue(stringValue, typeReference);
		}
		catch (JsonProcessingException e) {
			log.error("Error occurred in object conversion: {}", e.getMessage());
		}
		return defaultValue;
	}

}
