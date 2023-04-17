package com.redhat.parodos.tasks.kubeapi;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;

import java.io.IOException;
import java.io.StringReader;

class KubernetesApiImpl implements KubernetesApi {

	@Override
	public DynamicKubernetesObject get(String kubeconfig, String apiGroup, String apiVersion, String kindPluralName,
			String namespace, String name) throws ApiException, IOException {
		ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new StringReader(kubeconfig))).build();
		return new DynamicKubernetesApi(apiGroup, apiVersion, kindPluralName, client).get(namespace, name)
				.throwsApiException().getObject();
	}

	@Override
	public void create(String kubeconfig, String apiGroup, String apiVersion, String kindPluralName,
			DynamicKubernetesObject obj) throws ApiException, IOException {
		ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new StringReader(kubeconfig))).build();
		new DynamicKubernetesApi(apiGroup, apiVersion, kindPluralName, client).create(obj).throwsApiException();
	}

	@Override
	public void update(String kubeconfig, String apiGroup, String apiVersion, String kindPluralName,
			DynamicKubernetesObject obj) throws ApiException, IOException {
		ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new StringReader(kubeconfig))).build();
		new DynamicKubernetesApi(apiGroup, apiVersion, kindPluralName, client).update(obj).throwsApiException();
	}

}
