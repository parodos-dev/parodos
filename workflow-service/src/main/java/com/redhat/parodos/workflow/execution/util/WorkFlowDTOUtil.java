package com.redhat.parodos.workflow.execution.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class WorkFlowDTOUtil {
    public static Map<String, Map<String, String>> convertWorkFlowTaskRequestDTOListToMap(List<WorkFlowRequestDTO.WorkFlowTaskRequestDTO> workFlowTaskRequestDTOs) {
        Map<String, Map<String, String>> output = new HashMap<>();
        workFlowTaskRequestDTOs.forEach(arg -> {
            Map<String, String> hm = new HashMap<>();
            arg.getArguments().forEach(i -> hm.put(i.getKey(), i.getValue()));
            output.put(arg.getName(), hm);
        });
        return output;
    }

    public static String writeObjectValueAsString(Object objectValue) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append((new ObjectMapper()).writeValueAsString(objectValue));
        } catch (JsonProcessingException e) {
            log.error("Error occurred in string conversion: {}", e.getMessage());
        }
        return sb.toString();
    }
}
