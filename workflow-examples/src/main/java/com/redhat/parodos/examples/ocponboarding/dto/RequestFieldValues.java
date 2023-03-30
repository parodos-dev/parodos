package com.redhat.parodos.examples.ocponboarding.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class RequestFieldValues {

	private String summary;

	@JsonProperty("customfield_10003")
	private List<JiraUser> approvers;

	@JsonProperty("customfield_10065")
	private String projectName;

	@JsonProperty("customfield_10066")
	private String namespace;

	@Builder
	@Data
	public static class JiraUser {

		private String accountId;

	}

}
