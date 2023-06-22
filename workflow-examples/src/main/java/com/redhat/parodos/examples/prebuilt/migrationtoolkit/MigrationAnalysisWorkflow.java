package com.redhat.parodos.examples.prebuilt.migrationtoolkit;

import java.net.URI;
import java.util.List;

import com.redhat.parodos.tasks.migrationtoolkit.CreateApplicationTask;
import com.redhat.parodos.tasks.migrationtoolkit.GetAnalysisTask;
import com.redhat.parodos.tasks.migrationtoolkit.GetApplicationTask;
import com.redhat.parodos.tasks.migrationtoolkit.SubmitAnalysisTask;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.annotation.Parameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.infrastructure.Notifier;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.redhat.parodos.workflows.workflow.SequentialFlow.Builder.aNewSequentialFlow;

/**
 * A workflow to analyze applications using Migration Toolkit for Applications. This
 * workflow will:
 * <ol>
 * <li>create an application with a name, and git repo URL</li>
 * <li>create a taskgroup prior to submitting the report</li>
 * <li>submit an analysis report for it</li>
 * <li>wait till the report is back, send a notification when done</li>
 * </ol>
 * ENV Variables: For security reasons the MTA url and bearer token can be passed to all
 * the task constructors For example use MTA_URL and MTA_BEARER_TOKEN To get an email
 * supply the following variables MAILER_HOST MAILER_PORT MAILER_USER MAILER_PASS
 */
@Configuration
@Profile("dev")
@Slf4j
public class MigrationAnalysisWorkflow {

	// the url of the MTA (migration toolkit for application)
	@Value("${workflows.mta.url}")
	private String mtaUrl;

	public MigrationAnalysisWorkflow() {
	}

	@Bean
	public CreateApplicationTask createApplicationTask() {
		return new CreateApplicationTask(URI.create(mtaUrl), "");
	}

	@Bean
	public GetApplicationTask getApplicationTask() {
		return new GetApplicationTask(URI.create(mtaUrl), "");
	}

	@Bean
	public SubmitAnalysisTask submitAnalysisTask(WorkFlow fetchReportURL) {
		SubmitAnalysisTask t = new SubmitAnalysisTask(URI.create(mtaUrl), "");
		t.setWorkFlowCheckers(List.of(fetchReportURL));
		return t;
	}

	@Bean
	public GetAnalysisTask getAnalysisTask(Notifier notifier) {
		return new GetAnalysisTask(URI.create(mtaUrl), "", notifier);
	}

	@Bean(name = "AnalyzeApplication")
	@Infrastructure(parameters = {
			@Parameter(key = "repositoryURL", description = "The repository with the code to analyze",
					type = WorkParameterType.URI, optional = false),
			@Parameter(key = "applicationName", description = "The name of the application to analyze",
					type = WorkParameterType.TEXT, optional = false) })
	public WorkFlow AnalyzeApplication(CreateApplicationTask createApplicationTask, GetApplicationTask getAppTask,
			SubmitAnalysisTask submitAnalysisTask) {
		return aNewSequentialFlow().named("AnalyzeApplication").execute(createApplicationTask).then(getAppTask)
				.then(submitAnalysisTask).build();
	}

	@Bean("fetchReportURL")
	@Checker(cronExpression = "*/5 * * * * ?")
	public WorkFlow fetchReportURL(GetAnalysisTask getAnalysisTask) {
		return aNewSequentialFlow().named("fetchReportURL").execute(getAnalysisTask).build();
	}

}
