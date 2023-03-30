package com.redhat.parodos.examples.ocponboarding.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateJiraTicketResponseDto {

	private String issueId;

	private String issueKey;

	@JsonProperty("_links")
	private Map<String, String> links;

}
