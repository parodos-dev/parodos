package com.redhat.parodos.workflow.definition.entity;

import com.redhat.parodos.common.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

	private String workDefinitionType;

	@ManyToOne(optional = false)
	@JoinColumn(name = "workflow_definition_id")
	private WorkFlowDefinition workFlowDefinition;

	@Column(updatable = false)
	private Date createDate;

}