package com.redhat.parodos.workflow.execution.entity;

import com.redhat.parodos.common.AbstractEntity;
import com.redhat.parodos.workflow.execution.entity.converter.WorkContextConverter;
import com.redhat.parodos.workflows.work.WorkContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity(name = "workflow_execution_context")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkFlowExecutionContext extends AbstractEntity {

	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "workflow_execution_id")
	private WorkFlowExecution masterWorkFlowExecution;

	@Convert(converter = WorkContextConverter.class)
	private WorkContext workContext;

}
