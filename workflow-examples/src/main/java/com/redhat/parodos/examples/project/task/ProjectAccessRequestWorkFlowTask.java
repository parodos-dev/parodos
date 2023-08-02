package com.redhat.parodos.examples.project.task;

import java.util.List;
import java.util.Objects;

import com.redhat.parodos.examples.project.client.ProjectRequester;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.AccessRequestDTO;
import com.redhat.parodos.sdk.model.AccessResponseDTO;
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

import static com.redhat.parodos.examples.project.consts.ProjectAccessRequestConstant.ACCESS_REQUEST_APPROVAL_USERNAMES;
import static com.redhat.parodos.examples.project.consts.ProjectAccessRequestConstant.ACCESS_REQUEST_ESCALATION_USERNAME;
import static com.redhat.parodos.examples.project.consts.ProjectAccessRequestConstant.ACCESS_REQUEST_ID;
import static com.redhat.parodos.examples.project.consts.ProjectAccessRequestConstant.PARAMETER_ROLE;
import static com.redhat.parodos.examples.project.consts.ProjectAccessRequestConstant.PARAMETER_ROLE_DEFAULT;
import static com.redhat.parodos.examples.project.consts.ProjectAccessRequestConstant.PARAMETER_USERNAME;

@Slf4j
public class ProjectAccessRequestWorkFlowTask extends BaseWorkFlowTask {

	private final ProjectRequester projectRequester;

	public ProjectAccessRequestWorkFlowTask(ProjectRequester projectRequester) {
		this.projectRequester = projectRequester;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		String username, role;
		try {
			username = getRequiredParameterValue(PARAMETER_USERNAME);
			role = getOptionalParameterValue(PARAMETER_ROLE, PARAMETER_ROLE_DEFAULT, false);
			log.info("Project access request with the following - username: {}, role: {}", username, role);
		}
		catch (MissingParameterException e) {
			log.error("Exception when trying to get required parameter(s): {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		try {
			AccessRequestDTO.RoleEnum.valueOf(role.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			log.error("Exception when trying to convert role requested: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		AccessRequestDTO accessRequestDTO = new AccessRequestDTO();
		accessRequestDTO.setUsername(username);
		accessRequestDTO.setRole(AccessRequestDTO.RoleEnum.valueOf(role.toUpperCase()));
		accessRequestDTO.setUsername(username);
		try {
			AccessResponseDTO accessResponseDTO = projectRequester.createAccess(getProjectId(workContext),
					accessRequestDTO);
			addParameter(ACCESS_REQUEST_ID, Objects.requireNonNull(accessResponseDTO.getAccessRequestId()).toString());
			addParameter(ACCESS_REQUEST_APPROVAL_USERNAMES,
					String.join(",", Objects.requireNonNull(accessResponseDTO.getApprovalSentTo())));
			addParameter(ACCESS_REQUEST_ESCALATION_USERNAME, String.join(",", accessResponseDTO.getEscalationSentTo()));
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		catch (ApiException e) {
			log.error("There was an issue with the api call: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		return List.of(
				WorkParameter.builder().key(PARAMETER_USERNAME).type(WorkParameterType.TEXT).optional(false)
						.description("The project id to assign user into").build(),
				WorkParameter.builder().key(PARAMETER_ROLE).type(WorkParameterType.TEXT).optional(true)
						.description("The role to grant to the user").build());
	}

}
