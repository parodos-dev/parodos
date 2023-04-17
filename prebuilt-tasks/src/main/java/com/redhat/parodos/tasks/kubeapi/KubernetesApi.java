package com.redhat.parodos.tasks.kubeapi;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;

import java.io.IOException;

interface KubernetesApi {

	DynamicKubernetesObject get(String kubeconfig, String apiGroup, String apiVersion, String kindPluralName,
			String namespace, String name) throws ApiException, IOException;

	void create(String kubeconfig, String apiGroup, String apiVersion, String kindPluralName,
			DynamicKubernetesObject obj) throws ApiException, IOException;

	void update(String kubeconfig, String apiGroup, String apiVersion, String kindPluralName,
			DynamicKubernetesObject obj) throws ApiException, IOException;

}
