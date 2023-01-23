package com.redhat.parodos.workflow.definition.entity;

import com.redhat.parodos.workflow.execution.entity.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data
//@EqualsAndHashCode(callSuper = false)
@Entity(name = "workflow_definition")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowDefinitionEntity extends AbstractEntity {
    private String name;

    private String description;

    private String type;

    private String author;

    @Column(updatable = false)
    private Date createDate;

    private Date modifyDate;

    @OneToMany(mappedBy = "workFlowDefinitionEntity", fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private List<WorkFlowTaskDefinitionEntity> tasks = Collections.synchronizedList(new ArrayList<>());

    @OneToMany(mappedBy = "checkWorkFlow", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private Set<WorkFlowCheckerDefinitionEntity> workFlowCheckerDefinitionEntities = new HashSet<>();

    @OneToOne(mappedBy = "nextWorkFlow", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private WorkFlowCheckerDefinitionEntity workFlowCheckerDefinitionEntity;
}


