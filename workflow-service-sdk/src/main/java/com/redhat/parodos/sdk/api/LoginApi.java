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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.parodos.sdk.invoker.ApiCallback;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.ApiResponse;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.Pair;

public class LoginApi {

	private ApiClient localVarApiClient;

	private int localHostIndex;

	private String localCustomBaseUrl;

	public LoginApi() {
		this(Configuration.getDefaultApiClient());
	}

	public LoginApi(ApiClient apiClient) {
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
	 * Build call for login
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
	 * <td>400</td>
	 * <td>Bad Request</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>404</td>
	 * <td>Not Found</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>409</td>
	 * <td>Conflict</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public okhttp3.Call loginCall(final ApiCallback _callback) throws ApiException {
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
		String localVarPath = "/api/v1/login";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = { "*/*" };
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
	private okhttp3.Call loginValidateBeforeCall(final ApiCallback _callback) throws ApiException {
		return loginCall(_callback);

	}

	/**
	 * Login
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
	 * <td>400</td>
	 * <td>Bad Request</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>404</td>
	 * <td>Not Found</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>409</td>
	 * <td>Conflict</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public void login() throws ApiException {
		loginWithHttpInfo();
	}

	/**
	 * Login
	 * @return ApiResponse&lt;Void&gt;
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
	 * <td>400</td>
	 * <td>Bad Request</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>404</td>
	 * <td>Not Found</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>409</td>
	 * <td>Conflict</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public ApiResponse<Void> loginWithHttpInfo() throws ApiException {
		okhttp3.Call localVarCall = loginValidateBeforeCall(null);
		return localVarApiClient.execute(localVarCall);
	}

	/**
	 * Login (asynchronously)
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
	 * <td>400</td>
	 * <td>Bad Request</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>401</td>
	 * <td>Unauthorized</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>404</td>
	 * <td>Not Found</td>
	 * <td>-</td>
	 * </tr>
	 * <tr>
	 * <td>409</td>
	 * <td>Conflict</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public okhttp3.Call loginAsync(final ApiCallback<Void> _callback) throws ApiException {

		okhttp3.Call localVarCall = loginValidateBeforeCall(_callback);
		localVarApiClient.executeAsync(localVarCall, _callback);
		return localVarCall;
	}

}
