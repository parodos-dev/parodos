package com.redhat.parodos.examples.integration.utils;

import com.redhat.parodos.sdk.api.ApiException;
import com.redhat.parodos.sdk.api.ApiResponse;
import com.redhat.parodos.sdk.api.ProjectApi;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import org.apache.http.HttpStatus;
import org.assertj.core.util.Strings;

import javax.annotation.Nullable;
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

    @Nullable
    public static ProjectResponseDTO getProjectByNameAndDescription(List<ProjectResponseDTO> projects, String projectName, String projectDescription) {
        return projects.stream()
                .filter(prj -> projectName.equals(prj.getName()) && projectDescription.equals(prj.getDescription())
                        && prj.getUsername() == null && !Strings.isNullOrEmpty(prj.getId()))
                .findAny().orElse(null);
    }
}