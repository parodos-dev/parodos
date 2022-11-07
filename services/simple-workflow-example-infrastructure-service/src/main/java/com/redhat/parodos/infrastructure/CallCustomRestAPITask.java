package com.redhat.parodos.infrastructure;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.redhat.parodos.infrastructure.task.InfrastructureTask;
import com.redhat.parodos.infrastructure.task.InfrastructureTaskAware;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * An example of a task that calls a REST endpoint. It gets all its arguments passed in from the Service using the WorkContext
 * 
 * The InfrastructureTaskEngine will need to put the following arguments into the WorkContext for this task:
 * 
 * TOKEN_PASSED_IN_FROM_SERVICE: The rest API needs a token
 * PAYLOAD_PASSED_IN_FROM_SERVICE: The payload to send
 * URL_PASSED_IN_FROM_SERVICE: The URL of the service
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class CallCustomRestAPITask implements InfrastructureTask, InfrastructureTaskAware {
	
	static public final String PAYLOAD_PASSED_IN_FROM_SERVICE = "PAYLOAD_PASSED_IN_FROM_SERVICE";
	static public final String URL_PASSED_IN_FROM_SERVICE = "URL_PASSED_IN_FROM_SERVICE";
	
	/**
	 * Executed by the InfrastructureTask engine as part of the Workflow
	 * 
	 */
	public WorkReport execute(WorkContext workContext) {
		try {
		String urlString = getValueFromRequestParams(workContext,URL_PASSED_IN_FROM_SERVICE);
		String payload = getValueFromRequestParams(workContext,PAYLOAD_PASSED_IN_FROM_SERVICE);
		log.info("Running Task REST API Call: urlString: {} payload: {} ", urlString, payload );
		RestTemplate restTemplate = new RestTemplate();
	    ResponseEntity<String> result = restTemplate.postForEntity(urlString, payload, String.class);
		if (result.getStatusCode().is2xxSuccessful()) {
			log.info("Rest call completed: {}", result.getBody());
			return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
		}
		log.error("Call to the API was not successful. Response: {}", result.getStatusCode());
		} catch (Exception e) {
			log.error("There was an issue with the REST call: {}", e.getMessage());
			
		}
		return new DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

	@SuppressWarnings("unchecked")
	private String getValueFromRequestParams(WorkContext workContext, String key) throws MissingArguementsException {
		if (((Map<String,String>)workContext.get(INFRASTRUCTURE_TASK_WORKFLOW_DETAILS)).get(key) == null) {
			throw new MissingArguementsException("For this task the WorkContext required key: " + key + " and a cooresponding value");
		}
		return (String) ((Map<String,String>)workContext.get(INFRASTRUCTURE_TASK_WORKFLOW_DETAILS)).get(key);
	}
}