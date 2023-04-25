package com.redhat.parodos.workflow.definition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkParameterValueRequestDTO {

	private String key;

	private String value;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String workName;

}
