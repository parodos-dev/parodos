package com.redhat.parodos.workflow.execution.service;

import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionEntity;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecutionEntity;
import com.redhat.parodos.workflow.execution.repository.WorkFlowExecutionRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskExecutionRepository;
import com.redhat.parodos.workflows.common.enums.WorkFlowStatus;
import com.redhat.parodos.workflows.common.enums.WorkFlowTaskStatus;
import com.redhat.parodos.workflows.definition.WorkFlowDefinition;
import com.redhat.parodos.workflows.engine.WorkFlowEngineBuilder;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class WorkFlowExecutionServiceImpl implements WorkFlowExecutionService {
    private final WorkFlowDelegate workFlowDelegate;
    private final WorkFlowExecutionRepository workFlowExecutionRepository;
    private final WorkFlowTaskExecutionRepository workFlowTaskExecutionRepository;

    public WorkFlowExecutionServiceImpl(WorkFlowDelegate workFlowDelegate,
                                        WorkFlowDefinitionServiceImpl workFlowDefinitionService, WorkFlowExecutionRepository workFlowExecutionRepository,
                                        WorkFlowTaskExecutionRepository workFlowTaskExecutionRepository) {
        this.workFlowDelegate = workFlowDelegate;
        this.workFlowExecutionRepository = workFlowExecutionRepository;
        this.workFlowTaskExecutionRepository = workFlowTaskExecutionRepository;
    }

    @Override
    public WorkReport execute(WorkFlowDefinition workFlowDefinition, WorkFlow workFlow, Map<String, Map<String, String>> workFlowTaskArguments) {
        log.info("execute WorkFlowDefinition: {}, workFlow: {}", workFlowDefinition, workFlow);
        if (null != workFlow) {
            WorkContext workContext = workFlowDelegate.getWorkFlowContext(workFlowDefinition, workFlowTaskArguments);
            return WorkFlowEngineBuilder.aNewWorkFlowEngine().build().run(workFlow, workContext);
        } else {
            return new DefaultWorkReport(WorkStatus.FAILED, new WorkContext());
        }
    }

    @Override
    public WorkFlowExecutionEntity getWorkFlowById(UUID workFlowExecutionId) {
        return this.workFlowExecutionRepository.findById(workFlowExecutionId).get();
    }

    @Override
    public WorkFlowExecutionEntity saveWorkFlow(String username, String reason, UUID workFlowDefinitionId, WorkFlowStatus workFlowStatus) {
        return workFlowExecutionRepository.save(WorkFlowExecutionEntity.builder()
                .workFlowDefinitionId(workFlowDefinitionId)
                .executedBy(username)
                .executedFor(reason)
                .status(workFlowStatus)
                .startDate(new Date())
                .build());
    }

    @Override
    public WorkFlowExecutionEntity updateWorkFlow(WorkFlowExecutionEntity workFlowExecutionEntity) {
        return workFlowExecutionRepository.save(workFlowExecutionEntity);
    }

    @Override
    public WorkFlowTaskExecutionEntity getWorkFlowTask(UUID workFlowExecutionId, UUID workFlowTaskDefinitionId) {
        List<WorkFlowTaskExecutionEntity> workFlowTaskExecutionEntityList = workFlowTaskExecutionRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(workFlowExecutionId, workFlowTaskDefinitionId);
        return (workFlowTaskExecutionEntityList == null || workFlowTaskExecutionEntityList.isEmpty()) ? null : workFlowTaskExecutionEntityList.get(0);
    }

    @Override
    public WorkFlowTaskExecutionEntity saveWorkFlowTask(String arguments, UUID workFlowTaskDefinitionId, UUID workFlowExecutionId, WorkFlowTaskStatus workFlowTaskStatus) {
        return workFlowTaskExecutionRepository.save(WorkFlowTaskExecutionEntity.builder()
                .workFlowExecutionId(workFlowExecutionId)
                .workFlowTaskDefinitionId(workFlowTaskDefinitionId)
                .arguments(arguments)
                .status(workFlowTaskStatus)
                .startDate(new Date())
                .build());
    }

    @Override
    public WorkFlowTaskExecutionEntity updateWorkFlowTask(WorkFlowTaskExecutionEntity workFlowTaskExecutionEntity) {
        return workFlowTaskExecutionRepository.save(workFlowTaskExecutionEntity);
    }
}
