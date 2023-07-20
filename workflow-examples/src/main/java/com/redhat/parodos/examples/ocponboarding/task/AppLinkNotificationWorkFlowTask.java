/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.examples.ocponboarding.task;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

/**
 * An example of a task that send an app link email notification
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class AppLinkNotificationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private final static String TEMPLATE_DEFAULT_ENCODING = "UTF-8";

	private final static String TEMPLATE_BASE_PACKAGE_PATH = "templates";

	private final static String TEMPLATE_NAME = "appLinkNotification.ftlh";

	private static final String JIRA_TICKET_URL_WORKFLOW_TASK_PARAMETER_NAME = "ISSUE_LINK";

	private static final String OCP_APP_LINK_WORKFLOW_TASK_PARAMETER_NAME = "APP_LINK";

	private static final String NOTIFICATION_SUBJECT = "App link notification";

	private final Notifier notifier;

	public AppLinkNotificationWorkFlowTask(Notifier notifier) {
		super();
		this.notifier = notifier;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start appLinkNotificationWorkFlowTask...");
		Map<String, Object> messageData = new HashMap<>();
		String jiraTicketUrl, appLink, message;
		try {
			jiraTicketUrl = getRequiredParameterValue(JIRA_TICKET_URL_WORKFLOW_TASK_PARAMETER_NAME);
			messageData.put("jiraTicketUrl", jiraTicketUrl);
			appLink = getRequiredParameterValue(OCP_APP_LINK_WORKFLOW_TASK_PARAMETER_NAME);
			messageData.put("appLink", appLink);
			message = getMessage(messageData);
		}
		catch (MissingParameterException | IOException | TemplateException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
		notifier.send(NOTIFICATION_SUBJECT, message);
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private String getMessage(Map<String, Object> templateData) throws IOException, TemplateException {
		String message = "";
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
		cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), TEMPLATE_BASE_PACKAGE_PATH);
		cfg.setDefaultEncoding(TEMPLATE_DEFAULT_ENCODING);
		Template template = cfg.getTemplate(TEMPLATE_NAME);
		try (StringWriter out = new StringWriter()) {
			template.process(templateData, out);
			message = out.getBuffer().toString();
			out.flush();
		}
		return message;
	}

}
