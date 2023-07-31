package com.redhat.parodos.tasks.kubeapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;
import io.kubernetes.client.util.generic.dynamic.Dynamics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class KubeapiWorkFlowTaskTest {

	private final static KubernetesApi api = mock(KubernetesApi.class);

	private final static KubeapiWorkFlowTask task = new KubeapiWorkFlowTask(api, "Test");

	@Test
	public void missingArgs() {
		WorkContext ctx = createWorkContext(new HashMap<>());
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		task.preExecute(ctx);
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(MissingParameterException.class, result.getError().getClass());
	}

	@Test
	public void invalidOperationType() {
		HashMap<String, String> map = new HashMap<>();
		map.put("operation", "test");
		WorkContext ctx = createWorkContext(map);
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		task.preExecute(ctx);
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.FAILED, result.getStatus());
		assertEquals(IllegalArgumentException.class, result.getError().getClass());
	}

	@Test
	public void get() throws ApiException, IOException {
		String objJson = "{\"kind\":\"ConfigMap\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"my-cm\",\"namespace\":"
				+ "\"test\",\"uid\":\"5f28eebc-05b2-41b0-9da1-e7399adc1262\",\"resourceVersion\":\"163811\","
				+ "\"creationTimestamp\":\"2023-04-16T18:47:25Z\"},\"data\":{\"a\":\"vala\",\"b\":\"valb\"}}";
		DynamicKubernetesObject obj = Dynamics.newFromJson(objJson);
		WorkContext ctx = workContextGET();
		doReturn(obj).when(api).get(any(), any(), any(), any(), any(), any());
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		task.preExecute(ctx);
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		assertEquals(objJson, ctx.get("testkey"));
	}

	@Test
	public void create() throws ApiException, IOException {
		DynamicKubernetesObject expectedObj = Dynamics.newFromJson(
				"{\"apiVersion\": \"v1\",\"data\": {\"a\": \"vala\",\"b\": \"valb\"},\"kind\": \"ConfigMap\",\"metadata\": {\"name\": \"my-cm-create\",\"namespace\": \"test\"}}");
		WorkContext ctx = workContextCREATE();
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		task.preExecute(ctx);
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		verify(api, times(1)).create(any(), any(), any(), any(), eq(expectedObj));
	}

	@Test
	public void update() throws ApiException, IOException {
		DynamicKubernetesObject expectedObj = Dynamics.newFromJson(
				"{\"apiVersion\": \"v1\",\"data\": {\"a\": \"valb\",\"b\": \"vala\"},\"kind\": \"ConfigMap\",\"metadata\": {\"resourceVersion\": \"1000\", \"name\": \"my-cm-create\",\"namespace\": \"test\"}}");
		DynamicKubernetesObject getReturnValue = Dynamics.newFromJson(
				"{\"apiVersion\": \"v1\",\"data\": {\"a\": \"vala\",\"b\": \"valb\"},\"kind\": \"ConfigMap\",\"metadata\": {\"resourceVersion\": \"1000\", \"name\": \"my-cm-create\",\"namespace\": \"test\"}}");
		WorkContext ctx = workContextUPDATE();
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		task.preExecute(ctx);
		doReturn(getReturnValue).when(api).get(any(), any(), any(), any(), any(), any());
		WorkReport result = task.execute(ctx);
		assertEquals(WorkStatus.COMPLETED, result.getStatus());
		verify(api, times(1)).update(any(), any(), any(), any(), eq(expectedObj));
	}

	private WorkContext workContextGET() {
		HashMap<String, String> map = new HashMap<>();
		map.put("operation", "get");
		map.put("resource-name", "testname");
		map.put("resource-namespace", "testnamespace");
		map.put("work-ctx-key", "testkey");
		return createWorkContext(map);
	}

	private WorkContext workContextCREATE() {
		HashMap<String, String> map = new HashMap<>();
		map.put("operation", "create");
		map.put("resource-json",
				"{\"apiVersion\": \"v1\",\"data\": {\"a\": \"vala\",\"b\": \"valb\"},\"kind\": \"ConfigMap\",\"metadata\": {\"name\": \"my-cm-create\",\"namespace\": \"test\"}}");
		return createWorkContext(map);
	}

	private WorkContext workContextUPDATE() {
		HashMap<String, String> map = new HashMap<>();
		map.put("operation", "update");
		map.put("resource-json",
				"{\"apiVersion\": \"v1\",\"data\": {\"a\": \"valb\",\"b\": \"vala\"},\"kind\": \"ConfigMap\",\"metadata\": {\"name\": \"my-cm-create\",\"namespace\": \"test\"}}");
		return createWorkContext(map);
	}

	private WorkContext createWorkContext(HashMap<String, String> map) {
		WorkContext ctx = new WorkContext();
		map.put("kubeconfig-json", "");
		map.put("api-group", "");
		map.put("api-version", "v1");
		map.put("kind-plural-name", "configmaps");

		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, task.getName(),
				WorkContextDelegate.Resource.ARGUMENTS, map);
		return ctx;
	}

}
