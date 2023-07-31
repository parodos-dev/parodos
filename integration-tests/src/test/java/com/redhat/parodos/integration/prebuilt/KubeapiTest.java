package com.redhat.parodos.integration.prebuilt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import com.redhat.parodos.flows.common.WorkFlowTestBuilder;
import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.ArgumentRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowStatusResponseDTO;
import com.redhat.parodos.sdk.model.WorkRequestDTO;
import com.redhat.parodos.sdkutils.WorkFlowServiceUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KubeapiTest {

	private final String workflowName = "kubeapiWorkFlow";

	private final String taskName = "kubeapiTask";

	@Test
	public void runFlow() throws IOException, ApiException {
		WorkFlowTestBuilder.TestComponents components = new WorkFlowTestBuilder().withDefaultProject()
				.withWorkFlowDefinition(workflowName).build();

		// EXECUTE WORKFLOW
		WorkflowApi workflowApi = new WorkflowApi(components.apiClient());

		// 1 - Define WorkRequests
		String kubeconfigJson = Files.readString(new File(System.getenv("KUBECONFIG_JSON")).toPath());

		WorkRequestDTO workCreate = new WorkRequestDTO().workName(taskName).arguments(Arrays.asList(
				new ArgumentRequestDTO().key("kubeconfig-json").value(kubeconfigJson),
				new ArgumentRequestDTO().key("api-group").value(""),
				new ArgumentRequestDTO().key("api-version").value("v1"),
				new ArgumentRequestDTO().key("kind-plural-name").value("configmaps"),
				new ArgumentRequestDTO().key("operation").value("create"),
				new ArgumentRequestDTO().key("resource-json").value("{"
						+ "\"apiVersion\": \"v1\",\"data\": {\"a\": \"vala\",\"b\": \"valb\"},\"kind\": \"ConfigMap\","
						+ "\"metadata\": {\"name\": \"kubeapi-test-cm\",\"namespace\": \"test\"}}")));

		WorkRequestDTO workGet = new WorkRequestDTO().workName(taskName)
				.arguments(Arrays.asList(new ArgumentRequestDTO().key("kubeconfig-json").value(kubeconfigJson),
						new ArgumentRequestDTO().key("api-group").value(""),
						new ArgumentRequestDTO().key("api-version").value("v1"),
						new ArgumentRequestDTO().key("kind-plural-name").value("configmaps"),
						new ArgumentRequestDTO().key("operation").value("get"),
						new ArgumentRequestDTO().key("resource-name").value("kubeapi-test-cm"),
						new ArgumentRequestDTO().key("resource-namespace").value("test"),
						new ArgumentRequestDTO().key("work-ctx-key").value("theKey")));

		WorkRequestDTO workUpdate = new WorkRequestDTO().workName(taskName).arguments(Arrays.asList(
				new ArgumentRequestDTO().key("kubeconfig-json").value(kubeconfigJson),
				new ArgumentRequestDTO().key("api-group").value(""),
				new ArgumentRequestDTO().key("api-version").value("v1"),
				new ArgumentRequestDTO().key("kind-plural-name").value("configmaps"),
				new ArgumentRequestDTO().key("operation").value("update"),
				new ArgumentRequestDTO().key("resource-json").value("{"
						+ "\"apiVersion\": \"v1\",\"data\": {\"a\": \"valb\",\"b\": \"vala\"},\"kind\": \"ConfigMap\","
						+ "\"metadata\": {\"name\": \"kubeapi-test-cm\",\"namespace\": \"test\"}}")));

		// 2 - Define WorkFlowRequests
		WorkFlowRequestDTO workFlowRequestGet = new WorkFlowRequestDTO().projectId(components.project().getId())
				.workFlowName(workflowName).works(Arrays.asList(workCreate));
		WorkFlowRequestDTO workFlowRequestCreate = new WorkFlowRequestDTO().projectId(components.project().getId())
				.workFlowName(workflowName).works(Arrays.asList(workGet));
		WorkFlowRequestDTO workFlowRequestUpdate = new WorkFlowRequestDTO().projectId(components.project().getId())
				.workFlowName(workflowName).works(Arrays.asList(workUpdate));

		WorkFlowRequestDTO workFlowRequests[] = new WorkFlowRequestDTO[] { workFlowRequestGet, workFlowRequestCreate,
				workFlowRequestUpdate };

		// 3 - Execute WorkFlowRequests
		for (WorkFlowRequestDTO workFlowRequest : workFlowRequests) {
			WorkFlowExecutionResponseDTO workFlowResponseDTO = workflowApi.execute(workFlowRequest);

			assertNotNull(workFlowResponseDTO.getWorkFlowExecutionId());
			assertNotNull(workFlowResponseDTO.getWorkStatus());
			assertEquals(WorkFlowExecutionResponseDTO.WorkStatusEnum.IN_PROGRESS, workFlowResponseDTO.getWorkStatus());

			WorkFlowStatusResponseDTO workFlowStatusResponseDTO = WorkFlowServiceUtils
					.waitWorkflowStatusAsync(workflowApi, workFlowResponseDTO.getWorkFlowExecutionId());

			assertNotNull(workFlowStatusResponseDTO);
			assertNotNull(workFlowStatusResponseDTO.getWorkFlowExecutionId());
			assertNotNull(workFlowStatusResponseDTO.getStatus());
			assertEquals(WorkFlowStatusResponseDTO.StatusEnum.COMPLETED, workFlowStatusResponseDTO.getStatus());
		}
	}

}
