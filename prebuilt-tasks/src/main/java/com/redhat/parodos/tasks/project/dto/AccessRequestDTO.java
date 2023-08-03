package com.redhat.parodos.tasks.project.dto;

import com.redhat.parodos.project.enums.Role;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AccessRequestDTO {

	private String username;

	private Role role;

}
