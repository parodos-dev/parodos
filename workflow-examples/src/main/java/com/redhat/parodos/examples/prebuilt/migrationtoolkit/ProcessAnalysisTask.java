package com.redhat.parodos.examples.prebuilt.migrationtoolkit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.Predicate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflow.option.WorkFlowOptions;
import com.redhat.parodos.workflow.task.assessment.BaseAssessmentTask;
import com.redhat.parodos.workflow.task.infrastructure.Notifier;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

public class ProcessAnalysisTask extends BaseAssessmentTask {

	private final WorkFlowOption passOption;

	private final WorkFlowOption defaultOption;

	private final Predicate<MTAAnalysisReport.AnalysisIncidents> incidentsPredicate;

	private Notifier notificationSender;

	/**
	 * @param passOption option for a successful report analysis
	 * @param defaultOption default option to return
	 * @param passCriteria the predicate to test against the report incidents from the
	 * analysis task
	 */
	protected ProcessAnalysisTask(WorkFlowOption passOption, WorkFlowOption defaultOption,
			Predicate<MTAAnalysisReport.AnalysisIncidents> passCriteria, Notifier notifier) {
		super(List.of(passOption, defaultOption));
		this.passOption = passOption;
		this.defaultOption = defaultOption;
		this.incidentsPredicate = passCriteria;
		this.notificationSender = notifier;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		String reportURL;
		try {
			reportURL = getRequiredParameterValue("reportURL") + "/";
		}
		catch (MissingParameterException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}

		// this is the "migration issues" section from report from url.It assumed to be in
		// HTML format. In version 6.1 of MTA it can be
		// downloaded as csv and would be simpler to process, hopefully.
		URI uri = URI.create(reportURL);

		try {
			String issueSummaries;
			if (uri.getScheme().startsWith("file")) {
				issueSummaries = Files.readString(Paths.get(uri));
			}
			else {
				issueSummaries = download(uri.resolve("reports/data/issue_summaries.js"));
			}
			MTAAnalysisReport.AnalysisIncidents incidents = MTAAnalysisReport.extractIncidents(issueSummaries);

			if (incidentsPredicate.test(incidents)) {
				notificationSender.send(passOption.getDisplayName(),
						"The MTA report summary passes the specified criteria in the workflow. \\ %s \\ ##Incidents summary %s"
								.formatted(passOption.getDescription(), incidents));
				WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
						WorkContextDelegate.Resource.WORKFLOW_OPTIONS,
						new WorkFlowOptions.Builder().addNewOption(passOption).build());
			}
			else {
				WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
						WorkContextDelegate.Resource.WORKFLOW_OPTIONS,
						new WorkFlowOptions.Builder().addNewOption(defaultOption).build());

			}
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		catch (Exception e) {

			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}
	}

	private String download(URI uri) throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext nonValidatingSSLContext = SSLContext.getInstance("SSL");
		nonValidatingSSLContext.init(null, new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		} }, SecureRandom.getInstanceStrong());
		var c = HttpClient.newBuilder().sslContext(nonValidatingSSLContext).followRedirects(HttpClient.Redirect.ALWAYS)
				.build();
		try {
			HttpResponse<String> get = c.send(HttpRequest.newBuilder().uri(uri).build(),
					HttpResponse.BodyHandlers.ofString());
			if (get.statusCode() == HttpURLConnection.HTTP_OK) {
				return get.body();
			}
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

}
