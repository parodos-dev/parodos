package com.redhat.parodos.examples.kubeapi;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import com.redhat.parodos.sdk.api.WorkflowApi;
import com.redhat.parodos.sdk.api.WorkflowDefinitionApi;
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.model.ArgumentRequestDTO;
import com.redhat.parodos.sdk.model.ProjectResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkFlowResponseDTO;
import com.redhat.parodos.sdk.model.WorkRequestDTO;
import com.redhat.parodos.workflow.utils.CredUtils;
import org.junit.Test;

import org.springframework.http.HttpHeaders;

import static com.redhat.parodos.sdkutils.SdkUtils.getProjectAsync;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class KubeapiWorkFlow {

	private final String projectName = "project-1";

	private final String projectDescription = "Kubeapi example project";

	private final String workflowName = "kubeapiWorkFlow";

	private final String taskName = "kubeapiTask";

	@Test
	public void runFlow() {
		ApiClient defaultClient = Configuration.getDefaultApiClient();

		defaultClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + CredUtils.getBase64Creds("test", "test"));

		try {
			ProjectResponseDTO testProject = getProjectAsync(defaultClient, projectName, projectDescription);

			// GET workflow DEFINITIONS
			WorkflowDefinitionApi workflowDefinitionApi = new WorkflowDefinitionApi(defaultClient);
			List<WorkFlowDefinitionResponseDTO> simpleSequentialWorkFlowDefinitions = workflowDefinitionApi
					.getWorkFlowDefinitions(workflowName);
			assertEquals(1, simpleSequentialWorkFlowDefinitions.size());

			// GET WORKFLOW DEFINITION BY Id
			WorkFlowDefinitionResponseDTO simpleSequentialWorkFlowDefinition = workflowDefinitionApi
					.getWorkFlowDefinitionById(simpleSequentialWorkFlowDefinitions.get(0).getId());

			// EXECUTE WORKFLOW
			WorkflowApi workflowApi = new WorkflowApi();

			// 1 - Define WorkRequests
			String kubeconfigJson = Files.readString(new File("kubeconfig.json").toPath());

			WorkRequestDTO workGet = new WorkRequestDTO().workName(taskName)
					.arguments(Arrays.asList(new ArgumentRequestDTO().key("kubeconfig-json").value(kubeconfigJson),
							new ArgumentRequestDTO().key("api-group").value(""),
							new ArgumentRequestDTO().key("api-version").value("v1"),
							new ArgumentRequestDTO().key("kind-plural-name").value("configmaps"),
							new ArgumentRequestDTO().key("operation").value("get"),
							new ArgumentRequestDTO().key("resource-name").value("my-cm"),
							new ArgumentRequestDTO().key("resource-namespace").value("test"),
							new ArgumentRequestDTO().key("work-ctx-key").value("theKey")));

			WorkRequestDTO workCreate = new WorkRequestDTO().workName(taskName).arguments(Arrays.asList(
					new ArgumentRequestDTO().key("kubeconfig-json").value(kubeconfigJson),
					new ArgumentRequestDTO().key("api-group").value(""),
					new ArgumentRequestDTO().key("api-version").value("v1"),
					new ArgumentRequestDTO().key("kind-plural-name").value("configmaps"),
					new ArgumentRequestDTO().key("operation").value("create"),
					new ArgumentRequestDTO().key("resource-json").value("{"
							+ "\"apiVersion\": \"v1\",\"data\": {\"a\": \"vala\",\"b\": \"valb\"},\"kind\": \"ConfigMap\","
							+ "\"metadata\": {\"name\": \"my-cm-create\",\"namespace\": \"test\"}}")));

			WorkRequestDTO workUpdate = new WorkRequestDTO().workName(taskName).arguments(Arrays.asList(
					new ArgumentRequestDTO().key("kubeconfig-json").value(kubeconfigJson),
					new ArgumentRequestDTO().key("api-group").value(""),
					new ArgumentRequestDTO().key("api-version").value("v1"),
					new ArgumentRequestDTO().key("kind-plural-name").value("configmaps"),
					new ArgumentRequestDTO().key("operation").value("update"),
					new ArgumentRequestDTO().key("resource-json").value("{"
							+ "\"apiVersion\": \"v1\",\"data\": {\"a\": \"valb\",\"b\": \"vala\"},\"kind\": \"ConfigMap\","
							+ "\"metadata\": {\"name\": \"my-cm-create\",\"namespace\": \"test\"}}")));

			// 2 - Define WorkFlowRequests
			WorkFlowRequestDTO workFlowRequestGet = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workGet));
			WorkFlowRequestDTO workFlowRequestCreate = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workCreate));
			WorkFlowRequestDTO workFlowRequestUpdate = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workUpdate));

			WorkFlowRequestDTO workFlowRequests[] = new WorkFlowRequestDTO[] { workFlowRequestGet,
					workFlowRequestCreate, workFlowRequestUpdate };

			// 3 - Execute WorkFlowRequests
			for (WorkFlowRequestDTO workFlowRequest : workFlowRequests) {
				WorkFlowResponseDTO execute = workflowApi.execute(workFlowRequest);

				assertNotNull(execute.getWorkFlowExecutionId());
			}
		}
		catch (Exception e) {
			fail("Execution of kubeapi workflow failed with error: " + e.getMessage());
		}
	}

}
