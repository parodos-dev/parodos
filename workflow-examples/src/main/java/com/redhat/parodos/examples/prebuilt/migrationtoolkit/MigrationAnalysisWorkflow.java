package com.redhat.parodos.examples.prebuilt.migrationtoolkit;

import com.redhat.parodos.examples.escalation.checker.SimpleTaskOneChecker;
import com.redhat.parodos.examples.escalation.task.SimpleTaskOne;
import com.redhat.parodos.examples.escalation.task.SimpleTaskOneEscalator;
import com.redhat.parodos.examples.escalation.task.SimpleTaskTwo;
import com.redhat.parodos.tasks.migrationtoolkit.CreateApplicationTask;
import com.redhat.parodos.tasks.migrationtoolkit.GetAnalysisTask;
import com.redhat.parodos.tasks.migrationtoolkit.GetApplicationTask;
import com.redhat.parodos.tasks.migrationtoolkit.SubmitAnalysisTask;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Escalation;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import org.jose4j.http.Get;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Date;

@Configuration
public class MigrationAnalysisWorkflow {

    @Bean
    @Infrastructure
    public WorkFlow AnalyzeApplication (
            CreateApplicationTask createAppTask,
            GetApplicationTask getAppTask,
            SubmitAnalysisTask submitAnalysisTask,
            GetAnalysisTask getAnalysisTask) {
            return SequentialFlow.Builder
                    .aNewSequentialFlow()
                    .named("migration_toolkit_analyze_application")
                    .execute(createAppTask)
                    .then(getAppTask)
                    .then(submitAnalysisTask)
                    .build();
            // @formatter:on
    }

    // Task executed by workflowStartingCheckingAndEscalation
    @Bean
    public GetAnalysisTask getAnalysisTask (
            @Qualifier("getAnalysisTaskChecker") WorkFlow simpleTaskOneCheckerWorkflow) {
        GetAnalysisTask taskOne = new SimpleTaskOne();
        taskOne.setWorkFlowCheckers(Arrays.asList(simpleTaskOneCheckerWorkflow));
        return taskOne;
    }


    // ********** Start Checker + Escalation **************
    @Bean
    @Checker(cronExpression = "*/5 * * * * ?")
    public WorkFlow simpleTaskOneCheckerWorkflow(
            @Qualifier("simpleTaskOneCheckerTask") SimpleTaskOneChecker simpleTaskOneCheckerTask) {
        // @formatter:off
            return SequentialFlow.Builder
                    .aNewSequentialFlow()
                    .named("simpleTaskOneCheckerWorkflow")
                    .execute(simpleTaskOneCheckerTask)
                    .build();
            // @formatter:on
    }

    @Bean
    public SimpleTaskOneChecker simpleTaskOneCheckerTask(
            @Qualifier("simpleTaskOneEscalatorWorkflow") WorkFlow simpleTaskOneEscalatorWorkflow) {
        return new SimpleTaskOneChecker(simpleTaskOneEscalatorWorkflow, new Date().getTime() / 1000 + 30);
    }

    @Bean
    @Escalation
    public WorkFlow simpleTaskOneEscalatorWorkflow(
            @Qualifier("simpleTaskOneEscalator") SimpleTaskOneEscalator simpleTaskOneEscalator) {
        // @formatter:off
            return SequentialFlow.Builder
                    .aNewSequentialFlow()
                    .named("simpleTaskOneEscalatorWorkflow")
                    .execute(simpleTaskOneEscalator)
                    .build();
            // @formatter:on

    }

    @Bean
    public SimpleTaskOneEscalator simpleTaskOneEscalator() {
        return new SimpleTaskOneEscalator();
    }

    // ********** End Checker + Escalation **************

    // ********** Start Second Workflow **************
    // This is the workflow that runs if simpleTaskOneCheckerWorkflow (Checker for task
    // SimpleTaskOne) is successful
    @Bean
    @Infrastructure
    public WorkFlow workflowContinuesAfterCheckingEscalation(SimpleTaskTwo simpleTaskTwo) {
        // @formatter:off
            return SequentialFlow.Builder
                    .aNewSequentialFlow()
                    .named("workflowContinuesAfterCheckingEscalation")
                    .execute(simpleTaskTwo)
                    .build();
            // @formatter:on
    }

    @Bean
    public SimpleTaskTwo simpleTaskTwo() {
        return new SimpleTaskTwo();
    }
    // ********** End Second Workflow **************

}
}
