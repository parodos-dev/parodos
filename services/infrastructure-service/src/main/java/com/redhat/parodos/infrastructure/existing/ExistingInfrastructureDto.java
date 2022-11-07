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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Data;

/**
 * ExistingInfrastructure reference for passing between services
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Data
public class ExistingInfrastructureDto {
	
	 	private String projectName;

	    private String onboardedBy;
	    
	    private String infrastructureOptionDisplayName;

	    @Enumerated(EnumType.STRING)
	    private InfrastructureTaskStatus status;
	    
	    @Enumerated(EnumType.STRING)
	    private ExistingInfrastructureTypes workflowType;

	    @Column(updatable = false)
	    private OffsetDateTime createdAt;
	    
	    private List<TaskExecutionLog> taskLog = new ArrayList<>();

}
