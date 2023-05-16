package com.redhat.parodos.tasks.rest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class RestServiceImpl implements RestService {

	@Override
	public ResponseEntity<String> exchange(String url, HttpMethod method, HttpEntity<String> requestEntity)
			throws RestClientException {
		return new RestTemplate().exchange(url, method, requestEntity, String.class);
	}

}
