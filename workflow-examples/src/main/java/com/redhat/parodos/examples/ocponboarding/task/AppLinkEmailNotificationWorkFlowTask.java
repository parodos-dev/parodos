package com.redhat.parodos.examples.ocponboarding.task;

import com.redhat.parodos.examples.ocponboarding.task.dto.email.MessageRequestDTO;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

@Component
@Slf4j
public class AppLinkEmailNotificationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private final static String MAIL_SERVER_URL = "https://mail-handler-svc-ihtetft2da-uc.a.run.app/submit";

	private final static String MAIL_SITE_NAME = "parodos-ocp";

	private final static String TEMPLATE_DEFAULT_ENCODING = "UTF-8";

	private final static String TEMPLATE_BASE_PACKAGE_PATH = "templates";

	private final static String TEMPLATE_NAME = "appLinkEmailNotification.ftlh";

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start AppLinkEmailNotificationWorkFlowTask...");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = null;

		// requester name to extract securityContext or from workContext
		String requesterName = "John Doe";

		// requester email to extract securityContext or from workContext
		String requesterEmail = "jdoe@mail.com";

		// fill in message data to extract from workContext
		Map<String, Object> messageData = new HashMap<>();
		messageData.put("appLink", "https://ocp.xyz");

		// message request payload
		try {
			String message = getMessage(TEMPLATE_NAME, messageData);
			log.info("getMessage(): {}", message);
			if (!message.isEmpty()) {
				MessageRequestDTO messageRequestDTO = getMessageRequestDTO(requesterName, requesterEmail,
						MAIL_SITE_NAME, message);
				HttpEntity<MessageRequestDTO> request = new HttpEntity<>(messageRequestDTO);
				responseEntity = restTemplate.exchange(MAIL_SERVER_URL, HttpMethod.POST, request, String.class);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
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
	public List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		return Collections.emptyList();
	}

	@Override
	public List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.OTHER, WorkFlowTaskOutput.EXCEPTION);
	}

	private MessageRequestDTO getMessageRequestDTO(String requesterName, String requesterEmail, String siteName,
			String message) {
		MessageRequestDTO messageRequestDTO = new MessageRequestDTO();
		messageRequestDTO.setName(requesterName);
		messageRequestDTO.setEmail(requesterEmail);
		messageRequestDTO.setSiteName(siteName);
		messageRequestDTO.setMessage(message);
		return messageRequestDTO;
	}

	private String getMessage(String templateName, Map<String, Object> templateData)
			throws IOException, TemplateException {
		String message = "";
		Configuration cfg = new Configuration(freemarker.template.Configuration.VERSION_2_3_30);
		cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), TEMPLATE_BASE_PACKAGE_PATH);
		cfg.setDefaultEncoding(TEMPLATE_DEFAULT_ENCODING);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		Template template = cfg.getTemplate(templateName);
		try (StringWriter out = new StringWriter()) {
			template.process(templateData, out);
			message = out.getBuffer().toString();
			out.flush();
		}
		return message;
	}

}
