
package com.redhat.parodos.examples.integration.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.model.ProjectRequestDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

import com.redhat.parodos.sdk.api.ProjectApi;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiCallback;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.assertj.core.util.Strings;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import lombok.Data;

/***
 * A utility
 *
 * class to ease the writing of new examples.
 */

@Slf4j
public final class ExamplesUtils {

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
				fail("An error occurred while executing waitAsyncResponse: " + asyncResult.getError());
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
	public static WorkFlowStatusResponseDTO waitWorkflowStatusAsync(WorkflowApi workflowApi, String workFlowExecutionId)
			throws InterruptedException, ApiException {
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = waitAsyncResponse(new FuncExecutor<>() {
			@Override
			public boolean check(WorkFlowStatusResponseDTO result) {
				return !result.getStatus().equals(WorkStatus.COMPLETED.toString());
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
		return projects.stream()
				.filter(prj -> projectName.equals(prj.getName()) && projectDescription.equals(prj.getDescription())
						&& prj.getUsername() == null && prj.getId() == null)
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
			assertNotNull(projectResponseDTO);
			assertEquals(projectName, projectResponseDTO.getName());
			assertEquals(projectDescription, projectResponseDTO.getDescription());
			log.info("Project {} successfully created", projectName);
		}

		// ASSERT PROJECT "testProject" IS PRESENT
		projects = projectApi.getProjects();
		log.debug("PROJECTS: {}", projects);
		assertTrue(projects.size() > 0);
		testProject = getProjectByNameAndDescription(projects, projectName, projectDescription);
		assertNotNull(testProject);

		return testProject;
	}

}