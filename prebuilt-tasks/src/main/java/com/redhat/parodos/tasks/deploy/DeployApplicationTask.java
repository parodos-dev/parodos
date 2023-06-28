package com.redhat.parodos.tasks.deploy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteIngress;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeployApplicationTask extends BaseWorkFlowTask {

	private final OpenShiftClientCreator clientCreator;

	public DeployApplicationTask() {
		this.clientCreator = new DefaultOpenShiftClientCreator();
	}

	// Constructor for testing purposes for mocking openshift client in unit-tests
	public DeployApplicationTask(OpenShiftClientCreator clientCreator) {
		this.clientCreator = clientCreator;
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		// @formatter:off
		return List.of(
				WorkParameter.builder()
					.key(DeployConstants.KUBECONFIG)
					.type(WorkParameterType.TEXT)
					.optional(false)
					.description("kubeconfig file of the target cluster in json format")
					.build(),
				WorkParameter.builder()
					.key(DeployConstants.MANIFESTS_PATH)
					.type(WorkParameterType.TEXT)
					.optional(false)
					.description("The path to the manifests to deploy the application")
					.build(),
				WorkParameter.builder()
					.key(DeployConstants.NAMESPACE)
					.type(WorkParameterType.TEXT)
					.optional(false)
					.description("The namespace in which the application should be deployed")
					.build());
		// @formatter:on
	}

	/**
	 * Executes the task logic, based on the following steps:
	 * <ol>
	 * Verify parameters are present
	 * </ol>
	 * <ol>
	 * Load the manifests
	 * </ol>
	 * <ol>
	 * Deploy the manifests
	 * </ol>
	 * <ol>
	 * Update the context with the installed application routes
	 * </ol>
	 * @param workContext context in which this unit of work is being executed
	 * @return a {@link WorkReport} with the result of the execution
	 */
	@Override
	public WorkReport execute(WorkContext workContext) {
		final Input params;

		// Verify parameters are present
		try {
			params = getTaskParameters(workContext);
		}
		catch (MissingParameterException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		// Load the manifests
		final Set<Path> manifests;
		try {
			manifests = loadManifests(params.manifestsPath);
		}
		catch (IOException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("Failed to read manifest files from path %s with error %s"
							.formatted(params.manifestsPath, e.getMessage()), e));
		}

		if (manifests.isEmpty()) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("No manifest files found in path %s".formatted(params.manifestsPath)));
		}

		// Deploy the manifests
		List<String> hostnames = new ArrayList<>();
		try (OpenShiftClient osClient = createOpenShiftClient(params.kubeconfig)) {
			for (Path manifest : manifests) {
				Optional.of(applyManifest(osClient, params.namespace, manifest)).ifPresent(hostnames::addAll);
			}
		}
		catch (ManifestDeployException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}
		catch (KubernetesClientException e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, new RuntimeException(
					"Failed to create OpenShift client with error %s".formatted(e.getMessage()), e));
		}
		catch (Exception e) {
			return new DefaultWorkReport(WorkStatus.FAILED, workContext,
					new RuntimeException("Failed to deploy manifests with error %s".formatted(e.getMessage()), e));
		}

		workContext.put(DeployConstants.APPLICATION_HOSTNAMES, hostnames);
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private static Set<Path> loadManifests(String manifestsPath) throws IOException {
		final Set<Path> manifests;
		try (Stream<Path> walk = Files.walk(Paths.get(manifestsPath))) {
			manifests = walk.filter(Files::isRegularFile).filter(DeployApplicationTask::hasManifestSuffix)
					.collect(Collectors.toSet());
		}

		return manifests;
	}

	private static boolean hasManifestSuffix(Path file) {
		return file.toString().endsWith(".yaml") || file.toString().endsWith(".yml")
				|| file.toString().endsWith(".json");
	}

	private static Input getTaskParameters(WorkContext workContext) throws MissingParameterException {
		String kubeconfig = WorkContextDelegate.getRequiredValueFromRequestParams(workContext,
				DeployConstants.KUBECONFIG);
		String manifestsPath = WorkContextDelegate.getRequiredValueFromRequestParams(workContext,
				DeployConstants.MANIFESTS_PATH);
		String namespace = WorkContextDelegate.getRequiredValueFromRequestParams(workContext,
				DeployConstants.NAMESPACE);
		return new Input(kubeconfig, manifestsPath, namespace);
	}

	/**
	 * Deploys manifest from a file to a namespace
	 * @param osClient an initialized openshift client of the target cluster
	 * @param namespace the namespace to deploy the manifest on
	 * @param manifest the manifest to deploy
	 * @return returns the routes to the installed application exposed via the Route
	 * resource
	 * @throws ManifestDeployException if the manifest could not be deployed
	 */
	private List<String> applyManifest(OpenShiftClient osClient, String namespace, Path manifest)
			throws ManifestDeployException {
		log.info("Deploying manifest %s".formatted(manifest));
		try (InputStream stream = Files.newInputStream(manifest)) {
			List<HasMetadata> resources = osClient.load(stream).inNamespace(namespace).create();
			log.info("Manifest %s deployed successfully".formatted(manifest));

			// obtain the hostnames from the routes
			return getHostnamesFromRoute(osClient, resources);
		}
		catch (IOException e) {
			throw new ManifestDeployException(
					"Failed to read manifest %s with error: %s".formatted(manifest, e.getMessage()), e);
		}
		catch (KubernetesClientException e) {
			throw new ManifestDeployException(
					"Failed to create manifest %s on cluster with error: %s".formatted(manifest, e.getMessage()), e);
		}
	}

	private List<String> getHostnamesFromRoute(OpenShiftClient osClient, List<HasMetadata> resources)
			throws ManifestDeployException {
		List<String> hostnames = new ArrayList<>();
		for (HasMetadata resource : resources) {
			if ("Route".equals(resource.getKind())) {
				Route route;
				String namespace = resource.getMetadata().getNamespace();
				String name = resource.getMetadata().getName();
				try {
					route = osClient.routes().inNamespace(namespace).withName(name).get();
				}
				catch (KubernetesClientException e) {
					throw new ManifestDeployException(
							"Failed to get route %s/%s with error: %s".formatted(namespace, name, e.getMessage()), e);
				}

				if (route != null) {
					hostnames.addAll(route.getStatus().getIngress().stream().map(RouteIngress::getHost).toList());
				}
				else {
					throw new ManifestDeployException("Route %s/%s not found".formatted(namespace, name), null);
				}
			}
		}

		return hostnames;
	}

	private OpenShiftClient createOpenShiftClient(String kubeConfigString) {
		return this.clientCreator.create(kubeConfigString);
	}

	private record Input(String kubeconfig, String manifestsPath, String namespace) {
	}

	interface OpenShiftClientCreator {

		OpenShiftClient create(String kubeConfigString);

	}

	private static class DefaultOpenShiftClientCreator implements OpenShiftClientCreator {

		@Override
		public OpenShiftClient create(String kubeConfigString) {
			Config config = Config.fromKubeconfig(kubeConfigString);
			return new KubernetesClientBuilder().withConfig(config).build().adapt(OpenShiftClient.class);
		}

	}

}
