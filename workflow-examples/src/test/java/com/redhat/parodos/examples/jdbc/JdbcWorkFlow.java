package com.redhat.parodos.examples.jdbc;

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

import static com.redhat.parodos.sdkutils.SdkUtils.getProjectAsync;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/*
 * JDBC example with MySQL
 * After installing a MySQL server it listens on port 3306 for non-secure/non-encrypted connections
 * We show a simple SQL SELECT from the server's predefined tables.
 * You need to create a user 'test' with password 'test'. Use the following commands in mysql prompt:
 *   create user 'test'@'localhost' identified by 'test'
 *   grant all privileges on *.* to 'test'@'localhost'
 * You will also need to add the MySQL JDBC driver as a Maven dependency
 */
public class JdbcWorkFlow {

	private final String projectName = "project-1";

	private final String projectDescription = "Jdbc example project";

	private final String workflowName = "jdbcWorkFlow";

	private final String taskName = "jdbcTask";

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
			WorkRequestDTO workCreateDB = new WorkRequestDTO().workName(taskName)
					.arguments(Arrays.asList(
							new ArgumentRequestDTO().key("url")
									.value("jdbc:mysql://localhost:3306/?user=test&password=test"),
							new ArgumentRequestDTO().key("operation").value("execute"),
							new ArgumentRequestDTO().key("statement").value("create database service")));
			WorkRequestDTO workCreateTable = new WorkRequestDTO().workName(taskName)
					.arguments(Arrays.asList(
							new ArgumentRequestDTO().key("url")
									.value("jdbc:mysql://localhost:3306/service?user=test&password=test"),
							new ArgumentRequestDTO().key("operation").value("execute"),
							new ArgumentRequestDTO().key("statement").value("create table items (id VARCHAR(255))")));
			WorkRequestDTO workInsert = new WorkRequestDTO().workName(taskName)
					.arguments(Arrays.asList(
							new ArgumentRequestDTO().key("url")
									.value("jdbc:mysql://localhost:3306/service?user=test&password=test"),
							new ArgumentRequestDTO().key("operation").value("update"),
							new ArgumentRequestDTO().key("statement").value("insert into items values('item-test')")));
			WorkRequestDTO workSelect = new WorkRequestDTO().workName(taskName)
					.arguments(Arrays.asList(
							new ArgumentRequestDTO().key("url")
									.value("jdbc:mysql://localhost:3306/service?user=test&password=test"),
							new ArgumentRequestDTO().key("operation").value("query"),
							new ArgumentRequestDTO().key("statement").value("select * from items"),
							new ArgumentRequestDTO().key("result-ctx-key").value("theresult")));

			WorkFlowRequestDTO workFlowRequestDb = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workCreateDB));
			WorkFlowRequestDTO workFlowRequestTable = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workCreateTable));
			WorkFlowRequestDTO workFlowRequestInsert = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workInsert));
			WorkFlowRequestDTO workFlowRequestSelect = new WorkFlowRequestDTO().projectId(testProject.getId())
					.workFlowName(workflowName).works(Arrays.asList(workSelect));

			WorkFlowRequestDTO workFlowRequests[] = new WorkFlowRequestDTO[] { workFlowRequestDb, workFlowRequestTable,
					workFlowRequestInsert, workFlowRequestSelect };

			// 3 - Execute WorkFlowRequests
			for (WorkFlowRequestDTO workFlowRequest : workFlowRequests) {
				WorkFlowExecutionResponseDTO execute = workflowApi.execute(workFlowRequest);

				assertNotNull(execute.getWorkFlowExecutionId());
			}
		}
		catch (Exception e) {
			fail("Execution of jdbc workflow failed with error: " + e.getMessage());
		}
	}

}
