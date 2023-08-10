package com.redhat.parodos.project.dto.response;

import java.util.Date;
import java.util.UUID;

import com.redhat.parodos.project.enums.ProjectAccessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAccessRequestDTO {

	private UUID accessRequestId;

	private UUID projectId;

	private String role;

	private String username;

	private String firstname;

	private String lastname;

	private ProjectAccessStatus status;

	private String comment;

	private Date createDate;

}
