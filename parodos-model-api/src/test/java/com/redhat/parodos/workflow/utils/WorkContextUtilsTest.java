package com.redhat.parodos.workflow.utils;

import java.util.NoSuchElementException;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WorkContextUtilsTest {

	@Test
	public void testGetProjectIdWithoutProjectId() {
		WorkContext context = new WorkContext();
		assertThrows(NoSuchElementException.class, () -> WorkContextUtils.getProjectId(context));
	}

	@Test
	public void testGetProjectIdWithIllegalProjectId() {
		WorkContext context = new WorkContext();
		WorkContextDelegate.write(context, WorkContextDelegate.ProcessType.PROJECT, WorkContextDelegate.Resource.ID,
				"illegal");
		assertThrows(IllegalArgumentException.class, () -> WorkContextUtils.getProjectId(context));
	}

	@Test
	public void testSetAndGetProjectId() {
		UUID projectId = UUID.randomUUID();
		WorkContext context = new WorkContext();
		WorkContextUtils.setProjectId(context, projectId);
		assertEquals(projectId, WorkContextUtils.getProjectId(context));
	}

	@Test
	public void testSetProjectIdWithNullId() {

		assertThrows(NullPointerException.class, () -> WorkContextUtils.setProjectId(new WorkContext(), null));
	}

	@Test
	public void testGetMainExecutionIdWithoutProjectId() {
		WorkContext context = new WorkContext();
		assertThrows(NoSuchElementException.class, () -> WorkContextUtils.getMainExecutionId(context));
	}

	@Test
	public void testGetMainExecutionIdWithIllegalId() {
		WorkContext context = new WorkContext();
		WorkContextDelegate.write(context, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, "illegal");
		assertThrows(IllegalArgumentException.class, () -> WorkContextUtils.getMainExecutionId(context));
	}

	@Test
	public void testSetAndGetMainExecutionId() {
		UUID mainExecutionId = UUID.randomUUID();
		WorkContext context = new WorkContext();
		WorkContextUtils.setMainExecutionId(context, mainExecutionId);
		assertEquals(mainExecutionId, WorkContextUtils.getMainExecutionId(context));
	}

	@Test
	public void testSetMainExecutionIdWithNullId() {

		assertThrows(NullPointerException.class, () -> WorkContextUtils.setMainExecutionId(new WorkContext(), null));
	}

}