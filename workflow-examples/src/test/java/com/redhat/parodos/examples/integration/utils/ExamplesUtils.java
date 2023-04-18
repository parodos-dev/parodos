
package com.redhat.parodos.examples.integration.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.model.ProjectRequestDTO;
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

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public final class ExamplesUtils {

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

	public static void waitProjectStart(ProjectApi projectApi) throws InterruptedException, ApiException {
		waitAsyncResponse((FuncExecutor<List<ProjectResponseDTO>>) callback -> projectApi.getProjectsAsync(callback));
	}

	public static WorkFlowStatusResponseDTO waitWorkflowStatusAsync(WorkflowApi workflowApi, String workFlowExecutionId)
			throws InterruptedException, ApiException {
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = waitAsyncResponse(new FuncExecutor<>() {
			@Override
			public boolean check(WorkFlowStatusResponseDTO result) {
				return !result.getStatus().equals(WorkStatus.COMPLETED.toString());
			}

			@Override
			public void execute(ApiCallback<WorkFlowStatusResponseDTO> callback) throws ApiException {
				workflowApi.getStatusAsync(workFlowExecutionId, callback);
			}
		});
		return workFlowStatusResponseDTO;
	}

	@Nullable
	public static ProjectResponseDTO getProjectByNameAndDescription(List<ProjectResponseDTO> projects,
			String projectName, String projectDescription) {
		return projects.stream()
				.filter(prj -> projectName.equals(prj.getName()) && projectDescription.equals(prj.getDescription())
						&& prj.getUsername() == null && !Strings.isNullOrEmpty(prj.getId()))
				.findAny().orElse(null);
	}

	@Data
	private static class AsyncResult<T> {

		private String error;

		T result;

		int statusCode;

	}

	public interface FuncExecutor<T> {

		void execute(ApiCallback<T> callback) throws ApiException;

		default boolean check(T result) {
			return true;
		}

	}

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