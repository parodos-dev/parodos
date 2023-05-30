package com.redhat.parodos.workflow.execution.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.redhat.parodos.common.exceptions.IDType;
import com.redhat.parodos.common.exceptions.ResourceNotFoundException;
import com.redhat.parodos.common.exceptions.ResourceType;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.exceptions.WorkflowPersistenceFailedException;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecutionLog;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.task.log.dto.WorkFlowTaskLog;
import com.redhat.parodos.workflow.task.log.service.WorkFlowLogService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkFlowLogServiceImpl implements WorkFlowLogService {

	private final WorkFlowRepository workFlowRepository;

	private final WorkFlowTaskRepository workFlowTaskRepository;

	private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	public WorkFlowLogServiceImpl(WorkFlowTaskRepository workFlowTaskRepository,
			WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository, WorkFlowRepository workFlowRepository) {
		this.workFlowTaskRepository = workFlowTaskRepository;
		this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
		this.workFlowRepository = workFlowRepository;
	}

	@Override
	public String getLog(UUID mainWorkflowExecutionId, String taskName) {
		if (taskName == null) {
			return workFlowRepository.findByMainWorkFlowExecutionId(mainWorkflowExecutionId).stream()
					.map(workFlowExecution -> workFlowTaskRepository
							.findByWorkFlowExecutionId(workFlowExecution.getId()))
					.flatMap(List::stream).distinct()
					.filter(workFlowTaskExecution -> workFlowTaskExecution.getWorkFlowTaskExecutionLog() != null)
					.map(workFlowTaskExecution -> workFlowTaskExecution.getWorkFlowTaskExecutionLog().getLog())
					.collect(Collectors.joining(";"));
		}
		else {
			return getTaskLog(mainWorkflowExecutionId, taskName);
		}
	}

	@Override
	public void writeLog(UUID mainWorkflowExecutionId, String taskName, WorkFlowTaskLog workFlowTaskLog) {
		WorkFlowTaskExecution workFlowTaskExecution = getWorkFlowTaskExecution(mainWorkflowExecutionId, taskName);
		if (workFlowTaskExecution.getWorkFlowTaskExecutionLog() == null) {
			workFlowTaskExecution.setWorkFlowTaskExecutionLog(WorkFlowTaskExecutionLog.builder()
					.workFlowTaskExecution(workFlowTaskExecution).log(workFlowTaskLog.toString()).build());
		}
		else {
			workFlowTaskExecution.getWorkFlowTaskExecutionLog().addLog(workFlowTaskLog.toString());
		}
		try {
			workFlowTaskRepository.save(workFlowTaskExecution);
		}
		catch (DataAccessException | IllegalArgumentException e) {
			log.error("failed updating task execution for: {} in execution: {}. error Message: {}",
					workFlowTaskExecution.getWorkFlowTaskDefinitionId(), workFlowTaskExecution.getId(), e.getMessage());
			throw new WorkflowPersistenceFailedException(e.getMessage());
		}
	}

	private WorkFlowTaskExecution getWorkFlowTaskExecution(UUID mainWorkflowExecutionId, String taskName) {
		WorkFlowTaskDefinition workFlowTaskDefinition = Optional
				.ofNullable(workFlowTaskDefinitionRepository.findFirstByName(taskName))
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.WORKFLOW_TASK, IDType.NAME, taskName));
		WorkFlowExecution workFlowExecution = Optional
				.ofNullable(workFlowRepository.findFirstByMainWorkFlowExecutionIdAndWorkFlowDefinitionId(
						mainWorkflowExecutionId, workFlowTaskDefinition.getWorkFlowDefinition().getId()))
				.or(() -> workFlowRepository.findById(mainWorkflowExecutionId)).orElseThrow(
						() -> new ResourceNotFoundException(ResourceType.WORKFLOW_EXECUTION, mainWorkflowExecutionId));
		return Optional
				.ofNullable(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(
						workFlowExecution.getId(), workFlowTaskDefinition.getId()))
				.filter(workFlowTaskExecutions -> !workFlowTaskExecutions.isEmpty())
				.map(workFlowTaskExecutions -> workFlowTaskExecutions.get(0))
				.orElseThrow(() -> new ResourceNotFoundException(ResourceType.WORKFLOW_TASK_EXECUTION, IDType.NAME,
						taskName));
	}

	private String getTaskLog(UUID mainWorkflowExecutionId, String taskName) {
		WorkFlowTaskExecution workFlowTaskExecution = getWorkFlowTaskExecution(mainWorkflowExecutionId, taskName);
		return Optional.ofNullable(workFlowTaskExecution.getWorkFlowTaskExecutionLog())
				.map(WorkFlowTaskExecutionLog::getLog).orElse("");
	}

}
