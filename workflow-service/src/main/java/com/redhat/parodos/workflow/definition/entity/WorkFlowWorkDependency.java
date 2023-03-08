package com.redhat.parodos.workflow.definition.entity;

import com.redhat.parodos.common.AbstractEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;
import java.util.UUID;

@Entity(name = "workflow_work_dependency")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowWorkDependency extends AbstractEntity {

	private UUID workDefinitionId; // WorkFlow Definition Id or WorkFlowTask Definition Id

	private String workDefinitionType; // WorkFlow or WorkFlow Task

	@Column(name = "workflow_definition_id")
	private UUID workFlowDefinitionId;

	@Column(updatable = false)
	private Date createDate;

}