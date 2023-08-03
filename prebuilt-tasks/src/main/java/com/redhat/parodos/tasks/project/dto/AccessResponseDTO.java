package com.redhat.parodos.tasks.project.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AccessResponseDTO {

	private UUID accessRequestId;

	private List<String> approvalSentTo;

	private String escalationSentTo;

}
