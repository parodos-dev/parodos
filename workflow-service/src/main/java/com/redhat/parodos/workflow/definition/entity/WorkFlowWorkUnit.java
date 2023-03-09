package com.redhat.parodos.workflow.definition.entity;

import com.redhat.parodos.common.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;
import java.util.UUID;

@Entity(name = "workflow_work_unit")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowWorkUnit extends AbstractEntity {

	private UUID workDefinitionId;

	private String workDefinitionType;

	@Column(name = "workflow_definition_id")
	private UUID workFlowDefinitionId;

	@Column(updatable = false)
	private Date createDate;

}