package com.redhat.parodos.tasks.project.dto;

import java.util.UUID;

import com.redhat.parodos.project.enums.ProjectAccessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessStatusResponseDTO {

	private UUID accessRequestId;

	private ProjectAccessStatus status;

}
