package com.redhat.parodos.workflow.utils;

import java.util.NoSuchElementException;
import java.util.UUID;

import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflows.work.WorkContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WorkContextUtilsTest {

	@Test(expected = NoSuchElementException.class)
	public void testGetProjectIdWithoutProjectId() {
		WorkContext context = new WorkContext();
		WorkContextUtils.getProjectId(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetProjectIdWithIllegalProjectId() {
		WorkContext context = new WorkContext();
		WorkContextDelegate.write(context, WorkContextDelegate.ProcessType.PROJECT, WorkContextDelegate.Resource.ID,
				"illegal");
		WorkContextUtils.getProjectId(context);
	}

	@Test
	public void testSetAndGetProjectId() {
		UUID projectId = UUID.randomUUID();
		WorkContext context = new WorkContext();
		WorkContextUtils.setProjectId(context, projectId);
		assertEquals(projectId, WorkContextUtils.getProjectId(context));
	}

	@Test(expected = NullPointerException.class)
	public void testSetProjectIdWithNullId() {
		WorkContextUtils.setProjectId(new WorkContext(), null);
	}

	@Test(expected = NoSuchElementException.class)
	public void testGetMainExecutionIdWithoutProjectId() {
		WorkContext context = new WorkContext();
		WorkContextUtils.getMainExecutionId(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetMainExecutionIdWithIllegalId() {
		WorkContext context = new WorkContext();
		WorkContextDelegate.write(context, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, "illegal");
		WorkContextUtils.getMainExecutionId(context);
	}

	@Test
	public void testSetAndGetMainExecutionId() {
		UUID mainExecutionId = UUID.randomUUID();
		WorkContext context = new WorkContext();
		WorkContextUtils.setMainExecutionId(context, mainExecutionId);
		assertEquals(mainExecutionId, WorkContextUtils.getMainExecutionId(context));
	}

	@Test(expected = NullPointerException.class)
	public void testSetMainExecutionIdWithNullId() {
		WorkContextUtils.setMainExecutionId(new WorkContext(), null);
	}

}