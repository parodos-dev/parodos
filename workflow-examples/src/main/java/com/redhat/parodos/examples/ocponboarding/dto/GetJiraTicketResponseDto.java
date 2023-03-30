package com.redhat.parodos.examples.ocponboarding.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetJiraTicketResponseDto {

	private String issueId;

	private String issueKey;

	private List<GetJiraTicketResponseValue> requestFieldValues;

	private JiraStatus currentStatus;

	@JsonProperty("_links")
	private Map<String, String> links;

}
