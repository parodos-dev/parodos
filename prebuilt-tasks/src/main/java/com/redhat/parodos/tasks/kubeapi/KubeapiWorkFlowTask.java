package com.redhat.parodos.tasks.kubeapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;
import io.kubernetes.client.util.generic.dynamic.Dynamics;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * KubeapiWorkFlowTask A task for get/create/update Kubernetes resource User provides
 * kubeconfig and then each execution performs the action See 'getWorkFlowTaskParameters'
 * for detailed parameters list
 */
@Slf4j
public class KubeapiWorkFlowTask extends BaseWorkFlowTask {

	static public enum OperationType {

		GET, CREATE, UPDATE

	};

	private KubernetesApi api;

	public KubeapiWorkFlowTask() {
		this.api = new KubernetesApiImpl();
	}

	KubeapiWorkFlowTask(KubernetesApi api, String beanName) {
		this.api = api;
		this.setBeanName(beanName);
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		LinkedList<WorkParameter> params = new LinkedList<>();
		params.add(WorkParameter.builder().key("kubeconfig-json").type(WorkParameterType.TEXT).optional(false)
				.description("kubeconfig in json format").build());
		params.add(WorkParameter.builder().key("api-group").type(WorkParameterType.TEXT).optional(false)
				.description("API group of resource").build());
		params.add(WorkParameter.builder().key("api-version").type(WorkParameterType.TEXT).optional(false)
				.description("API version of resource").build());
		params.add(WorkParameter.builder().key("kind-plural-name").type(WorkParameterType.TEXT).optional(false)
				.description("Plural name of the resource kind. E.g. crontabs").build());
		params.add(WorkParameter.builder().key("operation").type(WorkParameterType.TEXT).optional(false)
				.description("Operation type/name. Can be one of " + Arrays.toString(OperationType.values())).build());
		params.add(WorkParameter.builder().key("resource-json").type(WorkParameterType.TEXT).optional(true)
				.description("The JSON to be used in create and update operations").build());
		params.add(WorkParameter.builder().key("resource-name").type(WorkParameterType.TEXT).optional(true)
				.description("Name of resource for get operation").build());
		params.add(WorkParameter.builder().key("resource-namespace").type(WorkParameterType.TEXT).optional(true)
				.description("Namespace of resource for get operation").build());
		params.add(WorkParameter.builder().key("work-ctx-key").type(WorkParameterType.TEXT).optional(true)
				.description("In get operation the result is stored in WorkContext with the provided key").build());
		return params;
	}

	@Override
	public @NonNull List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.OTHER);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		String operation = "";

		try {
			// Get the required parameters
			String kubeconfigJson = getRequiredParameterValue(workContext, "kubeconfig-json");
			String apiGroup = getRequiredParameterValue(workContext, "api-group");
			String apiVersion = getRequiredParameterValue(workContext, "api-version");
			String kindPluralName = getRequiredParameterValue(workContext, "kind-plural-name");
			operation = getRequiredParameterValue(workContext, "operation");

			String kubeconfig = new YAMLMapper().writeValueAsString(new ObjectMapper().readTree(kubeconfigJson));
			OperationType operationType = OperationType.valueOf(operation.toUpperCase());

			switch (operationType) {
				case UPDATE:
					update(workContext, kubeconfig, apiGroup, apiVersion, kindPluralName);
					break;
				case CREATE:
					create(workContext, kubeconfig, apiGroup, apiVersion, kindPluralName);
					break;
				case GET:
					get(workContext, kubeconfig, apiGroup, apiVersion, kindPluralName);
					break;
			}
		}
		catch (MissingParameterException | ApiException | IllegalArgumentException | IOException e) {
			log.error("Kubeapi task failed for operation " + operation, e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private void get(WorkContext ctx, String kubeconfig, String apiGroup, String apiVersion, String kindPluralName)
			throws MissingParameterException, ApiException, IOException {
		String resourceName = getRequiredParameterValue(ctx, "resource-name");
		String resourceNamespace = getRequiredParameterValue(ctx, "resource-namespace");
		String workCtxKey = getRequiredParameterValue(ctx, "work-ctx-key");

		DynamicKubernetesObject obj = api.get(kubeconfig, apiGroup, apiVersion, kindPluralName, resourceNamespace,
				resourceName);
		String resourceJson = obj.getRaw().toString();
		ctx.put(workCtxKey, resourceJson);
	}

	private void create(WorkContext ctx, String kubeconfig, String apiGroup, String apiVersion, String kindPluralName)
			throws MissingParameterException, ApiException, IOException {
		String resourceJson = getRequiredParameterValue(ctx, "resource-json");
		DynamicKubernetesObject obj = Dynamics.newFromJson(resourceJson);
		api.create(kubeconfig, apiGroup, apiVersion, kindPluralName, obj);
	}

	private void update(WorkContext ctx, String kubeconfig, String apiGroup, String apiVersion, String kindPluralName)
			throws MissingParameterException, ApiException, IOException {
		String resourceJson = getRequiredParameterValue(ctx, "resource-json");
		DynamicKubernetesObject newObj = Dynamics.newFromJson(resourceJson);
		String resourceNamespace = newObj.getMetadata().getNamespace();
		String resourceName = newObj.getMetadata().getName();
		DynamicKubernetesObject currObj = api.get(kubeconfig, apiGroup, apiVersion, kindPluralName, resourceNamespace,
				resourceName);
		V1ObjectMeta metadata = newObj.getMetadata();
		metadata.setResourceVersion(currObj.getMetadata().getResourceVersion());
		newObj.setMetadata(metadata);
		api.update(kubeconfig, apiGroup, apiVersion, kindPluralName, newObj);
	}

}
