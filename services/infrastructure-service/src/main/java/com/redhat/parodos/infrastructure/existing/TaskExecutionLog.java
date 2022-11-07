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
package com.redhat.parodos.infrastructure.existing;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Can be added to the ExistingInfrastructureEntity to keep track of the history of long running processes kicked
 * off my Tasks. Could be useful to update a UI on the 'status' of an InfrastructureOption being created.
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutionLog implements Serializable {
	private static final long serialVersionUID = 1L;
	private Date commentDate;
    private String comment;
}
