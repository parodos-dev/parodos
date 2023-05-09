package com.redhat.parodos;

import com.redhat.parodos.workflow.utils.CredUtils;
import lombok.Getter;
import lombok.Setter;

import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public abstract class ControllerMockClient {

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

	public HttpHeaders headersWithInValidCredentials() {
		HttpHeaders headers = this.initHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, this.invalidCredentials());
		return headers;
	}

	public MockHttpServletRequestBuilder getRequestWithValidCredentials(String path) {
		return MockMvcRequestBuilders.get(path).headers(this.headersWithValidCredentials());
	}

	public MockHttpServletRequestBuilder postRequestWithValidCredentials(String path) {
		return MockMvcRequestBuilders.post(path).headers(this.headersWithValidCredentials());
	}

	public MockHttpServletRequestBuilder deleteRequestWithValidCredentials(String path) {
		return MockMvcRequestBuilders.delete(path).headers(this.headersWithValidCredentials());
	}

	public MockHttpServletRequestBuilder getRequestWithInValidCredentials(String path) {
		return MockMvcRequestBuilders.get(path).headers(this.headersWithInValidCredentials());
	}

	public MockHttpServletRequestBuilder postRequestWithInValidCredentials(String path) {
		return MockMvcRequestBuilders.post(path).headers(this.headersWithInValidCredentials());
	}

	public String invalidCredentials() {
		return String.format("Basic %s", CredUtils.getBase64Creds("foo", "bar"));
	}

	public String credentials() {
		return String.format("Basic %s", CredUtils.getBase64Creds(this.getValidUser(), this.getValidPassword()));
	}

}
