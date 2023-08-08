package com.redhat.parodos.project.dto.response;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProjectMemberResponseDTO {

	private String username;

	private String firstName;

	private String lastName;

	private Set<String> roles;

}
