package com.redhat.parodos.workflow.version;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkFlowVersionServiceImplTest {

	private WorkFlowVersionServiceImpl workFlowVersionService;

	@BeforeEach
	public void setUp() {
		this.workFlowVersionService = new WorkFlowVersionServiceImpl();
	}

	@Test
	public void testGetHash() {
		// given
		Object object = new Object();

		// when
		assertDoesNotThrow(() -> {
			String hash = this.workFlowVersionService.getHash(object);
			assertEquals(hash, "b0c8ed039dc102c0bab6a5e979931a0b");
		});
	}

	@Test
	public void testInvalidGetHash() {
		// when
		assertThrows(RuntimeException.class, () -> {
			String hash = this.workFlowVersionService.getHash(null);
			assertEquals(hash, "");
		});
	}

}