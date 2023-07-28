package com.redhat.parodos.workflow.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
