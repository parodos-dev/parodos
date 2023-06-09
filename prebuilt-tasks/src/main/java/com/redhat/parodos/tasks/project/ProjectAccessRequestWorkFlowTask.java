package com.redhat.parodos.tasks.project;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.redhat.parodos.project.enums.Role;
import com.redhat.parodos.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;

import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.ACCESS_REQUEST_APPROVAL_USERNAMES;
import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.ACCESS_REQUEST_ESCALATION_USERNAME;
import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.ACCESS_REQUEST_ID;
import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.PARAMETER_ROLE;
import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.PARAMETER_ROLE_DEFAULT;
import static com.redhat.parodos.tasks.project.consts.ProjectAccessRequestConstant.PARAMETER_USERNAME;

@Slf4j
public class ProjectAccessRequestWorkFlowTask extends BaseWorkFlowTask {

	private final String serviceUrl;

	private final String servicePort;

	private final String serviceAccountUsername;

	private final String serviceAccountPassword;

	public ProjectAccessRequestWorkFlowTask(String serviceUrl, String servicePort, String serviceAccountUsername,
			String serviceAccountPassword) {
		super();
		this.serviceUrl = serviceUrl;
		this.servicePort = servicePort;
		this.serviceAccountUsername = serviceAccountUsername;
		this.serviceAccountPassword = serviceAccountPassword;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		String username;
		String role;
		try {
			username = getRequiredParameterValue(PARAMETER_USERNAME);
			role = getOptionalParameterValue(PARAMETER_ROLE, PARAMETER_ROLE_DEFAULT);
			log.info("Project access request with the following - username: {}, role: {}", username, role);
		}
		catch (MissingParameterException e) {
			log.error("Exception when trying to get required parameter(s): {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		try {
			Role.valueOf(role.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			log.error("Exception when trying to convert role requested: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		try {
			String url = String.format("http://%s:%s/api/v1/projects/%s/access", serviceUrl, servicePort,
					getProjectId(workContext));
			log.info("url: {}", url);
			AccessRequestDTO requestDTO = AccessRequestDTO.builder().username(username)
					.role(Role.valueOf(role.toUpperCase())).build();
			ResponseEntity<AccessResponseDTO> responseDTO = RestUtils.executePost(url, requestDTO,
					serviceAccountUsername, serviceAccountPassword, AccessResponseDTO.class);
			if (responseDTO.getStatusCode().is2xxSuccessful()) {
				log.info("Rest call completed with response: {}", responseDTO.getBody());
				addParameter(ACCESS_REQUEST_ID,
						Objects.requireNonNull(responseDTO.getBody()).accessRequestId.toString());
				addParameter(ACCESS_REQUEST_APPROVAL_USERNAMES,
						String.join(",", Objects.requireNonNull(responseDTO.getBody()).getApprovalSentTo()));
				addParameter(ACCESS_REQUEST_ESCALATION_USERNAME,
						String.join(",", Objects.requireNonNull(responseDTO.getBody()).getEscalationSentTo()));
				return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
			}
			log.error("Call to the api was not successful with status code: {}", responseDTO.getStatusCode());
		}
		catch (Exception e) {
			log.error("There was an issue with the REST call: {}", e.getMessage());
		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key(PARAMETER_USERNAME).type(WorkParameterType.TEXT).optional(false)
						.description("The project id to assign user into").build(),
				WorkParameter.builder().key(PARAMETER_ROLE).type(WorkParameterType.TEXT).optional(true)
						.description("The role to grant to the user").build());
	}

	@Data
	@Builder
	private static class AccessRequestDTO {

		private String username;

		private Role role;

	}

	@Data
	private static class AccessResponseDTO {

		private UUID accessRequestId;

		private List<String> approvalSentTo;

		private String escalationSentTo;

	}

}
