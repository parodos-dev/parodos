package com.redhat.parodos.sdkutils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.redhat.parodos.sdk.api.LoginApi;
import com.redhat.parodos.sdk.api.ProjectApi;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.ApiResponse;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.model.ProjectRequestDTO;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO.StatusEnum;
import com.redhat.parodos.workflow.utils.CredUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.MissingRequiredPropertiesException;

/***
 * A utility class to ease the writing of new examples.
 */

@Slf4j
public abstract class WorkFlowServiceUtils {

	private WorkFlowServiceUtils() {
	}

	/**
	 * Creates and configures the APIClient using the configuration properties available
	 * in environment variables.
	 * @return the ApiClient
	 */
	public static ApiClient getParodosAPiClient() throws ApiException, MissingRequiredPropertiesException {
		ApiClient apiClient = Configuration.getDefaultApiClient();
		String serverIp = Optional.ofNullable(System.getenv("WORKFLOW_SERVICE_HOST")).orElse("localhost");
		String serverPort = Optional.ofNullable(System.getenv("SERVER_PORT")).orElse("8080");

		if (Strings.isNullOrEmpty(serverIp) || Strings.isNullOrEmpty(serverPort)) {
			throw new IllegalArgumentException("SERVER_IP and SERVER_PORT must be set");
		}

		int port = Integer.parseInt(serverPort);
		if (port <= 0 || port > 65535) {
			throw new IllegalArgumentException("serverPort must be > 0 && <= 65535");
		}

		String basePath = "http://%s:%s".formatted(serverIp, serverPort);
		log.info("serverIp is: {}, serverPort is {}. Set BasePath to {}", serverIp, serverPort, basePath);

		apiClient.setBasePath(basePath);
		apiClient.addDefaultHeader("Authorization", "Basic " + CredUtils.getBase64Creds("test", "test"));
		waitProjectStart(new ProjectApi(apiClient));
		// Need to execute a GET method to get JSessionId and CSRF Token
		LoginApi loginApi = new LoginApi(apiClient);
		ApiResponse<Void> loginResponse = loginApi.loginWithHttpInfo();
		Map<String, List<String>> headers = loginResponse.getHeaders();
		List<String> cookieHeaders = headers.get("Set-Cookie");
		String xsrfToken = null;
		String JSessionID = null;
		if (cookieHeaders != null) {
			xsrfToken = getCookieValue(cookieHeaders, "XSRF-TOKEN");
			JSessionID = getCookieValue(cookieHeaders, "JSESSIONID");
		}

		log.debug("Found X-CSRF-TOKEN: {} and JSessionID: {}", xsrfToken, JSessionID);
		if (xsrfToken != null) {
			apiClient.addDefaultHeader("X-XSRF-TOKEN", xsrfToken);
			apiClient.addDefaultCookie("JSESSIONID", JSessionID);
			apiClient.addDefaultCookie("XSRF-TOKEN", xsrfToken);
		}
		return apiClient;
	}

	@Nullable
	private static String getCookieValue(List<String> cookieHeaders, String anObject) {
		String token = null;
		for (String cookieHeader : cookieHeaders) {
			token = Stream.of(cookieHeader.split(";")).map(cookie -> cookie.trim().split("="))
					.filter(parts -> parts.length == 2 && parts[0].equals(anObject)).findFirst().map(parts -> parts[1])
					.orElse(null);
			if (token != null) {
				break;
			}
		}
		return token;
	}

	/**
	 * Invokes @see
	 * com.redhat.parodos.sdk.api.ProjectAPI#getProjectsAsync(ApiCallback<List<ProjectResponseDTO>>)
	 * and retries for 60 seconds.
	 * @param projectApi the Project API
	 */
	public static void waitProjectStart(ProjectApi projectApi) {
		try (var executorService = new RetryExecutorService<Void>()) {
			Callable<Void> task = () -> {
				projectApi.getProjects();
				return null;
			};

			executorService.submitWithRetry(task);
		}
		catch (Exception e) {
			throw new RuntimeException("Project API is not up and running", e);
		}
	}

	/**
	 * Invokes @see com.redhat.parodos.sdk.api.WorkflowApi#getStatusAsync(String,
	 * ApiCallback<WorkFlowStatusResponseDTO>) and retries for 60 seconds.
	 * @param workflowApi the WorkflowAPI
	 * @param workFlowExecutionId the workflow execution ID to monitor, as {String}
	 * @return the workflow status if it's equal to @see
	 * com.redhat.parodos.workflows.work.WorkStatus#COMPLETED
	 */
	public static WorkFlowStatusResponseDTO waitWorkflowStatusAsync(WorkflowApi workflowApi, UUID workFlowExecutionId) {
		return waitWorkflowStatusAsync(workflowApi, workFlowExecutionId, StatusEnum.COMPLETED);
	}

	/**
	 * Invokes @see com.redhat.parodos.sdk.api.WorkflowApi#getStatusAsync(String,
	 * ApiCallback<WorkFlowStatusResponseDTO>) and retries for 60 seconds.
	 * @param workflowApi the WorkflowAPI
	 * @param workFlowExecutionId the workflow execution ID to monitor, as {String}
	 * @param expectedStatus the expectedStatus to wait for
	 * @return the workflow expectedStatus if it's equal to @see
	 * com.redhat.parodos.workflows.work.WorkStatus#COMPLETED
	 */
	public static WorkFlowStatusResponseDTO waitWorkflowStatusAsync(WorkflowApi workflowApi, UUID workFlowExecutionId,
			StatusEnum expectedStatus) {
		WorkFlowStatusResponseDTO result;

		try (var executorService = new RetryExecutorService<WorkFlowStatusResponseDTO>()) {
			Callable<WorkFlowStatusResponseDTO> task = () -> {
				WorkFlowStatusResponseDTO status = workflowApi.getStatus(workFlowExecutionId);
				if (status.getStatus() != expectedStatus) {
					throw new ApiException("Workflow status is not " + expectedStatus);
				}
				return status;
			};

			result = executorService.submitWithRetry(task);
		}
		catch (Exception e) {
			throw new RuntimeException("Workflow status is not " + expectedStatus, e);
		}
		return result;
	}

	/**
	 * Finds a project with @see #projectName and @see #projectDescription
	 * @param projects List to of project to analyze
	 * @param projectName the {String} project name to find
	 * @param projectDescription the {String} project decription to find
	 * @return the {ProjectResponse} if the project exists, {null} otherwise
	 */
	@Nullable
	public static ProjectResponseDTO getProjectByNameAndDescription(List<ProjectResponseDTO> projects,
			String projectName, String projectDescription) {
		return projects
				.stream().filter(prj -> projectName.equals(prj.getName())
						&& projectDescription.equals(prj.getDescription()) && prj.getId() != null)
				.findAny().orElse(null);
	}

	/**
	 * Checks if a project with {projectName} and {projectDescription} exists. Creates a
	 * new project, if it doesn't exist and asserts that it has been successfully created.
	 * <p>
	 * Returns the {ProjectAPI} response for the project with {projectName} and
	 * {projectDescription}.
	 * @param apiClient the API client
	 * @param projectName the project name
	 * @param projectDescription the project description
	 * @return The ProjectApiResponse
	 * @throws ApiException If the API methods invocations fail
	 */
	public static ProjectResponseDTO getProjectAsync(ApiClient apiClient, String projectName, String projectDescription)
			throws ApiException {
		ProjectApi projectApi = new ProjectApi(apiClient);

		ProjectResponseDTO testProject;

		// RETRIEVE ALL PROJECTS AVAILABLE
		log.info("Get all available projects");
		List<ProjectResponseDTO> projects = projectApi.getProjects();

		// CHECK IF testProject ALREADY EXISTS
		testProject = getProjectByNameAndDescription(projects, projectName, projectDescription);

		// CREATE PROJECT "Test Project Name" IF NOT EXISTS
		if (testProject == null) {
			log.info("There are no projects. Creating project {}", projectName);
			// DEFINE A TEST PROJECT REQUEST
			ProjectRequestDTO projectRequestDTO = new ProjectRequestDTO();
			projectRequestDTO.setName(projectName);
			projectRequestDTO.setDescription(projectDescription);

			ProjectResponseDTO projectResponseDTO = projectApi.createProject(projectRequestDTO);

			if (projectResponseDTO == null || projectResponseDTO.getName() == null
					|| projectResponseDTO.getDescription() == null) {
				throw new ApiException("Can't create new project");
			}
			if (!projectName.equals(projectResponseDTO.getName())
					|| !projectDescription.equals(projectResponseDTO.getDescription())) {
				throw new ApiException("Can't create project correctly. Requested project name and description are: "
						+ projectName + "," + projectDescription + ". Actual are: " + projectResponseDTO.getName()
						+ projectResponseDTO.getDescription());
			}
			log.info("Project {} successfully created", projectName);
		}

		// ASSERT PROJECT "testProject" IS PRESENT
		projects = projectApi.getProjects();

		if (projects.isEmpty()) {
			throw new ApiException("Project has not been created.");
		}
		testProject = getProjectByNameAndDescription(projects, projectName, projectDescription);

		if (testProject == null) {
			throw new ApiException("Can retrieve project with name " + projectName);
		}
		return testProject;
	}

}
