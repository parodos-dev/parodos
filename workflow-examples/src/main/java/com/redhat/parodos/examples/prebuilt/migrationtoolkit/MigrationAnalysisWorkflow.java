package com.redhat.parodos.examples.prebuilt.migrationtoolkit;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.redhat.parodos.email.Message;
import com.redhat.parodos.tasks.migrationtoolkit.*;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static com.redhat.parodos.workflows.workflow.SequentialFlow.Builder.aNewSequentialFlow;

/**
 * A workflow to analyze applications using Migration Toolkit for Applications
 *
 * This workflow will: - create an application with a name, and git repo URL - submit an
 * analysis report for it - wait till the report is back - send an email with the report
 * url
 *
 * ENV Variables: For security reasons the MTA url and bearer token can be passed to all
 * the task constructors For example use MTA_URL and MTA_BEARER_TOKEN To get an email
 * supply the following variables MAILER_HOST MAILER_PORT MAILER_USER MAILER_PASS
 */
@Profile("examples")
@Configuration
public class MigrationAnalysisWorkflow {

	// the url of the MTA (migration toolkit for application)
	private String mtaUrl;

	// in the end you can send a message to an email server with the fields below. The
	// email code
	// can be removed from the workflow configuration if not needed or be replaced with
	// any messaging system.
	// See #messageConsumer
	// If you do remove or change the messaging consumer then make sure to update the
	// constructor checks.
	private final String mailerHost;

	private final String mailerPort;

	private final String mailerUser;

	private final String mailerPass;

	public MigrationAnalysisWorkflow() {
		mtaUrl = Objects.requireNonNull(System.getenv("MTA_URL"));
		mailerHost = Objects.requireNonNull(System.getenv("MAILER_HOST"));
		mailerPort = Objects.requireNonNull(System.getenv("MAILER_PORT"));
		mailerUser = Objects.requireNonNull(System.getenv("MAILER_USER"));
		mailerPass = Objects.requireNonNull(System.getenv("MAILER_PASS"));
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
	public GetAnalysisTask getAnalysisTask() {
		return new GetAnalysisTask(URI.create(mtaUrl), "", messageConsumer());
	}

	@Bean(name = "AnalyzeApplication")
	@Infrastructure
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

	private Consumer<Message> messageConsumer() {
		var sender = new JavaMailSenderImpl();
		sender.setHost(mailerHost);
		sender.setPort(Integer.valueOf(mailerPort));
		sender.setUsername(mailerUser);
		sender.setPassword(mailerPass);

		Consumer<Message> messageConsumer = m -> {
			var message = new SimpleMailMessage();
			message.setTo(m.to());
			message.setFrom(m.from());
			message.setSubject(m.subject());
			message.setText(m.data());
			try {
				sender.send(message);
			}
			catch (Exception e) {
				// TODO log handling
			}
		};
		return messageConsumer;
	}

}
