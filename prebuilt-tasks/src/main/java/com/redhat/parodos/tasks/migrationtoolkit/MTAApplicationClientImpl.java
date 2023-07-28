package com.redhat.parodos.tasks.migrationtoolkit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import jakarta.ws.rs.NotFoundException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

@Slf4j
class MTAClient implements MTAApplicationClient, MTATaskGroupClient {

	private final HttpClient client;

	private final URI serverURI;

	private final String bearerToken;

	private final ObjectMapper mapper;

	MTAClient(URI serverURI, String bearerToken) {
		this.serverURI = serverURI;
		this.bearerToken = bearerToken;
		this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		try {
			SSLContext nonValidatingSSLContext = SSLContext.getInstance("SSL");
			nonValidatingSSLContext.init(null, new TrustManager[] { new nonValidatingTrustManager() },
					SecureRandom.getInstanceStrong());
			this.client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
					.sslContext(nonValidatingSSLContext).build();
		}
		catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Result<App> get(String name) {
		// apps in MTA have unique constraints on name.
		try {
			HttpResponse<String> getAll = client.send(
					HttpRequest.newBuilder().GET().uri(serverURI.resolve("/hub/applications")).build(),
					HttpResponse.BodyHandlers.ofString());
			if (getAll.statusCode() != HTTP_OK) {
				return new Result.Failure<>(null);
			}
			List<App> apps = mapper.readValue(getAll.body(), new TypeReference<>() {
			});

			Optional<App> app = apps.stream().filter(v -> v.name().equals(name)).findFirst();
			if (app.isPresent()) {
				return new Result.Success<>(app.get());
			}
			else {
				return new Result.Failure<>(new NotFoundException("failed to find application by name " + name));
			}
		}
		catch (IOException | InterruptedException e) {
			return new Result.Failure<>(e);
		}
	}

	@Override
	public Result<Identity> getIdentity(String name) {
		// identities in MTA have unique constraints on name.
		try {
			HttpResponse<String> getAll = client.send(
					HttpRequest.newBuilder().GET().uri(serverURI.resolve("/hub/identities")).build(),
					HttpResponse.BodyHandlers.ofString());
			if (getAll.statusCode() != HTTP_OK) {
				return null;
			}
			List<Identity> identities = mapper.readValue(getAll.body(), new TypeReference<>() {
			});

			Optional<Identity> identity = identities.stream().filter(v -> v.name().equals(name)).findFirst();
			if (identity.isPresent()) {
				return new Result.Success<>(identity.get());
			}
			else {
				return new Result.Failure<>(new NotFoundException("failed to find identity by name " + name));
			}
		}
		catch (IOException | InterruptedException e) {
			return new Result.Failure<>(e);
		}
	}

	// TODO unknown if we need to create the app or expect the app to be already present
	// in MTA
	@Override
	public Result<App> create(App app) {
		try {
			String body = mapper.writeValueAsString(app);
			HttpResponse<String> create = client
					.send(HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(body))
							.uri(serverURI.resolve("/hub/applications")).build(), HttpResponse.BodyHandlers.ofString());
			if (create.statusCode() != HTTP_CREATED) {
				return new Result.Failure<>(null);
			}
			App newApp = mapper.readValue(create.body(), new TypeReference<>() {
			});
			return new Result.Success<>(newApp);
		}
		catch (IOException | InterruptedException e) {
			return new Result.Failure<>(e);
		}
	}

	@Override
	public Result<TaskGroup> create(int appId) {

		try {
			var tgnew = TaskGroup.ofCloudReadiness(appId);

			// first post a task group, it will be used later to put on the
			// taskgroups/{id}/submit to start it
			var body = mapper.writeValueAsString(tgnew);
			log.debug("creating an MTA taskgroup for application ID {}", appId);
			HttpResponse<String> postTG = client.send(HttpRequest.newBuilder()
					.POST(HttpRequest.BodyPublishers.ofString(body)).uri(serverURI.resolve("/hub/taskgroups")).build(),
					HttpResponse.BodyHandlers.ofString());

			TaskGroup tg = mapper.readValue(postTG.body(), new TypeReference<>() {
			});

			// put on taskgroups/{id}/submit to start the report
			body = mapper.writeValueAsString(tg);
			HttpResponse<String> submitTG = client.send(
					HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString(body))
							.uri(serverURI.resolve("/hub/taskgroups/" + tg.id() + "/submit")).build(),
					HttpResponse.BodyHandlers.ofString());

			return switch (submitTG.statusCode()) {
				case HTTP_CREATED, HTTP_NO_CONTENT -> new Result.Success<>(tg);
				default -> new Result.Failure<>(new Exception(
						String.format("Http client error %s, code %d", submitTG.body(), submitTG.statusCode())));
			};
		}
		catch (Exception e) {
			return new Result.Failure<>(e);
		}
	}

	@Override
	public Result<TaskGroup> get(int id) {
		try {
			var getTG = client.send(
					HttpRequest.newBuilder().GET().uri(serverURI.resolve("/hub/taskgroups/" + id)).build(),
					HttpResponse.BodyHandlers.ofString());
			if (getTG.statusCode() == HTTP_OK) {
				TaskGroup tg = mapper.readValue(getTG.body(), new TypeReference<>() {
				});
				return new Result.Success<>(tg);
			}
			else {
				return new Result.Failure<>(null);
			}
		}
		catch (Throwable t) {
			return new Result.Failure<>(t);
		}
	}

	static class nonValidatingTrustManager implements X509TrustManager {

		public nonValidatingTrustManager() {
		}

		public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

	}

}
