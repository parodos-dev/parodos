package com.redhat.parodos.examples.integration.utils;

import com.redhat.parodos.sdk.api.ApiException;
import com.redhat.parodos.sdk.api.ApiResponse;
import com.redhat.parodos.sdk.api.ProjectApi;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import org.apache.http.HttpStatus;

import java.util.List;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
public final class ExamplesUtils {

	public static void waitProjectStart(ProjectApi projectApi) throws ApiException {
		ApiResponse<List<ProjectResponseDTO>> projectsWithHttpInfo = projectApi.getProjectsWithHttpInfo();
		for (int i = 0; i < 100; i++) {
			if (projectsWithHttpInfo.getStatusCode() == HttpStatus.SC_OK) {
				return;
			}
		}
		System.out.println("wait");
		throw new ApiException("HTTP request response with unexpected " + projectsWithHttpInfo.getStatusCode());
	}

}