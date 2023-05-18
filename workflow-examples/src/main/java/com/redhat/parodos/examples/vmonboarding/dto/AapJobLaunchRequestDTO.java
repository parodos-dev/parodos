package com.redhat.parodos.examples.vmonboarding.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AapJobLaunchRequestDTO {

	private String limit;

	@JsonProperty("extra_vars")
	private Map<String, String> extraVars;

}