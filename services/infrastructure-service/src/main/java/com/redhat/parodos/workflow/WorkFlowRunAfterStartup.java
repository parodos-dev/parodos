package com.redhat.parodos.workflow;

import com.redhat.parodos.workflow.execution.WorkFlowStatus;
import com.redhat.parodos.workflow.execution.transaction.WorkFlowTransactionRepository;
import com.redhat.parodos.workflows.WorkFlowConstants;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkFlowRunAfterStartup {

    private final WorkFlowTransactionRepository workFlowTransactionRepository;

    private final WorkFlowEngine workFlowEngine;

    private final WorkFlowDelegate workFlowDelegate;

    public WorkFlowRunAfterStartup(WorkFlowTransactionRepository workFlowTransactionRepository, WorkFlowEngine workFlowEngine, WorkFlowDelegate workFlowDelegate) {
        this.workFlowTransactionRepository = workFlowTransactionRepository;
        this.workFlowEngine = workFlowEngine;
        this.workFlowDelegate = workFlowDelegate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void workFlowRunAfterStartup() {
        log.info("retrieving workflows to be continued ...");
        workFlowTransactionRepository.findAll()
                .stream()
                .filter(entity -> WorkFlowStatus.IN_PROGRESS.name().equals(entity.getStatus()))
                .forEach(workFlowTransactionEntity -> {
                // this logic is the same for all workflows
                    WorkContext context = new WorkContext();
                    context.put(WorkFlowConstants.WORKFLOW_TYPE, workFlowTransactionEntity.getWorkFlowType());
                    WorkFlow workFlow = workFlowDelegate.getWorkFlowById(workFlowTransactionEntity.getWorkFlowId());
                    workFlowEngine.executeWorkFlows(context, workFlow);
                });
        log.info("continued workflows are all executed ...");
    }
}
