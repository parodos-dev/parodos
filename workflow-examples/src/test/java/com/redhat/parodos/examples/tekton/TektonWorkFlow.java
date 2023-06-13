package com.redhat.parodos.examples.tekton;

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
import com.redhat.parodos.sdk.model.WorkFlowExecutionResponseDTO;
import com.redhat.parodos.sdk.model.WorkFlowRequestDTO;
import com.redhat.parodos.sdk.model.WorkRequestDTO;
import com.redhat.parodos.workflow.utils.CredUtils;
import org.junit.Test;

import org.springframework.http.HttpHeaders;

import static com.redhat.parodos.sdkutils.WorkFlowServiceUtils.getProjectAsync;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TektonWorkFlow {

	private final String projectName = "project-1";

	private final String projectDescription = "Tekton example project";

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

			String namespaceJson = """
					{
					  "apiVersion": "v1",
					  "kind": "Namespace",
					  "metadata": {
						"name": "tekton-example"
					  }
					}""";

			String task1Json = """
					{
					  "apiVersion": "tekton.dev/v1",
					  "kind": "Task",
					  "metadata": {
						"name": "task-1",
						"namespace": "tekton-example"
					  },
					  "spec": {
						"steps": [
						  {
							"image": "busybox",
							"name": "echo",
							"script": "#!/bin/sh\\necho \\\"Hi I am task #1\\\""
						  }
						]
					  }
					}""";

			String task2Json = """
					{
					  "apiVersion": "tekton.dev/v1",
					  "kind": "Task",
					  "metadata": {
						"name": "task-2",
						"namespace": "tekton-example"
					  },
					  "spec": {
						"steps": [
						  {
							"image": "busybox",
							"name": "echo",
							"script": "#!/bin/sh\\necho \\\"Hi I am task #2\\\""
						  }
						]
					  }
					}""";

			String pipelineJson = """
					{
					  "apiVersion": "tekton.dev/v1",
					  "kind": "Pipeline",
					  "metadata": {
						"name": "pipeline-1",
						"namespace": "tekton-example"
					  },
					  "spec": {
						"tasks": [
						  {
							"name": "task-1",
							"taskRef": {
							  "kind": "Task",
							  "name": "task-1"
							}
						  },
						  {
							"name": "task-2",
							"runAfter": [
							  "task-1"
							],
							"taskRef": {
							  "kind": "Task",
							  "name": "task-2"
							}
						  }
						]
					  }
					}""";

			String pipelineRunJson = """
					{
					  "apiVersion": "tekton.dev/v1",
					  "kind": "PipelineRun",
					  "metadata": {
						"name": "pipeline-1","namespace": "tekton-example"
					  },
					  "spec": {
						"pipelineRef": {
						"name": "pipeline-1"
						}
					  }
					}""";

			WorkRequestDTO workNamespace = new WorkRequestDTO().workName(taskName)
					.arguments(Arrays.asList(new ArgumentRequestDTO().key("kubeconfig-json").value(kubeconfigJson),
							new ArgumentRequestDTO().key("api-group").value(""),
							new ArgumentRequestDTO().key("api-version").value("v1"),
							new ArgumentRequestDTO().key("kind-plural-name").value("namespaces"),
							new ArgumentRequestDTO().key("operation").value("create"),
							new ArgumentRequestDTO().key("resource-json").value(namespaceJson)));

			WorkRequestDTO workTask1 = new WorkRequestDTO().workName(taskName)
					.arguments(Arrays.asList(new ArgumentRequestDTO().key("kubeconfig-json").value(kubeconfigJson),
							new ArgumentRequestDTO().key("api-group").value("tekton.dev"),
							new ArgumentRequestDTO().key("api-version").value("v1"),
							new ArgumentRequestDTO().key("kind-plural-name").value("tasks"),
							new ArgumentRequestDTO().key("operation").value("create"),
							new ArgumentRequestDTO().key("resource-json").value(task1Json)));

			WorkRequestDTO workTask2 = new WorkRequestDTO().workName(taskName)
					.arguments(Arrays.asList(new ArgumentRequestDTO().key("kubeconfig-json").value(kubeconfigJson),
							new ArgumentRequestDTO().key("api-group").value("tekton.dev"),
							new ArgumentRequestDTO().key("api-version").value("v1"),
							new ArgumentRequestDTO().key("kind-plural-name").value("tasks"),
							new ArgumentRequestDTO().key("operation").value("create"),
							new ArgumentRequestDTO().key("resource-json").value(task2Json)));

			WorkRequestDTO workPipeline = new WorkRequestDTO().workName(taskName)
					.arguments(Arrays.asList(new ArgumentRequestDTO().key("kubeconfig-json").value(kubeconfigJson),
							new ArgumentRequestDTO().key("api-group").value("tekton.dev"),
							new ArgumentRequestDTO().key("api-version").value("v1"),
							new ArgumentRequestDTO().key("kind-plural-name").value("pipelines"),
							new ArgumentRequestDTO().key("operation").value("create"),
							new ArgumentRequestDTO().key("resource-json").value(pipelineJson)));

			WorkRequestDTO workPipelineRun = new WorkRequestDTO().workName(taskName)
					.arguments(Arrays.asList(new ArgumentRequestDTO().key("kubeconfig-json").value(kubeconfigJson),
							new ArgumentRequestDTO().key("api-group").value("tekton.dev"),
							new ArgumentRequestDTO().key("api-version").value("v1"),
							new ArgumentRequestDTO().key("kind-plural-name").value("pipelineruns"),
							new ArgumentRequestDTO().key("operation").value("create"),
							new ArgumentRequestDTO().key("resource-json").value(pipelineRunJson)));

			// 2 - Define WorkFlowRequests
			WorkFlowRequestDTO workFlowRequestNamespace = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workNamespace));
			WorkFlowRequestDTO workFlowRequestTask1 = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workTask1));
			WorkFlowRequestDTO workFlowRequestTask2 = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workTask2));
			WorkFlowRequestDTO workFlowRequestPipeline = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workPipeline));
			WorkFlowRequestDTO workFlowRequestPipelinerun = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workPipelineRun));

			WorkFlowRequestDTO workFlowRequests[] = new WorkFlowRequestDTO[] { workFlowRequestNamespace,
					workFlowRequestTask1, workFlowRequestTask2, workFlowRequestPipeline, workFlowRequestPipelinerun };

			// 3 - Execute WorkFlowRequests
			for (WorkFlowRequestDTO workFlowRequest : workFlowRequests) {
				WorkFlowExecutionResponseDTO execute = workflowApi.execute(workFlowRequest);

				assertNotNull(execute.getWorkFlowExecutionId());
			}
		}
		catch (Exception e) {
			fail("Execution of tekton workflow failed with error: " + e.getMessage());
		}
	}

}
