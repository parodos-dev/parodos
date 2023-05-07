/*
 * Parodos Workflow Service API
 * This is the API documentation for the Parodos Workflow Service. It provides operations to execute assessments to determine infrastructure options (tooling + environments). Also executes infrastructure task workflows to call downstream systems to stand-up an infrastructure option.
 *
 * The version of the OpenAPI document: v1.0.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.redhat.parodos.sdk.api;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.reflect.TypeToken;
import com.redhat.parodos.sdk.invoker.ApiCallback;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.ApiResponse;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.Pair;
import com.redhat.parodos.sdk.model.ProjectRequestDTO;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;

public class ProjectApi {

	private ApiClient localVarApiClient;

	private int localHostIndex;

	private String localCustomBaseUrl;

	public ProjectApi() {
		this(Configuration.getDefaultApiClient());
	}

	public ProjectApi(ApiClient apiClient) {
		this.localVarApiClient = apiClient;
	}

	public ApiClient getApiClient() {
		return localVarApiClient;
	}

	public void setApiClient(ApiClient apiClient) {
		this.localVarApiClient = apiClient;
	}

	public int getHostIndex() {
		return localHostIndex;
	}

	public void setHostIndex(int hostIndex) {
		this.localHostIndex = hostIndex;
	}

	public String getCustomBaseUrl() {
		return localCustomBaseUrl;
	}

	public void setCustomBaseUrl(String customBaseUrl) {
		this.localCustomBaseUrl = customBaseUrl;
	}

	/**
	 * Build call for createProject
	 * @param projectRequestDTO (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>201</td>
	 * <td>Created</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public okhttp3.Call createProjectCall(ProjectRequestDTO projectRequestDTO, final ApiCallback _callback)
			throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		}
		else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		}
		else {
			basePath = null;
		}

		Object localVarPostBody = projectRequestDTO;

		// create path and map variables
		String localVarPath = "/api/v1/projects";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = { "application/json" };
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = { "application/json" };
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "POST", localVarQueryParams,
				localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams,
				localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call createProjectValidateBeforeCall(ProjectRequestDTO projectRequestDTO,
			final ApiCallback _callback) throws ApiException {
		// verify the required parameter 'projectRequestDTO' is set
		if (projectRequestDTO == null) {
			throw new ApiException(
					"Missing the required parameter 'projectRequestDTO' when calling createProject(Async)");
		}

		return createProjectCall(projectRequestDTO, _callback);

	}

	/**
	 * Creates a new project
	 * @param projectRequestDTO (required)
	 * @return ProjectResponseDTO
	 * @throws ApiException If fail to call the API, e.g. server error or cannot
	 * deserialize the response body
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>201</td>
	 * <td>Created</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public ProjectResponseDTO createProject(ProjectRequestDTO projectRequestDTO) throws ApiException {
		ApiResponse<ProjectResponseDTO> localVarResp = createProjectWithHttpInfo(projectRequestDTO);
		return localVarResp.getData();
	}

	/**
	 * Creates a new project
	 * @param projectRequestDTO (required)
	 * @return ApiResponse&lt;ProjectResponseDTO&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot
	 * deserialize the response body
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>201</td>
	 * <td>Created</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public ApiResponse<ProjectResponseDTO> createProjectWithHttpInfo(ProjectRequestDTO projectRequestDTO)
			throws ApiException {
		okhttp3.Call localVarCall = createProjectValidateBeforeCall(projectRequestDTO, null);
		Type localVarReturnType = new TypeToken<ProjectResponseDTO>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Creates a new project (asynchronously)
	 * @param projectRequestDTO (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request
	 * body object
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>201</td>
	 * <td>Created</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public okhttp3.Call createProjectAsync(ProjectRequestDTO projectRequestDTO,
			final ApiCallback<ProjectResponseDTO> _callback) throws ApiException {

		okhttp3.Call localVarCall = createProjectValidateBeforeCall(projectRequestDTO, _callback);
		Type localVarReturnType = new TypeToken<ProjectResponseDTO>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getProjectById
	 * @param id (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>200</td>
	 * <td>Succeeded</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>404</td>
	 * <td>Not found</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public okhttp3.Call getProjectByIdCall(UUID id, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		}
		else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		}
		else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/api/v1/projects/{id}".replace("{" + "id" + "}",
				localVarApiClient.escapeString(id.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = { "application/json" };
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams,
				localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams,
				localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getProjectByIdValidateBeforeCall(UUID id, final ApiCallback _callback) throws ApiException {
		// verify the required parameter 'id' is set
		if (id == null) {
			throw new ApiException("Missing the required parameter 'id' when calling getProjectById(Async)");
		}

		return getProjectByIdCall(id, _callback);

	}

	/**
	 * Returns information about a specified project
	 * @param id (required)
	 * @return ProjectResponseDTO
	 * @throws ApiException If fail to call the API, e.g. server error or cannot
	 * deserialize the response body
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>200</td>
	 * <td>Succeeded</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>404</td>
	 * <td>Not found</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public ProjectResponseDTO getProjectById(UUID id) throws ApiException {
		ApiResponse<ProjectResponseDTO> localVarResp = getProjectByIdWithHttpInfo(id);
		return localVarResp.getData();
	}

	/**
	 * Returns information about a specified project
	 * @param id (required)
	 * @return ApiResponse&lt;ProjectResponseDTO&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot
	 * deserialize the response body
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>200</td>
	 * <td>Succeeded</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>404</td>
	 * <td>Not found</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public ApiResponse<ProjectResponseDTO> getProjectByIdWithHttpInfo(UUID id) throws ApiException {
		okhttp3.Call localVarCall = getProjectByIdValidateBeforeCall(id, null);
		Type localVarReturnType = new TypeToken<ProjectResponseDTO>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Returns information about a specified project (asynchronously)
	 * @param id (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request
	 * body object
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>200</td>
	 * <td>Succeeded</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>404</td>
	 * <td>Not found</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public okhttp3.Call getProjectByIdAsync(UUID id, final ApiCallback<ProjectResponseDTO> _callback)
			throws ApiException {

		okhttp3.Call localVarCall = getProjectByIdValidateBeforeCall(id, _callback);
		Type localVarReturnType = new TypeToken<ProjectResponseDTO>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getProjects
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>200</td>
	 * <td>Succeeded</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>403</td>
	 * <td>Forbidden</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public okhttp3.Call getProjectsCall(final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		}
		else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		}
		else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/api/v1/projects";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = { "application/json" };
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams,
				localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams,
				localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getProjectsValidateBeforeCall(final ApiCallback _callback) throws ApiException {
		return getProjectsCall(_callback);

	}

	/**
	 * Returns a list of project
	 * @return List&lt;ProjectResponseDTO&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot
	 * deserialize the response body
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>200</td>
	 * <td>Succeeded</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>403</td>
	 * <td>Forbidden</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public List<ProjectResponseDTO> getProjects() throws ApiException {
		ApiResponse<List<ProjectResponseDTO>> localVarResp = getProjectsWithHttpInfo();
		return localVarResp.getData();
	}

	/**
	 * Returns a list of project
	 * @return ApiResponse&lt;List&lt;ProjectResponseDTO&gt;&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot
	 * deserialize the response body
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>200</td>
	 * <td>Succeeded</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>403</td>
	 * <td>Forbidden</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public ApiResponse<List<ProjectResponseDTO>> getProjectsWithHttpInfo() throws ApiException {
		okhttp3.Call localVarCall = getProjectsValidateBeforeCall(null);
		Type localVarReturnType = new TypeToken<List<ProjectResponseDTO>>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Returns a list of project (asynchronously)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request
	 * body object
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>200</td>
	 * <td>Succeeded</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>403</td>
	 * <td>Forbidden</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public okhttp3.Call getProjectsAsync(final ApiCallback<List<ProjectResponseDTO>> _callback) throws ApiException {

		okhttp3.Call localVarCall = getProjectsValidateBeforeCall(_callback);
		Type localVarReturnType = new TypeToken<List<ProjectResponseDTO>>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

}
