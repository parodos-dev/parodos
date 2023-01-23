package com.redhat.parodos.workflow.definition.service;

import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinitionEntity;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinitionEntity;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflows.definition.WorkFlowDefinition;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WorkFlowDefinitionServiceImpl implements WorkFlowDefinitionService {
    private final WorkFlowDefinitionRepository workFlowDefinitionRepository;
    private final WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

    public WorkFlowDefinitionServiceImpl(WorkFlowDefinitionRepository workFlowDefinitionRepository,
                                         WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository) {
        this.workFlowDefinitionRepository = workFlowDefinitionRepository;
        this.workFlowTaskDefinitionRepository = workFlowTaskDefinitionRepository;
    }

    @Override
    public List<WorkFlowDefinitionEntity> getWorkFlowDefinitions() {
        return workFlowDefinitionRepository.findAll();
    }

    @Override
    public List<WorkFlowTaskDefinitionEntity> getWorkFlowTaskDefinitionById(UUID workFlowDefinitionId) {
        return workFlowTaskDefinitionRepository.findByWorkFlowDefinitionEntity(workFlowDefinitionRepository.findById(workFlowDefinitionId).get());
    }

    @Override
    public WorkFlowDefinitionEntity getWorkFlowDefinitionByName(String workFlowDefinitionName) {
        return workFlowDefinitionRepository.findByName(workFlowDefinitionName).get(0);
    }
}
