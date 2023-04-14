
package com.redhat.parodos.examples.integration.utils;

import com.redhat.parodos.sdk.api.ProjectApi;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiCallback;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import lombok.Data;
import org.assertj.core.util.Strings;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.fail;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
public final class ExamplesUtils {

	public static void waitProjectStart(ProjectApi projectApi) throws ApiException, InterruptedException {
		AsyncResult asyncResult = new AsyncResult();
		Lock lock = new ReentrantLock();
		Condition response = lock.newCondition();
		ApiCallback<List<ProjectResponseDTO>> apiCallback = new ApiCallback<>() {
			AtomicInteger failureCounter = new AtomicInteger(0);

			@Override
			public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
				int i = failureCounter.incrementAndGet();
				if (i >= 100) {
					asyncResult.setError(e.getMessage());
					signal();
				}
				else {
					try {
						projectApi.getProjectsAsync(this);
					}
					catch (ApiException apie) {
						asyncResult.setError(apie.getMessage());
						signal();
					}
				}
			}

			@Override
			public void onSuccess(List<ProjectResponseDTO> result, int statusCode,
					Map<String, List<String>> responseHeaders) {
				signal();
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
		projectApi.getProjectsAsync(apiCallback);
		lock.lock();
		try {
			// should be more than enough
			response.await(60, TimeUnit.SECONDS);
			if (asyncResult.getError() != null) {
				fail("An error occurred while executing getProjectsAsync: " + asyncResult.getError());
			}
		}
		finally {
			lock.unlock();
		}
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
	private static class AsyncResult {

		private String error;

	}

}