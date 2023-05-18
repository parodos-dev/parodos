package com.redhat.parodos.examples.vmonboarding.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AapGetJobResponseDto {

	private String status;

	private AapGetJobResponseArtifacts artifacts;

	@JsonProperty("extra_vars")
	private String extraVars;

}
