package com.redhat.parodos.workflow.task.log.service;

import java.util.UUID;

import com.redhat.parodos.workflow.task.log.WorkFlowTaskLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WorkFlowTaskLoggerTest {

	private WorkFlowTaskLogger workFlowTaskLogger;

	private WorkFlowLogService workFlowLogService;

	private Logger log;

	@BeforeEach
	public void init() {
		workFlowLogService = mock(WorkFlowLogService.class);
		log = mock(Logger.class);
		workFlowTaskLogger = new WorkFlowTaskLogger(UUID.randomUUID(), "test-task", workFlowLogService, log);
	}

	@Test
	void testLogInfo() {
		workFlowTaskLogger.logInfo("test {}", "test");
		verify(workFlowLogService, times(1)).writeLog(any(), any(), any());
	}

	@Test
	void testLogInfoWithSlf4j() {
		workFlowTaskLogger.logInfoWithSlf4j("test");
		verify(log, times(1)).info(anyString(), any(Object[].class));
	}

	@Test
	void testLogError() {
		workFlowTaskLogger.logError("test {}", "test");
		verify(workFlowLogService, times(1)).writeLog(any(), any(), any());
	}

	@Test
	void testLogErrorWithSlf4j() {
		workFlowTaskLogger.logErrorWithSlf4j("test");
		verify(log, times(1)).error(anyString(), any(Object[].class));
	}

	@Test
	void testLogWarn() {
		workFlowTaskLogger.logWarn("test {}", "test");
		verify(workFlowLogService, times(1)).writeLog(any(), any(), any());
	}

	@Test
	void testLogWarnWithSlf4j() {
		workFlowTaskLogger.logWarnWithSlf4j("test");
		verify(log, times(1)).warn(anyString(), any(Object[].class));
	}

}
