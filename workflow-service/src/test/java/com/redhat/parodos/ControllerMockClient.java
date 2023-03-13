package com.redhat.parodos;

import com.redhat.parodos.examples.utils.RestUtils;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
		return get(path).headers(this.headersWithValidCredentials());
	}

	public MockHttpServletRequestBuilder postRequestWithValidCredentials(String path) {
		return post(path).headers(this.headersWithValidCredentials());
	}

	public MockHttpServletRequestBuilder getRequestWithInValidCredentials(String path) {
		return get(path).headers(this.headersWithInValidCredentials());
	}

	public MockHttpServletRequestBuilder postRequestWithInValidCredentials(String path) {
		return post(path).headers(this.headersWithInValidCredentials());
	}

	public String invalidCredentials() {
		return String.format("Basic %s", RestUtils.getBase64Creds("foo", "bar"));
	}

	public String credentials() {
		return String.format("Basic %s", RestUtils.getBase64Creds(this.getValidUser(), this.getValidPassword()));
	}

}
