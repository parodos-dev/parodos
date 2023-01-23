package com.redhat.parodos.workflow.definition.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkFlowCheckerDefinitionPK implements Serializable {
    private static final long serialVersionUID = -4267447428417892672L;

    private UUID workFlowCheckerId;


    private UUID taskId;
}
