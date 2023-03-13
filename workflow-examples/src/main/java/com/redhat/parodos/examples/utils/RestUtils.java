package com.redhat.parodos.examples.utils;

import com.redhat.parodos.workflow.utils.CredUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * RestUtils is an utility class. All its methods must be declared as static so they can't
 * be overridden.
 */
public final class RestUtils {

	/**
	 * The constructor is private so it'll prevent instantiation.
	 * @throws UnsupportedOperationException
	 */
	private RestUtils() {
		throw new UnsupportedOperationException("Suppress default constructor for non instantiability");
	}

	/**
	 * Create a new resource by POSTing the given payload to the URL, and returns the
	 * response as ResponseEntity.
	 * @see org.springframework.web.client.RestTemplate#postForEntity(URI, Object, Class)
	 * @param urlString the URL
	 * @param payload object to post
	 * @return @see org.springframework.http.ResponseEntity
	 */
	public static ResponseEntity<String> executePost(String urlString, String payload) {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForEntity(urlString, payload, String.class);
	}

	/**
	 * Execute the GET HTTP method to the given URL, writing the given request entity to
	 * the request, and returns the response as ResponseEntity.
	 *
	 * @see org.springframework.web.client.RestTemplate#exchange(String, HttpMethod,
	 * HttpEntity, Class, Object...)
	 * @param urlString the URL
	 * @param username the username
	 * @param password the password
	 * @return @see org.springframework.http.ResponseEntity
	 */
	public static ResponseEntity<String> restExchange(String urlString, String username, String password) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result = restTemplate.exchange(urlString, HttpMethod.GET,
				getRequestWithHeaders(username, password), String.class);
		return result;
	}

	/**
	 * Creates a new HttpHeader and set the Authorization object according to @see
	 * getBase64Creds
	 * @param username the username
	 * @param password the password
	 * @return the @see org.springframework.http.HttpEntity
	 */
	public static HttpEntity<String> getRequestWithHeaders(String username, String password) {
		String base64Creds = CredUtils.getBase64Creds(username, password);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		return new HttpEntity<>(headers);
	}

}
