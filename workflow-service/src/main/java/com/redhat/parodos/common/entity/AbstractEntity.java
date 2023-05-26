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
package com.redhat.parodos.common.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for persisting and Entity
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
@MappedSuperclass
public class AbstractEntity {

	@Id
	@GeneratedValue
	@Column(columnDefinition = "uuid")
	private UUID id;

	// TODO: disabled temporarily as optimistic locking issue
	// @Version
	@JsonIgnore
	private Long objectVersion;

	protected AbstractEntity() {
		this.id = null;
	}

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Long getObjectVersion() {
		return objectVersion;
	}

}
