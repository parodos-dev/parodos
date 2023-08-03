package com.redhat.parodos.tasks.project.dto;

import java.util.UUID;

import com.redhat.parodos.project.enums.ProjectAccessStatus;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AccessStatusResponseDTO {

	private UUID accessRequestId;

	private ProjectAccessStatus status;

}
