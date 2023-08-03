package com.redhat.parodos.tasks.project.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessResponseDTO {

	private UUID accessRequestId;

	private List<String> approvalSentTo;

	private String escalationSentTo;

}
