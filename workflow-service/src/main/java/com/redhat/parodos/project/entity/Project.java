/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.project.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.redhat.parodos.common.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Project entity
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Entity(name = "prds_project")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project extends AbstractEntity {

	@Column(unique = true)
	private String name;

	private String description;

	@Column(updatable = false)
	private Date createDate;

	private Date modifyDate;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
	private Set<ProjectUserRole> projectUserRoles = new HashSet<>();

}
