package com.redhat.parodos.tasks.github;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.List;

@Slf4j
public class GithubCreateRepoTask extends BaseWorkFlowTask {

    public static final String APPLICATION_NAME = "applicationName";
    public static final String ORGANIZATION = "organization";
    public static final String CREDENTIALS = "credentials";
    public static final String BRANCH = "Branch";
    public static final String MAIN = "main";

    private final GitHub gitHub;

    public GithubCreateRepoTask(GitHub gitHub) {
        this.gitHub = gitHub;
    }

    @Override
    public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
        return List.of(
                WorkParameter.builder()
                        .key(ORGANIZATION)
                        .type(WorkParameterType.TEXT)
                        .description("Git organization")
                        .optional(false)
                        .build(),
                WorkParameter.builder()
                        .key(APPLICATION_NAME)
                        .type(WorkParameterType.TEXT)
                        .description("the name of application to be deployed")
                        .optional(false)
                        .build(),
                WorkParameter.builder()
                        .key(BRANCH)
                        .type(WorkParameterType.TEXT)
                        .optional(false)
                        .description("Git default branch")
                        .build());
    }

    @Override
    public WorkReport execute(WorkContext workContext) {
        String gitBranch;
        String applicationName = null;
        String organizationName;

        try {
            applicationName = this.getRequiredParameterValue(APPLICATION_NAME);
            organizationName = this.getRequiredParameterValue(ORGANIZATION);
            gitBranch = this.getOptionalParameterValue(BRANCH, MAIN);

            gitHub.getOrganization(organizationName)
                    .createRepository(applicationName + "-deploy")
                    .private_(true)
                    .wiki(false)
                    .projects(false)
                    .description("this is deploy repo for " + applicationName)
                    .defaultBranch(gitBranch)
                    .create();
        } catch (MissingParameterException e) {
            log.error("Failed to resolve required parameter: {}", e.getMessage());
            return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
        } catch (IOException e) {
            log.error("Cannot create repository: {} {}", applicationName, e.getMessage());
            return new DefaultWorkReport(WorkStatus.REJECTED, workContext,
                    new Exception("cannot create repository, error: " + e.getMessage()));
        }
        return new DefaultWorkReport(WorkStatus.COMPLETED, workContext, null);
    }
}
