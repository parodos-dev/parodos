/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.examples.ocponboarding.task;

import java.util.Collections;

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
import io.fabric8.openshift.api.model.TLSConfigBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OcpAppDeploymentWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private static final String OPENSHIFT_NGINX = "twalter/openshift-nginx";

	private static final int CONTAINER_PORT = 8081;

	private static final String DEMO_PORT = "demo-port";

	private static final String APP_LINK = "APP_LINK";

	private static final String ROUTE_PROTOCOl = "http://";

	private static final String NGINX = "nginx";

	private static final String NAMESPACE = "NAMESPACE";

	private static final String CLUSTER_TOKEN = "CLUSTER_TOKEN";

	private final String clusterApiUrl;

	public OcpAppDeploymentWorkFlowTask(String clusterApiUrl) {
		this.clusterApiUrl = clusterApiUrl;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start ocpAppDeploymentWorkFlowTask...");
		try {
			String namespace = getRequiredParameterValue(NAMESPACE);
			String clusterToken = getRequiredParameterValue(CLUSTER_TOKEN);

			Config config = new ConfigBuilder().withMasterUrl(clusterApiUrl).withOauthToken(clusterToken).build();

			try (KubernetesClient kclient = new KubernetesClientBuilder().withConfig(config).build()) {
				OpenShiftClient client = kclient.adapt(OpenShiftClient.class);

				// Project
				createProject(client, namespace);

				// Deployment
				Deployment deployment = new DeploymentBuilder().withNewMetadata().withName(NGINX).endMetadata()
						.withNewSpec().withReplicas(1).withNewTemplate().withNewMetadata().addToLabels("app", NGINX)
						.endMetadata().withNewSpec().addNewContainer().withName(NGINX).withImage(OPENSHIFT_NGINX)
						.addNewPort().withContainerPort(CONTAINER_PORT).endPort().endContainer().endSpec().endTemplate()
						.withNewSelector().addToMatchLabels("app", NGINX).endSelector().endSpec().build();
				client.apps().deployments().inNamespace(namespace).resource(deployment).create();

				// Service
				Service service = new ServiceBuilder().withNewMetadata().withName(NGINX).endMetadata().withNewSpec()
						.withSelector(Collections.singletonMap("app", NGINX)).addNewPort().withName(DEMO_PORT)
						.withProtocol("TCP").withPort(CONTAINER_PORT).withTargetPort(new IntOrString(CONTAINER_PORT))
						.endPort().endSpec().build();
				client.services().inNamespace(namespace).resource(service).create();

				// Route
				Route route = new RouteBuilder().withNewMetadata().withName(NGINX).endMetadata().withNewSpec()
						.withTo(new RouteTargetReferenceBuilder().withKind("Service").withName(NGINX).build())
						.withTls(new TLSConfigBuilder().withTermination("edge").build())
						.withPort(new RoutePortBuilder().withTargetPort(new IntOrString(CONTAINER_PORT)).build())
						.endSpec().build();
				route = client.routes().inNamespace(namespace).resource(route).create();

				addParameter(APP_LINK, String.format("%s%s", ROUTE_PROTOCOl, route.getSpec().getHost()));

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
			log.info("creating project {} is failed: {}, {}", namespace, e.getClass().getSimpleName(), e.getMessage());
		}
	}

}
