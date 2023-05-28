package com.redhat.parodos.tasks.jdbc;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.parameter.WorkParameter;
import com.redhat.parodos.workflow.parameter.WorkParameterType;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcWorkFlowTask extends BaseWorkFlowTask {

	enum OperationType {

		QUERY, UPDATE, EXECUTE

	}

	private JdbcService service;

	public JdbcWorkFlowTask() {
		this.service = new JdbcServiceImpl();
	}

	JdbcWorkFlowTask(String beanName, JdbcService service) {
		this.setBeanName(beanName);
		this.service = service;
	}

	@Override
	public @NonNull List<WorkParameter> getWorkFlowTaskParameters() {
		LinkedList<WorkParameter> params = new LinkedList<>();
		params.add(WorkParameter.builder().key("url").type(WorkParameterType.TEXT).optional(false).description(
				"JDBC URL. E.g. jdbc:postgresql://localhost:5432/service?user=service&password=service123&ssl=true&sslmode=verify-full")
				.build());
		params.add(WorkParameter.builder().key("operation").type(WorkParameterType.TEXT).optional(false).description(
				"Type of operation this statement is performing. One of " + Arrays.toString(OperationType.values()))
				.build());
		params.add(WorkParameter.builder().key("statement").type(WorkParameterType.TEXT).optional(false)
				.description("The database statement to execute. E.g. 'select * from table'").build());
		params.add(WorkParameter.builder().key("result-ctx-key").type(WorkParameterType.TEXT).optional(true)
				.description("In query operation the result is stored in WorkContext with the provided key").build());
		return params;
	}

	@Override
	public @NonNull List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return List.of(WorkFlowTaskOutput.OTHER);
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		String url = "";
		try {
			url = getRequiredParameterValue("url");
			String operation = getRequiredParameterValue("operation");
			String statement = getRequiredParameterValue("statement");
			String resultCtxKey = getOptionalParameterValue("result-ctx-key", "");

			OperationType operationType = OperationType.valueOf(operation.toUpperCase());

			switch (operationType) {

				case QUERY -> {
					List<Map<String, Object>> result = this.service.query(url, statement);
					if (!resultCtxKey.isEmpty()) {
						workContext.put(resultCtxKey, new ObjectMapper().writeValueAsString(result));
					}
				}
				case UPDATE -> {
					this.service.update(url, statement);
				}
				case EXECUTE -> {
					this.service.execute(url, statement);
				}
			}
		}
		catch (MissingParameterException | JsonProcessingException e) {
			log.error("Jdbc task failed for URL " + url, e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

}
