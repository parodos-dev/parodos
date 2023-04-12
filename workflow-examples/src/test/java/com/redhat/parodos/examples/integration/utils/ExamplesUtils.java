package com.redhat.parodos.examples.integration.utils;

import com.redhat.parodos.sdk.api.ApiCallback;
import com.redhat.parodos.sdk.api.ApiException;
import com.redhat.parodos.sdk.api.ProjectApi;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public final class ExamplesUtils {

	static final int maxTry = 100;

	public static void waitProjectStart(ProjectApi projectApi) throws ApiException, InterruptedException {

		AtomicInteger tryCount = new AtomicInteger(0);
		AsyncResult<List<ProjectResponseDTO>> asyncResult = new AsyncResult<>();
		ApiCallback<List<ProjectResponseDTO>> apiCallback = new ApiCallback<>() {
			AtomicInteger failureCounter = new AtomicInteger(0);

			@Override
			public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
				int i = failureCounter.incrementAndGet();
				if (i >= 100) {
					asyncResult.setError(e.getMessage());
					asyncResult.setStatus(statusCode);
				}
			}

			@Override
			public void onSuccess(List<ProjectResponseDTO> result, int statusCode,
					Map<String, List<String>> responseHeaders) {
				asyncResult.setResponse(result);
				asyncResult.setStatus(statusCode);
			}

			@Override
			public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
			}

			@Override
			public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
			}
		};

		do {
			if (tryCount.get() >= maxTry) {
				fail("Can get results from getProjectsAsync, timeout reached.");
			}
			Thread.sleep(1000);
			tryCount.incrementAndGet();

			if (asyncResult.responseReceived()) {
				if (asyncResult.getError() != null) {
					fail("An error occurred while executing getProjectsAsync: " + asyncResult.getError()
							+ ". Status code: " + asyncResult.getStatus());
				}
				if (asyncResult.getResponse() != null) {
					break;
				}
				// retry async call
				projectApi.getProjectsAsync(apiCallback);
			}
		}
		while (!asyncResult.responseReceived());

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

		private Integer status;

		private T response;

		private AsyncResult() {
			clear();
		}

		private boolean responseReceived() {
			return error != null && status != null && response != null;
		}

		private void clear() {
			error = null;
			status = null;
			response = null;
		}

	}

}