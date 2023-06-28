package com.redhat.parodos.notification.controller.base;

import java.util.Base64;

import lombok.Getter;
import lombok.Setter;

import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public abstract class BaseControllerTests {

	@Getter
	@Setter
	private String validUser = "test";

	@Getter
	@Setter
	private String validPassword = "test";

	private HttpHeaders initHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
		return headers;
	}

	public HttpHeaders headersWithValidCredentials() {
		HttpHeaders headers = this.initHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, this.credentials());
		return headers;
	}

	public HttpHeaders headersWithInvalidCredentials() {
		HttpHeaders headers = this.initHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, this.invalidCredentials());
		return headers;
	}

	public MockHttpServletRequestBuilder getRequestWithValidCredentials(String path) {
		return MockMvcRequestBuilders.get(path).headers(this.headersWithValidCredentials());
	}

	public MockHttpServletRequestBuilder getRequestWithInvalidCredentials(String path) {
		return MockMvcRequestBuilders.get(path).headers(this.headersWithInvalidCredentials());
	}

	public MockHttpServletRequestBuilder postRequestWithValidCredentials(String path) {
		return MockMvcRequestBuilders.post(path).headers(this.headersWithValidCredentials());
	}

	public MockHttpServletRequestBuilder postRequestWithInvalidCredentials(String path) {
		return MockMvcRequestBuilders.post(path).headers(this.headersWithInvalidCredentials());
	}

	public MockHttpServletRequestBuilder deleteRequestWithValidCredentials(String path) {
		return MockMvcRequestBuilders.delete(path).headers(this.headersWithValidCredentials());
	}

	public MockHttpServletRequestBuilder deleteRequestWithInvalidCredentials(String path) {
		return MockMvcRequestBuilders.delete(path).headers(this.headersWithInvalidCredentials());
	}

	public MockHttpServletRequestBuilder putRequestWithValidCredentials(String path) {
		return MockMvcRequestBuilders.put(path).headers(this.headersWithValidCredentials());
	}

	public MockHttpServletRequestBuilder putRequestWithInvalidCredentials(String path) {
		return MockMvcRequestBuilders.put(path).headers(this.headersWithInvalidCredentials());
	}

	public String invalidCredentials() {
		return String.format("Basic %s", getBase64Creds("foo", "bar"));
	}

	public String credentials() {
		return String.format("Basic %s", getBase64Creds(this.getValidUser(), this.getValidPassword()));
	}

	/**
	 * Generates a Base64 encoding of username:password string
	 * @param username the username
	 * @param password the password
	 * @return the Base64 encoded string
	 */
	private static String getBase64Creds(String username, String password) {
		String plainCreds = username + ":" + password;
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);
		return base64Creds;
	}

}
