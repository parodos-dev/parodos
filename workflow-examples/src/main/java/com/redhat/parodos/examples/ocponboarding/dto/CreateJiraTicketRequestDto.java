package com.redhat.parodos.examples.ocponboarding.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CreateJiraTicketRequestDto {

	private String serviceDeskId;

	private String requestTypeId;

	private RequestFieldValues requestFieldValues;

}