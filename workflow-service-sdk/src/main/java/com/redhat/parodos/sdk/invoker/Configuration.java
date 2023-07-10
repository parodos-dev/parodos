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

package com.redhat.parodos.sdk.invoker;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class Configuration {

	public static final String VERSION = "1.0.20-SNAPSHOT";

	private static ApiClient defaultApiClient = new ApiClient();

	/**
	 * Get the default API client, which would be used when creating API instances without
	 * providing an API client.
	 * @return Default API client
	 */
	public static ApiClient getDefaultApiClient() {
		return defaultApiClient;
	}

	/**
	 * Set the default API client, which would be used when creating API instances without
	 * providing an API client.
	 * @param apiClient API client
	 */
	public static void setDefaultApiClient(ApiClient apiClient) {
		defaultApiClient = apiClient;
	}

}
