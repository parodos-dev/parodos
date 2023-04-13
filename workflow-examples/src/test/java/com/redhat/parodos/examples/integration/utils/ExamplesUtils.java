package com.redhat.parodos.examples.integration.utils;

import com.redhat.parodos.sdk.api.ApiCallback;
import com.redhat.parodos.sdk.api.ApiException;
import com.redhat.parodos.sdk.api.ProjectApi;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
public final class ExamplesUtils {

	public static void waitProjectStart(ProjectApi projectApi) throws ApiException {
		AtomicBoolean retry = new AtomicBoolean(true);
		ApiCallback<List<ProjectResponseDTO>> apiCallback = new ApiCallback<>() {
			AtomicInteger failureCounter = new AtomicInteger(0);

			@Override
			public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
				int i = failureCounter.incrementAndGet();
				if (i >= 100) {
					retry.set(false);
				}
			}

			@Override
			public void onSuccess(List<ProjectResponseDTO> result, int statusCode,
					Map<String, List<String>> responseHeaders) {
				retry.set(false);
			}

			@Override
			public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
				return;
			}

			@Override
			public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
				return;
			}
		};

		if (retry.get()) {
			projectApi.getProjectsAsync(apiCallback);
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

}