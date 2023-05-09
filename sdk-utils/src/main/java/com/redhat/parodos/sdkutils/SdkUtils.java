package com.redhat.parodos.sdkutils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.redhat.parodos.sdk.api.LoginApi;
import com.redhat.parodos.sdk.api.ProjectApi;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiCallback;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.ApiResponse;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.model.ProjectRequestDTO;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflow.utils.CredUtils;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.MissingRequiredPropertiesException;

/***
 * A utility class to ease the writing of new examples.
 */

@Slf4j
public abstract class SdkUtils {

	private static String serverIp = "localhost";

	private SdkUtils() {
	}

	/**
	 * Creates and configures the APIClient using the configuration properties available
	 * in `application.yml`
	 * @return the ApiClient
	 */
	public static ApiClient getParodosAPiClient() throws ApiException, MissingRequiredPropertiesException {
		ApiClient apiClient = Configuration.getDefaultApiClient();
		CustomPropertiesReader reader = new CustomPropertiesReader();
		serverIp = reader.getServerIp();
		String serverPort = reader.getServerPort();

		if (Strings.isNullOrEmpty(serverIp) || Strings.isNullOrEmpty(serverPort)) {
			throw new MissingRequiredPropertiesException();
		}

		int port = Integer.parseInt(serverPort);
		if (port <= 0 && port > 65535) {
			throw new IllegalArgumentException("serverPort must be > 0 && <= 65535");
		}

		String basePath = "http://" + serverIp + ":" + serverPort;
		log.info("serverIp is: {}, serverPort is {}. Set BasePath to {}", serverIp, serverPort, basePath);

		apiClient.setBasePath(basePath);
		apiClient.addDefaultHeader("Authorization", "Basic " + CredUtils.getBase64Creds("test", "test"));

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
	 * Executes a @see FuncExecutor. Waits at most 60 seconds for a successful result of
	 * an async API invocation.
	 * @param f the @see FuncExecutor
	 * @param <T> the type of the function executor
	 * @return @see AsyncResult
	 * @throws ApiException if the api invocation fails
	 * @throws InterruptedException If the async call reaches the waiting timeout
	 */
	public static <T> T waitAsyncResponse(FuncExecutor<T> f) throws ApiException, InterruptedException {
		AsyncResult<T> asyncResult = new AsyncResult<>();
		Lock lock = new ReentrantLock();
		Condition response = lock.newCondition();
		ApiCallback<T> apiCallback = new ApiCallback<T>() {

			@Override
			public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
				log.info("onFailure {}", e.getMessage());
				try {
					f.execute(this);
				}
				catch (ApiException apie) {
					asyncResult.setError(apie.getMessage());
					signal();
				}
			}

			@Override
			public void onSuccess(T result, int statusCode, Map<String, List<String>> responseHeaders) {
				if (f.check(result)) {
					try {
						f.execute(this);
					}
					catch (ApiException apie) {
						asyncResult.setError(apie.getMessage());
						signal();
					}
				}
				else {
					asyncResult.setStatusCode(statusCode);
					asyncResult.setResult(result);
					asyncResult.setError(null);
					signal();
				}
			}

			@Override
			public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
			}

			@Override
			public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
			}

			private void signal() {
				lock.lock();
				try {
					response.signal();
				}
				finally {
					lock.unlock();
				}
			}
		};
		f.execute(apiCallback);
		lock.lock();
		try {
			// should be more than enough
			response.await(60, TimeUnit.SECONDS);
			if (asyncResult.getError() != null) {
				throw new ApiException(
						"An error occurred while executing waitAsyncResponse: " + asyncResult.getError());
			}
		}
		finally {
			lock.unlock();
		}
		return asyncResult.getResult();
	}

	/**
	 * Invokes @see
	 * com.redhat.parodos.sdk.api.ProjectAPI#getProjectsAsync(ApiCallback<List<ProjectResponseDTO>>)
	 * and retries for 60 seconds.
	 * @param projectApi the Project API
	 * @throws InterruptedException If the async call reaches the waiting timeout
	 * @throws ApiException If the API method invocation fails
	 */
	public static void waitProjectStart(ProjectApi projectApi) throws InterruptedException, ApiException {
		waitAsyncResponse((FuncExecutor<List<ProjectResponseDTO>>) callback -> projectApi.getProjectsAsync(callback));
	}

	/**
	 * Invokes @see com.redhat.parodos.sdk.api.WorkflowApi#getStatusAsync(String,
	 * ApiCallback<WorkFlowStatusResponseDTO>) and retries for 60 seconds.
	 * @param workflowApi the WorkflowAPI
	 * @param workFlowExecutionId the workflow execution Id to monitor, as {String}
	 * @return the workflow status if it's equal to @see
	 * com.redhat.parodos.workflows.work.WorkStatus#COMPLETED
	 * @throws InterruptedException If the async call reaches the waiting timeout
	 * @throws ApiException If the API method invocation fails
	 */
	public static WorkFlowStatusResponseDTO waitWorkflowStatusAsync(WorkflowApi workflowApi, UUID workFlowExecutionId)
			throws InterruptedException, ApiException {
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = waitAsyncResponse(new FuncExecutor<>() {
			@Override
			public boolean check(WorkFlowStatusResponseDTO result) {
				return !result.getStatus().equals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED);
			}

			@Override
			public void execute(@NonNull ApiCallback<WorkFlowStatusResponseDTO> callback) throws ApiException {
				workflowApi.getStatusAsync(workFlowExecutionId, callback);
			}
		});
		return workFlowStatusResponseDTO;
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

	@Data
	private static class AsyncResult<T> {

		private String error;

		T result;

		int statusCode;

	}

	public interface FuncExecutor<T> {

		/**
		 * Defines the @see com.redhat.parodos.sdk.invoker.ApiCallback to execute
		 * @param callback the
		 * @throws ApiException If the API callback invocation fails
		 */
		void execute(@NonNull ApiCallback<T> callback) throws ApiException;

		/**
		 * Define when considering an ApiCallback result as successful.
		 * @param result the result to check
		 * @return {true} if it is necessary to continue monitoring the result, {false}
		 * when it's possible to stop the monitoring.
		 */
		default boolean check(T result) {
			return true;
		}

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
	 * @throws InterruptedException If the async call reaches the waiting timeout
	 * @throws ApiException If the API methods invocations fail
	 */
	public static ProjectResponseDTO getProjectAsync(ApiClient apiClient, String projectName, String projectDescription)
			throws InterruptedException, ApiException {
		ProjectApi projectApi = new ProjectApi(apiClient);
		log.info("Wait project to be ready on {}", apiClient.getBasePath());
		waitProjectStart(projectApi);
		log.info("Project is ✔️ on {}", apiClient.getBasePath());

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
		log.debug("PROJECTS: {}", projects);

		if (projects.isEmpty()) {
			throw new ApiException("Project has not been created.");
		}
		testProject = getProjectByNameAndDescription(projects, projectName, projectDescription);

		if (testProject == null) {
			throw new ApiException("Can retrieve project with name " + projectName);
		}
		return testProject;
	}

	public static String getServerIp() {
		return serverIp;
	}
}
