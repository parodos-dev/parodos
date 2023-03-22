package com.redhat.parodos.workflow.definition.entity;

import com.redhat.parodos.common.AbstractEntity;
import com.redhat.parodos.workflow.enums.WorkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity(name = "workflow_work_definition")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowWorkDefinition extends AbstractEntity {

	private UUID workDefinitionId;

	@Enumerated
	private WorkType workDefinitionType;

	@ManyToOne(optional = false)
	@JoinColumn(name = "workflow_definition_id")
	private WorkFlowDefinition workFlowDefinition;

	@Column(updatable = false)
	private Date createDate;

}