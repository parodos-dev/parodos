package com.redhat.parodos.project.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import com.redhat.parodos.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "prds_project_user_role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserRole {

	@EmbeddedId
	private Id id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "project_id", nullable = false, insertable = false, updatable = false)
	private Project project;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id", nullable = false, insertable = false, updatable = false)
	private Role role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
	private User user;

	@Embeddable
	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Id implements Serializable {

		@Column(name = "project_id", nullable = false)
		private UUID projectId;

		@Column(name = "user_id", nullable = false)
		private UUID userId;

		@Column(name = "role_id", nullable = false)
		private UUID roleId;

	}

}
