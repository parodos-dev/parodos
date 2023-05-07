package com.redhat.parodos.workflow.context;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResourceTest {

	@Test
	public void testIsPublicWithPublicVisibility() {
		assertTrue(WorkContextDelegate.Resource.WORKFLOW_OPTIONS.isPublic());
	}

	@Test
	public void testIsPublicWithPrivateVisibility() {
		assertFalse(WorkContextDelegate.Resource.ARGUMENTS.isPublic());
	}

}
