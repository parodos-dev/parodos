package com.redhat.parodos.examples.ocponboarding.task;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.ProjectRequestBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.api.model.RoutePortBuilder;
import io.fabric8.openshift.api.model.RouteTargetReferenceBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Slf4j
public class OcpAppDeploymentWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private static final String OPENSHIFT_NGINX = "twalter/openshift-nginx";

	private static final int CONTAINER_PORT = 8081;

	private static final String DEMO_PORT = "demo-port";

	private static final String APP_LINK = "APP_LINK";

	private static final String NGINX = "nginx";

	private static final String NAMESPACE = "NAMESPACE";

	private static final String CLUSTER_TOKEN = "CLUSTER_TOKEN";

	private final String clusterApiUrl;

	public OcpAppDeploymentWorkFlowTask(String clusterApiUrl) {
		this.clusterApiUrl = clusterApiUrl;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		try {
			// String namespace = getRequiredParameterValue(workContext, NAMESPACE);
			String namespace = getOptionalParameterValue(workContext, NAMESPACE, "demo");
			String clusterToken = getRequiredParameterValue(workContext, CLUSTER_TOKEN);
			Config config = new ConfigBuilder().withMasterUrl(clusterApiUrl).withOauthToken(clusterToken).build();
			try (KubernetesClient kclient = new KubernetesClientBuilder().withConfig(config).build()) {
				OpenShiftClient client = kclient.adapt(OpenShiftClient.class);
				createProject(client, namespace);

				Deployment deployment = new DeploymentBuilder().withNewMetadata().withName(NGINX).endMetadata()
						.withNewSpec().withReplicas(1).withNewTemplate().withNewMetadata().addToLabels("app", NGINX)
						.endMetadata().withNewSpec().addNewContainer().withName(NGINX)
						.withImage(OPENSHIFT_NGINX).addNewPort().withContainerPort(CONTAINER_PORT).endPort()
						.endContainer().endSpec().endTemplate().withNewSelector().addToMatchLabels("app", NGINX)
						.endSelector().endSpec().build();

				deployment = client.apps().deployments().inNamespace(namespace).resource(deployment).create();

				Service service = new ServiceBuilder().withNewMetadata().withName(NGINX).endMetadata().withNewSpec()
						.withSelector(Collections.singletonMap("app", NGINX)).addNewPort().withName(DEMO_PORT)
						.withProtocol("TCP").withPort(CONTAINER_PORT).withTargetPort(new IntOrString(CONTAINER_PORT)).endPort().endSpec()
						.build();

				service = client.services().inNamespace(namespace).resource(service).create();

				Route route = new RouteBuilder().withNewMetadata().withName(NGINX).endMetadata().withNewSpec()
						.withTo(new RouteTargetReferenceBuilder().withKind("Service").withName(NGINX).build())
						.withPort(new RoutePortBuilder().withTargetPort(new IntOrString(CONTAINER_PORT)).build()).endSpec()
						.build();

				route = client.routes().inNamespace(namespace).resource(route).create();
				addParameter(workContext, APP_LINK, route.getSpec().getHost());
				log.info("deployment is successful");
			}
		}
		catch (MissingParameterException e) {
			log.error("can't get namespace or cluster token");
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}
		catch (KubernetesClientException e) {
			log.error("deploy failed. error message: {}", e.getMessage());
			return new DefaultWorkReport(WorkStatus.FAILED, workContext);
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private void createProject(OpenShiftClient client, String namespace) {
		try {
			client.projectrequests().create(new ProjectRequestBuilder().withNewMetadata().withName(namespace)
					.endMetadata().withDescription(namespace).withDisplayName(namespace).build());
		}
		catch (KubernetesClientException e) {
			log.info("project {} already exists: {}", namespace, e.getClass().getSimpleName());
		}
	}

}
