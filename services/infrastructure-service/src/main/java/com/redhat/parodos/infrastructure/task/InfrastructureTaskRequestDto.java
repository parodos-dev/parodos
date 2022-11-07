package com.redhat.parodos.infrastructure.task;

/**
 * Transfer object for the UI to use to request the execution of an InfrastructureTask Workflow to start creating an InfrastructureOption
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class InfrastructureTaskRequestDto {
    private String workFlowName;
    private Map<String,String> requestDetails;
}
