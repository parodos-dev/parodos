package com.redhat.parodos.workflow.definition.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.redhat.parodos.workflow.parameter.WorkParameterValueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkParameterValueResponseDTO {

	private String key;

	private List<String> options;

	private String value;

	private String propertyPath;

	public static WorkParameterValueResponseDTO convertToDto(WorkParameterValueResponse workParameterValueResponse,
			String propertyPath) {
		return WorkParameterValueResponseDTO.builder().key(workParameterValueResponse.getKey())
				.options(workParameterValueResponse.getOptions()).value(workParameterValueResponse.getValue())
				.propertyPath(propertyPath).build();
	}

}
