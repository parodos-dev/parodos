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

import static java.util.Objects.isNull;

import com.redhat.parodos.examples.ocponboarding.task.dto.email.MessageRequestDTO;
import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

/**
 * An example of a task that send an app link email notification
 *
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class AppLinkEmailNotificationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private final static String TEMPLATE_DEFAULT_ENCODING = "UTF-8";

	private final static String TEMPLATE_BASE_PACKAGE_PATH = "templates";

	private final static String TEMPLATE_NAME = "appLinkEmailNotification.ftlh";

	private static final String APP_LINK_PARAMETER_NAME = "APP_LINK";

	private final String mailServiceUrl;

	private final String mailServiceSiteName;

	public AppLinkEmailNotificationWorkFlowTask(String mailServiceUrl, String mailServiceSiteName) {
		super();
		this.mailServiceUrl = mailServiceUrl;
		this.mailServiceSiteName = mailServiceSiteName;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start appLinkEmailNotificationWorkFlowTask...");

		// requester name to extract securityContext or from workContext
		String requesterName = "Test Test";

		// requester email to extract securityContext or from workContext
		String requesterEmail = "ttest@test.com";

		// fill in message data to extract from workContext
		Map<String, Object> messageData = new HashMap<>();
		String appLink;
		try {
			appLink = getRequiredParameterValue(workContext, APP_LINK_PARAMETER_NAME);
			messageData.put("appLink", appLink);
			log.info("App link is: {}", appLink);
		}
		catch (MissingParameterException e) {
			log.error("AppLinkEmailNotificationWorkFlowTask failed! Message: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}

		ResponseEntity<String> responseEntity = null;
		try {
			// message template
			String message = getMessage(TEMPLATE_NAME, messageData);
			if (message.isEmpty()) {
				log.info("AppLinkEmailNotificationWorkFlowTask failed due to empty message template!");
				return new DefaultWorkReport(WorkStatus.FAILED, workContext);
			}
			// message request payload
			MessageRequestDTO messageRequestDTO = MessageRequestDTO.builder().name(requesterName).email(requesterEmail)
					.siteName(mailServiceSiteName).message(message).build();
			HttpEntity<MessageRequestDTO> requestEntity = new HttpEntity<>(messageRequestDTO);
			responseEntity = RestUtils.executePost(mailServiceUrl, requestEntity);
		}
		catch (Exception e) {
			log.error("Error occurred when preparing or submitting the message: {}", e.getMessage());
		}

		if (!isNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful()
				&& !isNull(responseEntity.getBody()) && responseEntity.getBody().contains("Mail Sent")) {
			log.info("AppLinkEmailNotificationWorkFlowTask completed!");
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}

		log.info("AppLinkEmailNotificationWorkFlowTask failed!");
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.EXCEPTION, WorkFlowTaskOutput.OTHER);
	}

	private String getMessage(String templateName, Map<String, Object> templateData)
			throws IOException, TemplateException {
		String message = "";
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
		cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), TEMPLATE_BASE_PACKAGE_PATH);
		cfg.setDefaultEncoding(TEMPLATE_DEFAULT_ENCODING);
		Template template = cfg.getTemplate(templateName);
		try (StringWriter out = new StringWriter()) {
			template.process(templateData, out);
			message = out.getBuffer().toString();
			out.flush();
		}
		return message;
	}

}
