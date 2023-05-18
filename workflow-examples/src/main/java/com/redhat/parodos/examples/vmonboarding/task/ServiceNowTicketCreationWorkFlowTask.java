package com.redhat.parodos.examples.vmonboarding.task;

import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.examples.vmonboarding.dto.GetRequestItemResponseDto;
import com.redhat.parodos.examples.vmonboarding.dto.OrderServiceCatalogItemRequestDto;
import com.redhat.parodos.examples.vmonboarding.dto.OrderServiceCatalogItemRequestVariable;
import com.redhat.parodos.examples.vmonboarding.dto.OrderServiceCatalogItemResponseDto;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;

/**
 * An example of a task that create a serviceNow ticket
 *
 * @author Annel Ketcha (Github: anlukde)
 */

@Slf4j
public class ServiceNowTicketCreationWorkFlowTask extends BaseInfrastructureWorkFlowTask {

	private final String serviceNowUrl;

	private final String username;

	private final String password;

	private final String rhelSysId;

	private static final String SERVICE_CATALOG_CONTEXT_PATH = "/api/sn_sc/servicecatalog/items/%s/order_now";

	private static final String GET_ITEMS_BY_REQUEST_CONTEXT_PATH = "/api/now/table/sc_req_item?sysparm_query=%s&sysparm_limit=1";

	public ServiceNowTicketCreationWorkFlowTask(String serviceNowUrl, String rhelSysId, String username,
			String password) {
		this.serviceNowUrl = serviceNowUrl;
		this.rhelSysId = rhelSysId;
		this.username = username;
		this.password = password;
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		log.info("Start ServiceNowTicketCreationWorkFlowTask...");
		try {
			String urlString = serviceNowUrl + String.format(SERVICE_CATALOG_CONTEXT_PATH, rhelSysId);
			String vmName = getOptionalParameterValue(workContext, "hostname", "snowrhel");

			log.info("vm name: {}", vmName);

			OrderServiceCatalogItemRequestDto request = OrderServiceCatalogItemRequestDto.builder().quantity("1")
					.variables(OrderServiceCatalogItemRequestVariable.builder().vmName(vmName).build()).build();

			ResponseEntity<OrderServiceCatalogItemResponseDto> response = RestUtils.executePost(urlString, request,
					username, password, OrderServiceCatalogItemResponseDto.class);

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				String sysId = response.getBody().getResult().getSysId();
				String requestNumber = response.getBody().getResult().getNumber();
				log.info("Rest call completed, sys id: {}, request number: {}", sysId, requestNumber);
				Thread.sleep(7000);
				String getItemsByRequestUrl = serviceNowUrl
						+ String.format(GET_ITEMS_BY_REQUEST_CONTEXT_PATH, "request.number=" + requestNumber);
				ResponseEntity<GetRequestItemResponseDto> getItemsResponse = RestUtils
						.restExchange(getItemsByRequestUrl, username, password, GetRequestItemResponseDto.class);

				if (getItemsResponse.getStatusCode().is2xxSuccessful() && getItemsResponse.getBody() != null) {
					log.info("Rest call completed, request item number: {}",
							getItemsResponse.getBody().getResult().get(0).getNumber());
					addParameter(workContext, "JOB_ID",
							getItemsResponse.getBody().getResult().get(0).getShortDescription());
					return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
				}
				log.error("Call to the API was not successful. Response: {}", getItemsResponse.getStatusCode());
			}
			log.error("Call to the API was not successful. Response: {}", response.getStatusCode());
		}
		catch (Exception e) {
			log.error("There was an issue with the REST call: {}", e.getMessage());
		}
		return new

		DefaultWorkReport(WorkStatus.FAILED, workContext);
	}

}
