package com.redhat.parodos.utils;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestUtils is a utility class. All its methods must be declared as static so they can't
 * be overridden.
 */
public abstract class RestUtils {

	/**
	 * The constructor is private so it'll prevent instantiation.
	 */
	private RestUtils() {
		throw new UnsupportedOperationException("Suppress default constructor for non instantiability");
	}

	/**
	 * Create a new resource by POSTing the given payload to the URL, and returns the
	 * response as ResponseEntity.
	 * @param urlString the URL
	 * @param payload object to post
	 * @return @see org.springframework.http.ResponseEntity
	 * @see org.springframework.web.client.RestTemplate#postForEntity(URI, Object, Class)
	 */
	public static ResponseEntity<String> executePost(String urlString, String payload) {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForEntity(urlString, payload, String.class);
	}

	/**
	 * Create a new resource by POSTing the given requestEntity to the URL, and returns
	 * the response as String.
	 * @param urlString the URL
	 * @param requestEntity object to post
	 * @return the response as string
	 * @see org.springframework.web.client.RestTemplate#postForEntity(URI, Object, Class)
	 */
	public static ResponseEntity<String> executePost(String urlString, HttpEntity<?> requestEntity) {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.exchange(urlString, HttpMethod.POST, requestEntity, String.class);
	}

	public static <T, E> ResponseEntity<E> executePost(String urlString, T requestDto, String username, String password,
			Class<E> responseType) {
		HttpEntity<T> request = getRequestWithHeaders(requestDto, username, password);
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForEntity(urlString, request, responseType);
	}

	public static <T, E> ResponseEntity<E> executePost(RestTemplate restTemplate, String urlString, T requestDto,
			String username, String password, Class<E> responseType) {
		HttpEntity<T> request = getRequestWithHeaders(requestDto, username, password);
		return restTemplate.postForEntity(urlString, request, responseType);
	}

	/**
	 * Execute the GET HTTP method to the given URL
	 * @param urlString the URL
	 * @return HTTP response as string
	 */
	public static ResponseEntity<String> executeGet(String urlString) {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.getForEntity(urlString, String.class);
	}

	/**
	 * Execute the GET HTTP method to the given URL, writing the given request entity to
	 * the request, and returns the response as ResponseEntity.
	 * @param urlString the URL
	 * @param username the username
	 * @param password the password
	 * @return @see org.springframework.http.ResponseEntity
	 * @see org.springframework.web.client.RestTemplate#exchange(String, HttpMethod,
	 * HttpEntity, Class, Object...)
	 */
	public static ResponseEntity<String> restExchange(String urlString, String username, String password) {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.exchange(urlString, HttpMethod.GET, getRequestWithHeaders(username, password),
				String.class);
	}

	public static <E> ResponseEntity<E> restExchange(String urlString, String username, String password,
			Class<E> responseType) {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.exchange(urlString, HttpMethod.GET, getRequestWithHeaders(username, password),
				responseType);
	}

	public static <E> ResponseEntity<E> restExchange(RestTemplate restTemplate, String urlString, String username,
			String password, Class<E> responseType) {
		return restTemplate.exchange(urlString, HttpMethod.GET, getRequestWithHeaders(username, password),
				responseType);
	}

	public static RestTemplate ignoreSSLVerifyRestTemplate() {
		try {
			SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
					SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
					NoopHostnameVerifier.INSTANCE);
			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(scsf).build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

			// requestFactory.setHttpClient(httpClient);
			return new RestTemplate(requestFactory);
		}
		catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			return new RestTemplate();
		}
	}

	/**
	 * Creates a new HttpHeader and set the Authorization object according to @see
	 * getBase64Creds
	 * @param username the username
	 * @param password the password
	 * @return the @see org.springframework.http.HttpEntity
	 */
	public static HttpEntity<String> getRequestWithHeaders(String username, String password) {
		String base64Creds = getBase64Creds(username, password);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		return new HttpEntity<>(headers);
	}

	/**
	 * Creates a new HttpHeader with request and set the Authorization object according
	 * to @see getBase64Creds
	 * @param username the username
	 * @param password the password
	 * @return the @see org.springframework.http.HttpEntity
	 */
	public static <T> HttpEntity<T> getRequestWithHeaders(T request, String username, String password) {
		String base64Creds = getBase64Creds(username, password);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(request, headers);
	}

	/**
	 * Generates a Base64 encoding of username:password string
	 * @param username the username
	 * @param password the password
	 * @return the Base64 encoded string
	 */
	public static String getBase64Creds(String username, String password) {
		String plainCreds = username + ":" + password;
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
		return new String(base64CredsBytes);
	}

}
