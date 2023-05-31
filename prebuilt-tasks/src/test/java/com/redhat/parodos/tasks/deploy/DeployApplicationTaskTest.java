package com.redhat.parodos.tasks.deploy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.utils.WorkContextUtils;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.api.model.RouteIngressBuilder;
import io.fabric8.openshift.api.model.RouteStatusBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.redhat.parodos.tasks.deploy.DeployConstants.APPLICATION_HOSTNAMES;
import static com.redhat.parodos.tasks.deploy.DeployConstants.KUBECONFIG;
import static com.redhat.parodos.tasks.deploy.DeployConstants.MANIFESTS_PATH;
import static com.redhat.parodos.tasks.deploy.DeployConstants.NAMESPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

public class DeployApplicationTaskTest {

	private DeployApplicationTask underTest;

	private OpenShiftClient osClient;

	private OpenShiftServer mockServer;

	@BeforeEach
	public void setUp() throws Exception {
		underTest = new DeployApplicationTask(kubeConfigString -> osClient);
	}

	@Test
	public void getWorkFlowTaskParameters() {
		// when
		List<WorkParameter> params = underTest.getWorkFlowTaskParameters();

		// then
		assertThat(params).isNotNull();
		assertThat(params.size()).isEqualTo(3);
		assertThat(params.stream().map(WorkParameter::getKey).toList()).contains(KUBECONFIG, NAMESPACE, MANIFESTS_PATH);
	}

	@Test
	public void execute_missing_parameters() {
		// given
		WorkContext workContext = getIncompleteWorkContext();

		// when
		underTest.preExecute(workContext);
		WorkReport report = underTest.execute(workContext);

		// then
		assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(report.getError()).isNotNull();
		assertThat(report.getError()).isInstanceOf(MissingParameterException.class);
		assertThat(report.getError().getMessage()).contains(MANIFESTS_PATH);
	}

	@Test
	public void execute_manifests_path_does_not_exist() {
		// given
		String manifestsPath = UUID.randomUUID().toString();
		WorkContext workContext = getCompleteWorkContext(manifestsPath);

		// when
		underTest.preExecute(workContext);
		WorkReport report = underTest.execute(workContext);

		// then
		assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(report.getError()).isNotNull();
		assertThat(report.getError()).isInstanceOf(RuntimeException.class);
		assertThat(report.getError().getMessage()).contains("Failed to read manifest files from path")
				.contains(manifestsPath);
	}

	@SneakyThrows(IOException.class)
	@Test
	public void execute_no_files_with_manifests_suffix() {
		// given
		Path tempDirectory = ManifestsProvider.createTempDirectory();
		Path tempFile = Files.createTempFile(tempDirectory, "test", ".txt");
		String manifestsPath = tempDirectory.toAbsolutePath().toString();
		WorkContext workContext = getCompleteWorkContext(manifestsPath);

		// when
		underTest.preExecute(workContext);
		WorkReport report = underTest.execute(workContext);

		// then
		assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(report.getError()).isNotNull();
		assertThat(report.getError()).isInstanceOf(RuntimeException.class);
		assertThat(report.getError().getMessage()).contains("No manifest files found in path").contains(manifestsPath);

		// cleanup
		assertThatNoException().isThrownBy(() -> {
			Files.deleteIfExists(tempFile);
			Files.deleteIfExists(tempDirectory);
		});
	}

	@Nested
	class TestWithInvalidOpenShiftServer {

		private ManifestsProvider.ManifestFiles manifestFiles;

		@BeforeEach
		public void setUp() throws Exception {
			manifestFiles = ManifestsProvider.createManifests();

			underTest = new DeployApplicationTask(kubeConfigString -> {
				throw new KubernetesClientException("Error");
			});
		}

		@AfterEach
		public void tearDown() {
			assertThatNoException().isThrownBy(() -> ManifestsProvider.deleteManifests(manifestFiles));
		}

		@Test
		public void execute_invalid_openshift_client() {
			// given
			WorkContext workContext = getCompleteWorkContext(manifestFiles.manifestsPath.toAbsolutePath().toString());

			// when
			underTest.preExecute(workContext);
			WorkReport report = underTest.execute(workContext);

			// then
			assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
			assertThat(report.getError()).isNotNull();
			assertThat(report.getError()).isInstanceOf(RuntimeException.class);
			assertThat(report.getError().getMessage()).contains("Failed to create OpenShift client with error");
		}

	}

	@Nested
	class TestWithOpenShiftServer {

		private ManifestsProvider.ManifestFiles manifestFiles;

		@BeforeEach
		public void setUp() throws Exception {
			manifestFiles = ManifestsProvider.createManifests();
			mockServer = new OpenShiftServer(false, false);
			mockServer.before();
			osClient = mockServer.getOpenshiftClient();
			underTest = new DeployApplicationTask(kubeConfigString -> osClient);
		}

		@AfterEach
		public void tearDown() {
			mockServer.after();
			assertThatNoException().isThrownBy(() -> ManifestsProvider.deleteManifests(manifestFiles));
		}

		@Test
		public void execute_successfully() {
			// given
			// @formatter:off
			WorkContext workContext = getCompleteWorkContext(manifestFiles.manifestsPath.toAbsolutePath().toString());
			Deployment deployment = new DeploymentBuilder().withNewMetadata()
					.withName("my-deployment")
					.withNamespace("default").and().build();
			mockServer.expect()
					.post()
					.withPath("/apis/apps/v1/namespaces/default/deployments")
					.andReturn(200, deployment)
					.once();

			Route route = new RouteBuilder().withNewMetadata()
				.withName("my-route")
				.withNamespace("default")
				.endMetadata()
				.withNewSpec()
				.withHost("my-host")
				.endSpec()
				.build();
			mockServer.expect()
					.post()
					.withPath("/apis/route.openshift.io/v1/namespaces/default/routes")
					.andReturn(200, route)
					.once();

			mockServer.expect()
					.get()
					.withPath("/apis/route.openshift.io/v1/namespaces/default/routes/my-route")
					.andReturn(200, new RouteBuilder().withNewMetadata()
							.withName("my-route")
							.withNamespace("default")
							.endMetadata()
							.withNewSpec()
							.withHost("my-host")
							.endSpec()
							.withStatus(new RouteStatusBuilder().withIngress(
									new RouteIngressBuilder().withHost("my-host").build()
							).build())
							.build())
					.once();
			// @formatter:on

			// when
			underTest.preExecute(workContext);
			WorkReport report = underTest.execute(workContext);

			// then
			assertThat(report.getStatus()).isEqualTo(WorkStatus.COMPLETED);
			assertThat(report.getError()).isNull();
			assertThat(report.getWorkContext().getContext()).isNotNull();
			Object appHostnames = report.getWorkContext().getContext().get(APPLICATION_HOSTNAMES);
			assertThat(appHostnames).isNotNull();
			assertThat(appHostnames).isInstanceOf(List.class);
			@SuppressWarnings("unchecked")
			List<String> appHostnamesList = (List<String>) appHostnames;
			assertThat(appHostnamesList).containsExactly("my-host");
		}

		@Test
		public void execute_failed_applying_manifests() {
			// given
			WorkContext workContext = getCompleteWorkContext(manifestFiles.manifestsPath.toAbsolutePath().toString());

			// when
			underTest.preExecute(workContext);
			WorkReport report = underTest.execute(workContext);

			// then
			assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
			assertThat(report.getError()).isNotNull();
			assertThat(report.getError()).isInstanceOf(ManifestDeployException.class);
			assertThat(report.getError().getMessage()).contains("Failed to create manifest");
		}

		@Test
		public void execute_failed_creating_routes() {
			// given
			// @formatter:off
			WorkContext workContext = getCompleteWorkContext(manifestFiles.manifestsPath.toAbsolutePath().toString());
			Deployment deployment = new DeploymentBuilder().withNewMetadata()
					.withName("my-deployment")
					.withNamespace("default").and().build();
			mockServer.expect()
					.post()
					.withPath("/apis/apps/v1/namespaces/default/deployments")
					.andReturn(200, deployment)
					.once();

			Route route = new RouteBuilder().withNewMetadata()
					.withName("my-route")
					.withNamespace("default")
					.endMetadata()
					.withNewSpec()
					.withHost("my-host")
					.endSpec()
					.build();
			mockServer.expect()
					.post()
					.withPath("/apis/route.openshift.io/v1/namespaces/default/routes")
					.andReturn(400, route)
					.once();
			// @formatter:on

			// when
			underTest.preExecute(workContext);
			WorkReport report = underTest.execute(workContext);

			// then
			assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
			assertThat(report.getError()).isNotNull();
			assertThat(report.getError()).isInstanceOf(ManifestDeployException.class);
		}

		@Test
		public void execute_failed_reading_routes() {
			// given
			// @formatter:off
			WorkContext workContext = getCompleteWorkContext(manifestFiles.manifestsPath.toAbsolutePath().toString());
			Deployment deployment = new DeploymentBuilder().withNewMetadata()
					.withName("my-deployment")
					.withNamespace("default").and().build();
			mockServer.expect()
					.post()
					.withPath("/apis/apps/v1/namespaces/default/deployments")
					.andReturn(200, deployment)
					.once();

			Route route = new RouteBuilder().withNewMetadata()
					.withName("my-route")
					.withNamespace("default")
					.endMetadata()
					.withNewSpec()
					.withHost("my-host")
					.endSpec()
					.build();
			mockServer.expect()
					.post()
					.withPath("/apis/route.openshift.io/v1/namespaces/default/routes")
					.andReturn(200, route)
					.once();

			mockServer.expect()
					.get()
					.withPath("/apis/route.openshift.io/v1/namespaces/default/routes/my-route")
					.andReturn(400, "Some error")
					.once();
			// @formatter:on

			// when
			underTest.preExecute(workContext);
			WorkReport report = underTest.execute(workContext);

			// then
			assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
			assertThat(report.getError()).isNotNull();
			assertThat(report.getError()).isInstanceOf(ManifestDeployException.class);
			assertThat(report.getError().getMessage()).contains("Failed to get route").contains("default/my-route");
		}

		@Test
		public void execute_failed_finding_routes() {
			// given
			// @formatter:off
			WorkContext workContext = getCompleteWorkContext(manifestFiles.manifestsPath.toAbsolutePath().toString());
			Deployment deployment = new DeploymentBuilder().withNewMetadata()
					.withName("my-deployment")
					.withNamespace("default").and().build();
			mockServer.expect()
					.post()
					.withPath("/apis/apps/v1/namespaces/default/deployments")
					.andReturn(200, deployment)
					.once();

			Route route = new RouteBuilder().withNewMetadata()
					.withName("my-route")
					.withNamespace("default")
					.endMetadata()
					.withNewSpec()
					.withHost("my-host")
					.endSpec()
					.build();
			mockServer.expect()
					.post()
					.withPath("/apis/route.openshift.io/v1/namespaces/default/routes")
					.andReturn(200, route)
					.once();

			mockServer.expect()
					.get()
					.withPath("/apis/route.openshift.io/v1/namespaces/default/routes/my-route")
					.andReturn(404, "Not found")
					.once();
			// @formatter:on

			// when
			underTest.preExecute(workContext);
			WorkReport report = underTest.execute(workContext);

			// then
			assertThat(report.getStatus()).isEqualTo(WorkStatus.FAILED);
			assertThat(report.getError()).isNotNull();
			assertThat(report.getError()).isInstanceOf(ManifestDeployException.class);
			assertThat(report.getError().getMessage()).contains("not found").contains("default/my-route");
		}

	}

	private WorkContext getCompleteWorkContext(String manifestsPath) {
		WorkContext workContext = new WorkContext();
		WorkContextUtils.setMainExecutionId(workContext, UUID.randomUUID());
		workContext.put(KUBECONFIG, "kubeconfig-content");
		workContext.put(NAMESPACE, "default");
		workContext.put(MANIFESTS_PATH, manifestsPath);
		return workContext;
	}

	private WorkContext getIncompleteWorkContext() {
		WorkContext workContext = new WorkContext();
		WorkContextUtils.setMainExecutionId(workContext, UUID.randomUUID());
		workContext.put(KUBECONFIG, "kubeconfig");
		return workContext;
	}

	private static class ManifestsProvider {

		@SneakyThrows(IOException.class)
		static Path createTempDirectory() {
			return Files.createTempDirectory("deploy");
		}

		@SneakyThrows(IOException.class)
		static Path createRouterFile(Path parentDir) {
			// Define the content of the OpenShift route in YAML format
			// @formatter:off
			String routeContent = """
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: my-route
spec:
  host: example.com
  to:
    kind: Service
    name: my-service""";
			// @formatter:on
			Path routeFile = Files.createTempFile(parentDir, "openshift-route", ".yaml");
			Files.write(routeFile, routeContent.getBytes(), StandardOpenOption.WRITE);
			return routeFile;
		}

		@SneakyThrows(IOException.class)
		static Path createDeploymentFile(Path parentDir) {
			// @formatter:off
			String deploymentContent = """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: my-app
  template:
    metadata:
      labels:
        app: my-app
    spec:
      containers:
      - name: my-container
        image: my-image:latest""";
			// @formatter:on
			Path deploymentFile = Files.createTempFile(parentDir, "deployment", ".yml");
			Files.write(deploymentFile, deploymentContent.getBytes(), StandardOpenOption.WRITE);
			return deploymentFile;
		}

		static ManifestFiles createManifests() {
			Path manifestsPath = createTempDirectory();
			Path deploymentFile = createDeploymentFile(manifestsPath);
			Path routerFile = createRouterFile(manifestsPath);
			return new ManifestFiles(manifestsPath, deploymentFile, routerFile);
		}

		static void deleteManifests(ManifestFiles manifestFiles) throws IOException {
			Files.deleteIfExists(manifestFiles.deploymentFile);
			Files.deleteIfExists(manifestFiles.routerFile);
			Files.deleteIfExists(manifestFiles.manifestsPath);
		}

		record ManifestFiles(Path manifestsPath, Path deploymentFile, Path routerFile) {
		}

	}

}
