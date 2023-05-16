package com.redhat.parodos.tasks.rest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

interface RestService {

	ResponseEntity<String> exchange(String url, HttpMethod method, HttpEntity<String> requestEntity)
			throws RestClientException;

}
