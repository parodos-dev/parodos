package com.redhat.parodos.project.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import com.redhat.parodos.common.entity.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "prds_role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AbstractEntity {

	@Column(unique = true)
	private String name;

	private String description;

	@Column(updatable = false)
	private Date createDate;

	private Date modifyDate;

}
