package com.redhat.parodos.examples.ocponboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetJiraTicketResponseValue {

	private String fieldId;

	private String label;

	private Object value;

}
