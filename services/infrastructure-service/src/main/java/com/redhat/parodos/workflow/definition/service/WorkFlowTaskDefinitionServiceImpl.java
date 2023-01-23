package com.redhat.parodos.workflow.definition.service;

import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinitionEntity;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflows.definition.WorkFlowDefinition;
import com.redhat.parodos.workflows.definition.task.WorkFlowTaskDefinition;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WorkFlowTaskDefinitionServiceImpl implements WorkFlowTaskDefinitionService {
    private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

    public WorkFlowTaskDefinitionServiceImpl(WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository) {
        this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
    }

    @Override
    public WorkFlowTaskDefinitionEntity getWorkFlowTaskDefinitionById(UUID id) {
        return workFlowTaskDefinitionRepository.findById(id).get();
    }
}
