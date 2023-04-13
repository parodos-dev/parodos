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

import com.redhat.parodos.sdk.api.ApiCallback;
import com.redhat.parodos.sdk.api.ApiClient;
import com.redhat.parodos.sdk.api.ApiException;
import com.redhat.parodos.sdk.api.ApiResponse;
import com.redhat.parodos.sdk.api.Configuration;
import com.redhat.parodos.sdk.api.Pair;
import com.redhat.parodos.sdk.api.ProgressRequestBody;
import com.redhat.parodos.sdk.api.ProgressResponseBody;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowDefinitionApi {

	private ApiClient localVarApiClient;

	private int localHostIndex;

	private String localCustomBaseUrl;

	public WorkflowDefinitionApi() {
		this(Configuration.getDefaultApiClient());
	}

	public WorkflowDefinitionApi(ApiClient apiClient) {
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
	 * Build call for getWorkFlowDefinitionById
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
	public okhttp3.Call getWorkFlowDefinitionByIdCall(String id, final ApiCallback _callback) throws ApiException {
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
		String localVarPath = "/api/v1/workflowdefinitions/{id}".replaceAll("\\{" + "id" + "\\}",
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

		final String[] localVarContentTypes = {

		};
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
	private okhttp3.Call getWorkFlowDefinitionByIdValidateBeforeCall(String id, final ApiCallback _callback)
			throws ApiException {

		// verify the required parameter 'id' is set
		if (id == null) {
			throw new ApiException("Missing the required parameter 'id' when calling getWorkFlowDefinitionById(Async)");
		}

		okhttp3.Call localVarCall = getWorkFlowDefinitionByIdCall(id, _callback);
		return localVarCall;

	}

	/**
	 * Returns information about a workflow definition by id
	 * @param id (required)
	 * @return WorkFlowDefinitionResponseDTO
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
	public WorkFlowDefinitionResponseDTO getWorkFlowDefinitionById(String id) throws ApiException {
		ApiResponse<WorkFlowDefinitionResponseDTO> localVarResp = getWorkFlowDefinitionByIdWithHttpInfo(id);
		return localVarResp.getData();
	}

	/**
	 * Returns information about a workflow definition by id
	 * @param id (required)
	 * @return ApiResponse&lt;WorkFlowDefinitionResponseDTO&gt;
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
	public ApiResponse<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionByIdWithHttpInfo(String id)
			throws ApiException {
		okhttp3.Call localVarCall = getWorkFlowDefinitionByIdValidateBeforeCall(id, null);
		Type localVarReturnType = new TypeToken<WorkFlowDefinitionResponseDTO>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Returns information about a workflow definition by id (asynchronously)
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
	public okhttp3.Call getWorkFlowDefinitionByIdAsync(String id,
			final ApiCallback<WorkFlowDefinitionResponseDTO> _callback) throws ApiException {

		okhttp3.Call localVarCall = getWorkFlowDefinitionByIdValidateBeforeCall(id, _callback);
		Type localVarReturnType = new TypeToken<WorkFlowDefinitionResponseDTO>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getWorkFlowDefinitions
	 * @param name (optional)
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
	public okhttp3.Call getWorkFlowDefinitionsCall(String name, final ApiCallback _callback) throws ApiException {
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
		String localVarPath = "/api/v1/workflowdefinitions";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (name != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("name", name));
		}

		final String[] localVarAccepts = { "application/json" };
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
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
	private okhttp3.Call getWorkFlowDefinitionsValidateBeforeCall(String name, final ApiCallback _callback)
			throws ApiException {

		okhttp3.Call localVarCall = getWorkFlowDefinitionsCall(name, _callback);
		return localVarCall;

	}

	/**
	 * Returns a list of workflow definition
	 * @param name (optional)
	 * @return List&lt;WorkFlowDefinitionResponseDTO&gt;
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
	public List<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitions(String name) throws ApiException {
		ApiResponse<List<WorkFlowDefinitionResponseDTO>> localVarResp = getWorkFlowDefinitionsWithHttpInfo(name);
		return localVarResp.getData();
	}

	/**
	 * Returns a list of workflow definition
	 * @param name (optional)
	 * @return ApiResponse&lt;List&lt;WorkFlowDefinitionResponseDTO&gt;&gt;
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
	public ApiResponse<List<WorkFlowDefinitionResponseDTO>> getWorkFlowDefinitionsWithHttpInfo(String name)
			throws ApiException {
		okhttp3.Call localVarCall = getWorkFlowDefinitionsValidateBeforeCall(name, null);
		Type localVarReturnType = new TypeToken<List<WorkFlowDefinitionResponseDTO>>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Returns a list of workflow definition (asynchronously)
	 * @param name (optional)
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
	public okhttp3.Call getWorkFlowDefinitionsAsync(String name,
			final ApiCallback<List<WorkFlowDefinitionResponseDTO>> _callback) throws ApiException {

		okhttp3.Call localVarCall = getWorkFlowDefinitionsValidateBeforeCall(name, _callback);
		Type localVarReturnType = new TypeToken<List<WorkFlowDefinitionResponseDTO>>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

}
