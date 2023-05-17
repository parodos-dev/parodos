package com.redhat.parodos.workflow.task.log;

import java.util.UUID;

import com.redhat.parodos.workflow.enums.WorkFlowLogLevel;
import com.redhat.parodos.workflow.task.log.dto.WorkFlowTaskLog;
import com.redhat.parodos.workflow.task.log.service.WorkFlowLogService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

@AllArgsConstructor
public class WorkFlowTaskLogger {

	private UUID mainWorkFlowId;

	private String taskName;

	private WorkFlowLogService workFlowLogService;

	private Logger log;

	public void logInfo(String logText, String... value) {
		logWithLogLevel(WorkFlowLogLevel.INFO, logText, value);
	}

	public void logInfoWithSlf4j(String logText, String... value) {
		logInfo(logText, value);
		log.info(logText, (Object[]) value);
	}

	public void logError(String logText, String... value) {
		logWithLogLevel(WorkFlowLogLevel.ERROR, logText, value);
	}

	public void logErrorWithSlf4j(String logText, String... value) {
		logError(logText, value);
		log.error(logText, (Object[]) value);
	}

	public void logWarn(String logText, String... value) {
		logWithLogLevel(WorkFlowLogLevel.WARNING, logText, value);
	}

	public void logWarnWithSlf4j(String logText, String... value) {
		logWarn(logText, value);
		log.warn(logText, (Object[]) value);
	}

	private void logWithLogLevel(WorkFlowLogLevel workFlowLoglevel, String logText, String... value) {
		String formattedLog = value.length > 0 ? MessageFormatter.arrayFormat(logText, value).getMessage() : logText;
		workFlowLogService.writeLog(mainWorkFlowId, taskName,
				WorkFlowTaskLog.builder().logText(formattedLog).workFlowLoglevel(workFlowLoglevel).build());
	}

}
